---

description: "Implementation tasks for the flat player response contract"
---

# Tasks: Formato de respuesta de jugadores

**Input**: Design documents from `backend/specs/010-players-response-format/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/players-api.yaml, quickstart.md

**Tests**: Test tasks are included because RF-012 and the plan explicitly require service and MockMvc coverage for successful, empty, partial, zero-value, unavailable-catalog, and by-ID cases.

**Organization**: Tasks are grouped by user story so each story can be implemented and tested independently after the shared foundation.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirm the existing backend module and test conventions used by this contract change.

- [X] T001 Confirm the Gradle backend module and existing Spring Boot test configuration in `backend/settings.gradle`, `backend/build.gradle`, and `backend/app/build.gradle`
- [X] T002 [P] Inventory the current player DTO, entity, repository, controller, service, OpenAPI, and MockMvc test paths against `backend/specs/010-players-response-format/plan.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish the shared response and mapping foundation required by all player endpoint stories.

**⚠️ CRITICAL**: No user story implementation can begin until this phase is complete.

- [X] T003 [P] Create `TeamResponse` in `backend/app/src/main/java/com/dappunq/dto/TeamResponse.java` with the public `externalId` and `name` fields and no persistence identifier
- [X] T004 Replace the legacy fields in `backend/app/src/main/java/com/dappunq/dto/PlayerResponse.java` with the complete public player field set from `backend/specs/010-players-response-format/data-model.md`, excluding the internal `id`
- [X] T005 [P] Add focused response-shape assertions and fixture helpers for the new records in `backend/app/src/test/java/com/dappunq/service/PlayerCatalogReadServiceTest.java`

**Checkpoint**: Shared DTO types and test fixtures are ready; user stories can now be implemented.

---

## Phase 3: User Story 1 - Consultar jugadores con el nuevo contrato (Priority: P1) 🎯 MVP

**Goal**: Return every persisted player as one flat `PlayerResponse` with the complete public data and correct team/league relationships.

**Independent Test**: Mock `PlayerRepository.findAll()` with players from multiple leagues, call `GET /players`, and verify HTTP 200 with one flat array whose elements contain the documented fields and relationships.

### Tests for User Story 1

- [X] T006 [P] [US1] Add service tests in `backend/app/src/test/java/com/dappunq/service/PlayerCatalogReadServiceTest.java` proving `findCatalog()` maps every `PlayerEntity` returned by `PlayerRepository.findAll()` into one ordered flat list
- [X] T007 [P] [US1] Update successful multi-player and multi-league response assertions in `backend/app/src/test/java/com/dappunq/e2e/PlayerCatalogE2ETest.java` to validate the flat array and nested `team`/`league` objects
- [X] T008 [P] [US1] Adapt legacy grouping scenarios in `backend/app/src/test/java/com/dappunq/e2e/PlayerCatalogGroupingE2ETest.java` to assert that no league-based inner arrays or fabricated empty entries are returned

### Implementation for User Story 1

- [X] T009 [US1] Refactor `findCatalog()` and player mapping in `backend/app/src/main/java/com/dappunq/service/PlayerCatalogReadService.java` to use one `PlayerRepository.findAll()` read and return `List<PlayerResponse>`
- [X] T010 [US1] Add complete `TeamEntity` to `TeamResponse` mapping and all `PlayerEntity` statistics mappings in `backend/app/src/main/java/com/dappunq/service/PlayerCatalogReadService.java`
- [X] T011 [US1] Update `getPlayers()` in `backend/app/src/main/java/com/dappunq/controller/PlayerControllerRest.java` to expose the flat `List<PlayerResponse>` result without changing public access

**Checkpoint**: `GET /players` returns a complete flat player catalog and preserves each player's league association.

---

## Phase 4: User Story 2 - Mantener resultados consistentes (Priority: P1)

**Goal**: Keep response shape, nullability, zero values, empty catalogs, and availability errors consistent across all list results.

**Independent Test**: Exercise complete, partial, zero-valued, empty, and unavailable catalog cases and verify stable JSON shape, `null` versus zero semantics, `[]`, and HTTP 503 error payload.

### Tests for User Story 2

- [X] T012 [P] [US2] Add service tests in `backend/app/src/test/java/com/dappunq/service/PlayerCatalogReadServiceTest.java` for an empty `findAll()` result, null optional fields, zero-valued statistics, repository failure, and verification that `LeagueRepository` is never read
- [X] T013 [P] [US2] Update `backend/app/src/test/java/com/dappunq/e2e/PlayerIncompleteDataE2ETest.java` to verify optional nulls remain null or absent according to Jackson configuration while valid zeros remain serialized as zero
- [X] T014 [P] [US2] Update `backend/app/src/test/java/com/dappunq/e2e/PlayerCatalogE2ETest.java` with empty-list and HTTP 503 assertions for the flat listing contract

### Implementation for User Story 2

- [X] T015 [US2] Remove the `LeagueRepository` catalog dependency and league-grouping helper from `backend/app/src/main/java/com/dappunq/service/PlayerCatalogReadService.java`, preserving `CatalogUnavailableException` wrapping for list-read failures
- [X] T016 [US2] Update `backend/app/src/main/resources/static/openapi.yaml` so `GET /players` documents a flat `PlayerResponse[]`, `[]`, public access, all new fields, and the existing 503 `PlayerError`
- [X] T017 [US2] Align `backend/specs/010-players-response-format/contracts/players-api.yaml` with the final DTO field types, nullability, nested team/league schemas, empty response, and availability error behavior

**Checkpoint**: All list response variants are shape-consistent, preserve null/zero semantics, and retain the established 503 contract.

---

## Phase 5: User Story 3 - Consultar detalles sin afectar el listado (Priority: P2)

**Goal**: Preserve the existing `GET /players/{id}` success and validation/not-found behavior while the list contract changes.

**Independent Test**: Query an existing public player ID and invalid or missing IDs through MockMvc and verify the existing response and 400/404 behavior.

### Tests for User Story 3

- [X] T018 [P] [US3] Update `backend/app/src/test/java/com/dappunq/e2e/PlayerByIdE2ETest.java` to verify the complete new `PlayerResponse` for an existing player and unchanged invalid/not-found responses
- [X] T019 [P] [US3] Add service assertions in `backend/app/src/test/java/com/dappunq/service/PlayerCatalogReadServiceTest.java` that `findPlayer()` still uses `findByExternalId`, validates positive IDs, and maps the same player relationship fields

### Implementation for User Story 3

- [X] T020 [US3] Preserve and verify `findPlayer(Integer id)` behavior in `backend/app/src/main/java/com/dappunq/service/PlayerCatalogReadService.java` while routing it through the shared complete response mapper
- [X] T021 [US3] Preserve the `GET /players/{id}` route and public error documentation in `backend/app/src/main/java/com/dappunq/controller/PlayerControllerRest.java` and `backend/app/src/main/resources/static/openapi.yaml`

**Checkpoint**: List consumers receive the new contract without changing individual-player lookup semantics.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Validate the complete contract and remove stale grouping assumptions.

- [X] T022 [P] Update player endpoint examples and expected response bodies in `docs/postman/UserAuth.postman_collection.json` only if the collection contains player requests; otherwise record that no player request exists and no collection change is needed (the collection contains player requests but no embedded response examples, so no payload change was required)
- [X] T023 [P] Remove stale grouping comments, imports, and constructor usage from `backend/app/src/main/java/com/dappunq/controller/PlayerControllerRest.java`, `backend/app/src/main/java/com/dappunq/service/PlayerCatalogReadService.java`, and affected tests
- [X] T024 Run the focused catalog test command from `backend/specs/010-players-response-format/quickstart.md` and confirm the final response shape, empty list, null/zero handling, 503 error, and by-ID behavior

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; can start immediately.
- **Foundational (Phase 2)**: Depends on Setup; blocks all user stories.
- **User Story 1 (Phase 3)**: Depends on Foundational and delivers the MVP listing.
- **User Story 2 (Phase 4)**: Depends on the shared DTO/mapping foundation and integrates with US1's list implementation.
- **User Story 3 (Phase 5)**: Depends on the shared response mapper; it must preserve the existing by-ID route while US1 changes the list.
- **Polish (Phase 6)**: Depends on all selected user stories being complete.

### User Story Dependencies

- **US1 (P1)**: Can start after Phase 2; no dependency on another story.
- **US2 (P1)**: Can start after Phase 2, but its list-error and consistency checks validate the US1 list implementation.
- **US3 (P2)**: Can start after Phase 2; shares the response mapper with US1 but remains independently testable through `GET /players/{id}`.

### Parallel Opportunities

- T003, T005, and T002 can run in parallel during setup/foundation when their files are not being edited by another task.
- Within US1, T006, T007, and T008 are parallel test updates; T009 and T010 are sequential because T010 extends the mapper introduced by T009.
- Within US2, T012, T013, and T014 are parallel test updates; T016 and T017 can run in parallel with each other after the response contract is settled.
- Within US3, T018 and T019 are parallel test updates.
- US1 and US3 can be developed in parallel after Phase 2 if the shared DTO/mapping edits are coordinated.

## Parallel Example: User Story 1

```text
Task T006: service mapping and repository interaction tests
Task T007: successful flat-list MockMvc assertions
Task T008: replacement of grouping-specific MockMvc assertions
```

## Parallel Example: User Story 2

```text
Task T012: service null/zero/empty/error and repository verification tests
Task T013: incomplete-data MockMvc assertions
Task T014: empty-list and 503 MockMvc assertions
Task T016: public OpenAPI update
Task T017: feature contract update
```

## Implementation Strategy

### MVP First (US1 Only)

1. Complete Phase 1 and Phase 2.
2. Complete US1, including DTOs, repository-backed flat mapping, controller response, and successful listing tests.
3. Stop and validate the flat `GET /players` response independently.

### Incremental Delivery

1. Add US2 to cover empty, partial, zero-valued, and unavailable catalog behavior plus public documentation.
2. Add US3 to prove by-ID behavior remains unchanged.
3. Complete Polish tasks and run the quickstart validation command.

## Notes

- Every task has a checkbox, sequential ID, required story label where applicable, and an exact file path.
- `[P]` marks tasks that can proceed in parallel without depending on incomplete work in the same file.
- Existing tests are updated only because this feature explicitly replaces their obsolete grouping/DTO contract.

