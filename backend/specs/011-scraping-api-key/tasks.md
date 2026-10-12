---

description: "Implementation tasks for the scraping API key protection feature"
---

# Tasks: Protección del scraping con API key

**Input**: Design documents from `backend/specs/011-scraping-api-key/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Include tests because the feature explicitly requires authorization coverage, rotation rollback, and end-to-end validation for `POST /scrape` and the admin command.

**Organization**: Tasks are grouped by user story so each story can be implemented and tested independently after the shared foundation.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirm the current Spring Boot backend and module layout before adding the shared API-key authorization and rotation flow.

- [ ] T001 Confirm the Gradle backend module and existing Spring Security setup in `backend/settings.gradle`, `backend/build.gradle`, `backend/app/build.gradle`, and `backend/app/src/main/java/com/dappunq/security/SecurityConfig.java`
- [ ] T002 [P] Inventory the current scraping endpoint, service wiring, and repository conventions in `backend/app/src/main/java/com/dappunq/controller/ScrapingControllerRest.java`, `backend/app/src/main/java/com/dappunq/service/WhoScoredScrapingService.java`, and the persistence layer under `backend/app/src/main/java/com/dappunq/persistence/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish the shared API-key domain model, persistence contract, validation service, and security filter required by every story.

**⚠️ CRITICAL**: No user story implementation can begin until this phase is complete.

- [ ] T003 Create the `ApiKey` domain model in `backend/app/src/main/java/com/dappunq/model/ApiKey.java` with the `permission`, `keyHash`, `active`, `createdAt`, and `updatedAt` invariants from `backend/specs/011-scraping-api-key/data-model.md`
- [ ] T004 [P] Add `ApiKeyEntity` and `ApiKeyRepository` in `backend/app/src/main/java/com/dappunq/persistence/ApiKeyEntity.java` and `backend/app/src/main/java/com/dappunq/persistence/ApiKeyRepository.java` with the unique `permission` rule and active-by-permission lookup for `scraping`
- [ ] T005 [P] Add the pessimistic-lock update method and repository contract for serializing concurrent rotations in `backend/app/src/main/java/com/dappunq/persistence/ApiKeyRepository.java`
- [ ] T006 Implement the transactional rotation and validation service in `backend/app/src/main/java/com/dappunq/service/ApiKeyService.java` to generate, hash, validate, and replace the singleton `scraping` key without leaving zero or multiple active values
- [ ] T007 Add the `ApiKeyAuthenticationFilter` and enforce `hasAuthority("scraping")` for the protected route in `backend/app/src/main/java/com/dappunq/security/ApiKeyAuthenticationFilter.java` and `backend/app/src/main/java/com/dappunq/security/SecurityConfig.java`

**Checkpoint**: The domain model, repository contract, validation service, and security filter are ready; user stories can now be implemented independently.

---

## Phase 3: User Story 1 - Ejecutar scraping con una credencial autorizada (Priority: P1) 🎯 MVP

**Goal**: Allow only requests with a valid `X-API-Key` for the `scraping` permission to run `POST /scrape` while preserving the normal scraping behavior for authorized callers.

**Independent Test**: Submit a valid request to `POST /scrape` with the generated API key and verify HTTP 200 and the existing scraping execution path.

### Tests for User Story 1

- [ ] T008 [P] [US1] Add a MockMvc E2E test in `backend/app/src/test/java/com/dappunq/e2e/ScrapingApiKeyE2ETest.java` proving that a valid `X-API-Key` authorizes `POST /scrape` and the scrape path executes normally
- [ ] T009 [P] [US1] Add a focused security unit test in `backend/app/src/test/java/com/dappunq/security/ApiKeyAuthenticationFilterTest.java` verifying that a valid active `scraping` key yields `hasAuthority("scraping")` and passes the filter chain

### Implementation for User Story 1

- [ ] T010 [US1] Adjust `POST /scrape` authorization in `backend/app/src/main/java/com/dappunq/controller/ScrapingControllerRest.java` and `backend/app/src/main/java/com/dappunq/security/SecurityConfig.java` so the endpoint is guarded by the API-key authority instead of `permitAll`
- [ ] T011 [US1] Implement the active key lookup and permission validation in `backend/app/src/main/java/com/dappunq/service/ApiKeyService.java` so the same shared credential can be used by multiple authorized clients without creating additional records
- [ ] T012 [US1] Wire the authorized request context and success path in `backend/app/src/main/java/com/dappunq/security/ApiKeyAuthenticationFilter.java` so it authenticates a valid `X-API-Key` before the controller is invoked

**Checkpoint**: A valid shared `scraping` credential can access `POST /scrape` without changing the normal scraping contract for authorized users.

---

## Phase 4: User Story 2 - Rechazar solicitudes no autorizadas (Priority: P1)

**Goal**: Reject missing, empty, malformed, or invalid API keys before any scraping logic is executed and ensure the response message never exposes the secret.

**Independent Test**: Send requests without header, with empty/invalid header values, or with a stale or wrong-permission key and verify `401` without invoking the scraping service.

### Tests for User Story 2

- [ ] T013 [P] [US2] Add failing request-authorization tests in `backend/app/src/test/java/com/dappunq/e2e/ScrapingApiKeyE2ETest.java` for missing, blank, malformed, and stale `X-API-Key` values
- [ ] T014 [P] [US2] Extend `backend/app/src/test/java/com/dappunq/security/ApiKeyAuthenticationFilterTest.java` to prove the filter blocks unauthorized requests and leaves the scraping service uninvoked

### Implementation for User Story 2

- [ ] T015 [US2] Harden the filter logic in `backend/app/src/main/java/com/dappunq/security/ApiKeyAuthenticationFilter.java` to reject missing, blank, whitespace-padded, malformed, or non-`scraping` keys before continuing the request chain
- [ ] T016 [US2] Update the authorization error handling in `backend/app/src/main/java/com/dappunq/exception/GlobalExceptionHandler.java` and/or the security entry point so the response is `401` and uses a generic Spanish message without returning the secret or stack traces
- [ ] T017 [US2] Verify that the scraping service is not called on rejected requests in `backend/app/src/main/java/com/dappunq/security/ApiKeyAuthenticationFilter.java` and the relevant integration tests under `backend/app/src/test/java/com/dappunq/integration/`

**Checkpoint**: Any request without a valid `X-API-Key` is rejected before scraping begins and the response remains redacted and comprehensible.

---

## Phase 5: User Story 3 - Generar y distribuir la credencial (Priority: P2)

**Goal**: Provide a deploy-time operation to rotate the singleton `scraping` API key, persist the new hash atomically, and print the plaintext value only after the transaction succeeds.

**Independent Test**: Run the rotation command in a configured environment, verify that the new key is printed, the old key stops authorizing, and a second execution keeps exactly one active credential.

### Tests for User Story 3

- [ ] T018 [P] [US3] Add a service test in `backend/app/src/test/java/com/dappunq/service/ApiKeyServiceTest.java` covering first-time creation, replacement of an existing active key, and rollback on persistence failure
- [ ] T019 [P] [US3] Add a command-level contract test in `backend/app/src/test/java/com/dappunq/integration/RotateScrapingApiKeyCommandTest.java` for successful output to `stdout`, failure to `stderr`, and no plaintext secret exposure in logs or responses

### Implementation for User Story 3

- [ ] T020 [US3] Implement the application command entrypoint in `backend/app/src/main/java/com/dappunq/config/AppCommandConfig.java` or the Spring application bootstrap so `--app.command=rotate-scraping-api-key` executes the rotation flow without exposing an HTTP admin endpoint
- [ ] T021 [US3] Add the transactional rotation command logic in `backend/app/src/main/java/com/dappunq/service/ApiKeyService.java` or a dedicated `RotateScrapingApiKeyCommand.java` to generate a new secret, persist the hash only after a successful commit, and keep the prior active key usable if validation or storage fails
- [ ] T022 [US3] Create the PowerShell wrapper in `backend/scripts/rotate-scraping-api-key.ps1` to invoke the application command, surface the plaintext value once on `stdout`, and return a non-zero exit code with a Spanish error message on persistence or connectivity failures
- [ ] T023 [US3] Validate the command contract and documentation in `backend/specs/011-scraping-api-key/contracts/rotate-scraping-api-key.md` and `backend/specs/011-scraping-api-key/quickstart.md` so the generated secret and error behavior match the implementation

**Checkpoint**: The deploy-time rotation command creates or refreshes exactly one active `scraping` key and prints only the new secret after a successful transaction.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final validation of authorization, rotation, and secret redaction across all stories.

- [ ] T024 [P] Review and update the public contract for `POST /scrape` in `backend/specs/011-scraping-api-key/contracts/scrape.md` and the app OpenAPI metadata so the `X-API-Key` requirement and `401` behavior match the implemented filter
- [ ] T025 [P] Add or tighten tests for secret redaction and log safety in `backend/app/src/test/java/com/dappunq/security/ApiKeyAuthenticationFilterTest.java` and `backend/app/src/test/java/com/dappunq/integration/RotateScrapingApiKeyCommandTest.java`
- [ ] T026 Run the focused validation commands from `backend/specs/011-scraping-api-key/quickstart.md`: `./gradlew.bat :app:test` and the rotation script flow to confirm the API-key contract, transaction safety, and authorized scrape path all pass together

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; it can start immediately.
- **Foundational (Phase 2)**: Depends on Setup and blocks all user stories.
- **User Story 1 (Phase 3)**: Depends on Foundational; it delivers the MVP authorization path.
- **User Story 2 (Phase 4)**: Depends on the shared filter/service foundation and validates the unauthorized rejection path.
- **User Story 3 (Phase 5)**: Depends on the key persistence and validation service; it adds the rotation command and operational safety.
- **Polish (Phase 6)**: Depends on all implemented stories being complete.

### User Story Dependencies

- **US1 (P1)**: Can start after Phase 2; no dependency on another story.
- **US2 (P1)**: Can start after Phase 2 and is independent of US3; it verifies the unauthorized path.
- **US3 (P2)**: Can start after Phase 2 and can proceed in parallel with US1/US2 once the shared persistence service is ready.

### Parallel Opportunities

- T002 can run in parallel with T001 during the setup review.
- T004 and T005 can run in parallel once the domain model is defined.
- T008 and T009 can run in parallel for US1 tests.
- T013 and T014 can run in parallel for US2 tests.
- T018 and T019 can run in parallel for US3 tests.
- T024 and T025 can run together during final polish after the stories are complete.

## Parallel Example: User Story 1

```text
Task T008: `ScrapingApiKeyE2ETest` for a valid authorized request
Task T009: `ApiKeyAuthenticationFilterTest` for the valid `scraping` authority
Task T010: controller + security gate update
Task T011: service-side validation of the active shared key
``` 

## Parallel Example: User Story 3

```text
Task T018: rotation service tests for create/replace/rollback
Task T019: command contract tests for `stdout`, `stderr`, and secret safety
Task T020: command entrypoint wiring
Task T021: transactional rotation implementation
``` 

## Implementation Strategy

### MVP First (US1 Only)

1. Complete Phase 1 and Phase 2.
2. Implement US1: valid request authorization, permit filter, and the active `scraping` key check.
3. Stop and validate a successful `POST /scrape` request with a valid `X-API-Key` before continuing.

### Incremental Delivery

1. Add US2 to harden the rejection flow for missing, invalid, and stale keys.
2. Add US3 to provide the operational rotation command and safe secret generation flow.
3. Complete the final validation and cross-cutting docs/tests for security and redaction.

## Notes

- Each task uses a checkbox, sequential ID, and an exact file path.
- `[P]` identifies tasks that can run in parallel without depending on incomplete work in the same file.
- The implementation should preserve the existing scraping behavior for valid requests and reject unauthorized ones before the scraping service is invoked.
- All secret-bearing outputs must remain redacted from normal logs and error payloads.
