# Tasks: Documentación interactiva y contrato de API

**Input**: Design documents from `/specs/005-swagger/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: The examples below include test tasks. This feature includes contract, UI, and parity checks as explicit quality gates.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirm the repository structure and the build wiring needed to ship the contract and Swagger UI from the backend.

- [X] T001 Confirm the feature workspace, backend paths, and contract artifact locations in specs/005-swagger/
- [X] T002 [P] Add Springdoc WebMVC dependency and resource publication wiring in backend/app/build.gradle
- [X] T003 [P] Prepare the static documentation asset directories under backend/app/src/main/resources/static/
- [X] T004 [P] Verify the contract source path and Web UI route contract in specs/005-swagger/contracts/openapi.yaml

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish the shared contract and validation pipeline before any story work starts.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T005 Configure the OpenAPI source-of-truth contract and metadata in specs/005-swagger/contracts/openapi.yaml
- [X] T006 [P] Wire the packaged contract to `/openapi.yaml` and expose Swagger UI in backend/app/build.gradle
- [X] T007 [P] Add the app configuration that points Swagger UI to the versioned YAML in backend/app/src/main/resources/application.properties
- [X] T008 Add a contract validation baseline that checks route parity against the current Spring MVC mappings in backend/app/src/test/java/com/dappunq/e2e/OpenApiParityTest.java
- [X] T009 Add the CI contract check to .github/workflows/ci-backend.yml so spec changes are covered even without backend code changes

**Checkpoint**: Foundation ready - all user stories can now proceed in parallel once the API contract and UI source are committed.

---

## Phase 3: User Story 1 - Explorar el contrato de la API (Priority: P1) 🎯 MVP

**Goal**: Publish a browsable, Spanish-language OpenAPI contract that lists all public backend operations, inputs, responses, and data models.

**Independent Test**: Open `/swagger-ui.html`, confirm the page loads, and verify that each public route from `SecurityConfig` appears with method, path, parameters, payload schema, and error responses.

### Tests for User Story 1

- [X] T010 [P] [US1] Add a Swagger UI smoke test covering `/swagger-ui.html` and `/openapi.yaml` in backend/app/src/test/java/com/dappunq/e2e/SwaggerUiSmokeTest.java
- [X] T011 [P] [US1] Add a parity test for the 5 current routes in backend/app/src/test/java/com/dappunq/e2e/OpenApiParityTest.java

### Implementation for User Story 1

- [X] T012 [P] [US1] Define the canonical OpenAPI metadata, tags, paths, and all five public operations in specs/005-swagger/contracts/openapi.yaml
- [X] T013 [US1] Add the `UserRequest`, `UserResponse`, `PlayerResponse`, `LeagueResponse`, `MessageError`, and `PlayerError` schemas in specs/005-swagger/contracts/openapi.yaml
- [X] T014 [US1] Configure Springdoc to serve the canonical YAML without duplicating controller annotations in backend/app/src/main/resources/application.properties
- [X] T015 [US1] Add the localized Swagger UI bootstrap customization in backend/app/src/main/resources/static/swagger-ui-custom.js
- [X] T016 [US1] Ensure the shipped static files are included in the Gradle resources output in backend/app/build.gradle

**Checkpoint**: At this point, User Story 1 should be fully functional and independently testable in the browser.

---

## Phase 4: User Story 2 - Probar operaciones desde la interfaz (Priority: P1)

**Goal**: Let developers execute test requests from Swagger UI and inspect the actual response, while warning before destructive or effectful calls.

**Independent Test**: Select an endpoint, provide valid values, execute a request, and confirm the browser shows the HTTP status, headers, and response payload.

### Tests for User Story 2

- [X] T017 [P] [US2] Add a request-execution smoke test for the Swagger UI flow in backend/app/src/test/java/com/dappunq/e2e/SwaggerUiRequestTest.java
- [X] T018 [US2] Add a register-confirmation regression test in backend/app/src/test/java/com/dappunq/e2e/SwaggerUiRegistrationE2ETest.java

### Implementation for User Story 2

- [X] T019 [P] [US2] Add an accessible confirmation dialog for `POST /register` in backend/app/src/main/resources/static/swagger-ui-custom.js
- [X] T020 [P] [US2] Add the UI hook that blocks unconfirmed registration while preserving keyboard and screen-reader accessibility in backend/app/src/main/resources/static/swagger-ui-custom.js
- [X] T021 [US2] Ensure the validation contract for required request fields is reflected in the YAML and UI behavior in specs/005-swagger/contracts/openapi.yaml
- [X] T022 [US2] Verify that successful and error responses from `/login`, `/register`, and player/user endpoints are displayed in the UI according to the current backend contract in backend/app/src/test/java/com/dappunq/e2e/SwaggerUiRequestTest.java

**Checkpoint**: At this point, User Story 2 should allow testing without leaving the API docs interface, while clearly warning before mutating requests.

---

## Phase 5: User Story 3 - Entender y usar la autenticación (Priority: P1)

**Goal**: Document the current public security posture and the JWT bearer scheme without claiming stricter auth than the backend actually enforces.

**Independent Test**: Open the contract and confirm `security: []` for the five current routes while the bearer scheme remains available in the Authorize dialog without enforcement.

### Tests for User Story 3

- [X] T023 [P] [US3] Add a security-check assertion for public routes in backend/app/src/test/java/com/dappunq/e2e/SecurityContractCheckTest.java
- [X] T024 [US3] Add a contract-level validation that ensures no documented operation claims a requirement the backend does not enforce in backend/app/src/test/java/com/dappunq/e2e/OpenApiSecurityParityTest.java

### Implementation for User Story 3

- [X] T025 [P] [US3] Declare the `bearerAuth` scheme and keep all current operations public in specs/005-swagger/contracts/openapi.yaml
- [X] T026 [US3] Document the `Authorization` response header and JWT usage in specs/005-swagger/contracts/openapi.yaml
- [X] T027 [US3] Add the Swagger UI `Authorize` support metadata and public-operation labeling in backend/app/src/main/resources/static/swagger-ui-custom.js
- [X] T028 [US3] Verify the current `SecurityConfig` public routes against the YAML contract in backend/app/src/test/java/com/dappunq/e2e/SecurityContractCheckTest.java

**Checkpoint**: At this point, the auth model shown in the UI matches the actual backend policy for the current release.

---

## Phase 6: User Story 4 - Mantener el contrato como fuente única (Priority: P1)

**Goal**: Guarantee that documentation, published contracts, and backend behavior remain aligned and versioned from one canonical source.

**Independent Test**: Change a route or schema in the specification and confirm the contract validation detects the mismatch before merge or publish.

### Tests for User Story 4

- [X] T029 [P] [US4] Add a contract validation test for mismatched routes or schemas in backend/app/src/test/java/com/dappunq/e2e/ContractConsistencyTest.java
- [X] T030 [US4] Add a versioning guard that rejects undocumented or stale contract changes in backend/app/src/test/java/com/dappunq/e2e/OpenApiVersionCheckTest.java

### Implementation for User Story 4

- [X] T031 [P] [US4] Update the spec and CI guardrail to run on `specs/005-swagger/**` changes in .github/workflows/ci-backend.yml
- [X] T032 [US4] Add a source-of-truth checklist and validation note in specs/005-swagger/quickstart.md
- [X] T033 [US4] Add a release/version traceability note in specs/005-swagger/contracts/openapi.yaml for the initial `1.0.0` baseline
- [X] T034 [US4] Verify no duplicate API descriptions are introduced outside the canonical YAML in backend/app/src/main/java/com/dappunq/controller/

**Checkpoint**: At this point, the project has one canonical API contract and a validation gate that catches mismatches.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Final QA, documentation cleanup, and accessibility review before the feature is considered done.

- [X] T035 [P] Review the Spanish copy, aria labeling, and keyboard flow for the custom Swagger UI layer in backend/app/src/main/resources/static/swagger-ui-custom.js
- [X] T036 [P] Review the contract examples and error models for current backend behavior in specs/005-swagger/contracts/openapi.yaml
- [X] T037 Validate manual quickstart steps and the browser workflow in specs/005-swagger/quickstart.md
- [X] T038 Run the backend Gradle validation suite and contract checks using `backend/gradlew.bat clean test`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories.
- **User Stories (Phase 3-6)**: All depend on Foundational completion; they can proceed in parallel once the contract and UI source is configured.
- **Polish (Phase 7)**: Depends on all desired user stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational; no dependency on other stories.
- **User Story 2 (P1)**: Can start after Foundational and may depend on the UI assets created in US1.
- **User Story 3 (P1)**: Can start after Foundational and should validate against the same contract baseline as US1.
- **User Story 4 (P1)**: Can start after Foundational and should complete after the contract and UI are stable.

### Within Each User Story

- Tests (when included) should be written first and fail before implementation if the project is following TDD.
- Contract/schema tasks come before UI customization tasks.
- UI work should be validated against the actual backend responses.

### Parallel Opportunities

- Setup tasks T002-T004 can run in parallel.
- Foundational tasks T006-T009 can run in parallel once T005 establishes the canonical contract draft.
- User Story 1 tests T010-T011 can run in parallel and implementation tasks T012-T016 can be split across the same story.
- User Story 2 tests T017-T018 can run in parallel.
- User Story 3 tests T023-T024 can run in parallel.
- User Story 4 tests T029-T030 can run in parallel.
- Polish tasks T035-T038 are best run after all story work is complete, with T038 as the final gate.

---

## Parallel Example: User Story 1

```bash
# Execute the contract checks together
Task: "Add a Swagger UI smoke test covering /swagger-ui.html and /openapi.yaml in backend/app/src/test/java/com/dappunq/e2e/SwaggerUiSmokeTest.java"
Task: "Add a parity test for the 5 current routes in backend/app/src/test/java/com/dappunq/e2e/OpenApiParityTest.java"

# Implement the contract and UI wiring together
Task: "Define the canonical OpenAPI metadata, tags, paths, and all five public operations in specs/005-swagger/contracts/openapi.yaml"
Task: "Add the localized Swagger UI bootstrap customization in backend/app/src/main/resources/static/swagger-ui-custom.js"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup.
2. Complete Phase 2: Foundational.
3. Complete Phase 3: User Story 1.
4. Stop and validate `/swagger-ui.html` loads and shows the canonical API documentation.
5. Deploy/demo only when the published contract is visible and correct.

### Incremental Delivery

1. Setup + Foundational → contract and UI can be served from the backend.
2. Add User Story 1 → test contract visibility and all public routes.
3. Add User Story 2 → test request execution and register confirmation.
4. Add User Story 3 → validate the auth contract against actual security policy.
5. Add User Story 4 → add parity/versioning and CI checks.
6. Run final polish and QA before merge.

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together.
2. Once Foundational is done:
   - Developer A: User Story 1 (contract and UI exposure)
   - Developer B: User Story 2 (interactive request flow and confirmation)
   - Developer C: User Story 3 (security documentation)
   - Developer D: User Story 4 (parity/versioning/CI)
3. All stories complete and integrate by the final polish gate.

---

## Notes

- [P] tasks = different files, no dependencies.
- [Story] label maps each task to the user story it supports.
- The contract YAML is the canonical source of truth and is the only documented API definition that should be published.
- The initial version is `1.0.0` and must be preserved unless a breaking change is intentionally introduced.
- Validate all contract and UI checks before considering the feature complete.

