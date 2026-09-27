# Tasks: Football Player Data

**Input**: Design documents from `/specs/002-football-player-data/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Initialize the backend structure, local configuration, and test harness for the football player module.

- [X] T001 Create player module package structure in `backend/app/src/main/java/com/dappunq/controller/`, `backend/app/src/main/java/com/dappunq/service/`, `backend/app/src/main/java/com/dappunq/model/`, `backend/app/src/main/java/com/dappunq/persistence/`, `backend/app/src/main/java/com/dappunq/dto/`, `backend/app/src/main/java/com/dappunq/exception/`, and `backend/app/src/main/java/com/dappunq/sync/`
- [X] T002 [P] Configure local PostgreSQL development profile and datasource settings in `backend/app/src/main/resources/application-local.properties`
- [X] T003 [P] Add shared Testcontainers configuration for integration/E2E tests in `backend/app/src/test/java/com/dappunq/config/TestcontainersConfig.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that must be complete before any user story implementation can begin.

**Critical**: No user story work can start until this phase is complete.

- [X] T004 Define the domain entity and integrity rules for player records in `backend/app/src/main/java/com/dappunq/model/Jugador.java`
- [X] T005 [P] Define historical snapshot entity and persistence contract in `backend/app/src/main/java/com/dappunq/model/SnapshotEstadistico.java` and `backend/app/src/main/java/com/dappunq/persistence/SnapshotEstadisticoRepository.java`
- [X] T006 [P] Implement player repository layer with filtering support in `backend/app/src/main/java/com/dappunq/persistence/JugadorRepository.java`
- [X] T007 Define API/DTO contract and validation records in `backend/app/src/main/java/com/dappunq/dto/JugadorRequestDTO.java` and `backend/app/src/main/java/com/dappunq/dto/JugadorResponseDTO.java`
- [X] T008 Implement domain exception hierarchy and API error handling in `backend/app/src/main/java/com/dappunq/exception/JugadorNotFoundException.java`, `backend/app/src/main/java/com/dappunq/exception/InvalidPlayerDataException.java`, `backend/app/src/main/java/com/dappunq/exception/DuplicatePlayerException.java`, and `backend/app/src/main/java/com/dappunq/exception/GlobalExceptionHandler.java`
- [X] T009 Create base service scaffolding and cross-cutting validation dependencies in `backend/app/src/main/java/com/dappunq/service/JugadorService.java`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel.

---

## Phase 3: User Story 1 - Consultar y filtrar el listado de jugadores (Priority: P1) 🎯 MVP

**Goal**: Expose a searchable catalog of players that can be filtered by league, team, and position through the REST API.

**Independent Test**: Run a request to `/players?liga=La%20Liga&equipo=Real%20Madrid&posicion=Delantero` and verify that only matching records are returned and that no extraneous players leak into the response.

### Tests for User Story 1

- [X] T010 [P] [US1] Add filtered list integration test in `backend/app/src/test/java/com/dappunq/integration/JugadorListadoIntegrationTest.java`
- [X] T011 [P] [US1] Add player-by-id integration test in `backend/app/src/test/java/com/dappunq/integration/JugadorDetalleIntegrationTest.java`

### Implementation for User Story 1

- [X] T012 [P] [US1] Add repository query methods for `liga`, `equipo`, and `posicion` filtering in `backend/app/src/main/java/com/dappunq/persistence/JugadorRepository.java`
- [X] T013 [US1] Implement list and lookup business logic in `backend/app/src/main/java/com/dappunq/service/JugadorService.java`
- [X] T014 [US1] Implement the REST endpoint for `GET /players` and `GET /players/{id}` in `backend/app/src/main/java/com/dappunq/controller/JugadorControllerRest.java`
- [X] T015 [US1] Add response mapping and serialization rules for `JugadorResponseDTO` in `backend/app/src/main/java/com/dappunq/dto/JugadorResponseDTO.java`
- [X] T016 [US1] Add validation and sanitization logic for request inputs and non-empty payloads in `backend/app/src/main/java/com/dappunq/dto/JugadorRequestDTO.java`
- [X] T017 [US1] Validate edge cases for missing players and empty search results in `backend/app/src/main/java/com/dappunq/exception/GlobalExceptionHandler.java`

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently.

---

## Phase 4: User Story 2 - Sincronizar y conservar estadísticas externas (Priority: P2)

**Goal**: Import external player statistics, normalize them into the internal model, and keep a historical local snapshot so the API remains available when providers fail.

**Independent Test**: Execute the sync process against a mocked or stub external payload and verify that the snapshot is persisted locally with a timestamp and then returned from the local store when the provider is unavailable.

### Tests for User Story 2

- [ ] T018 [P] [US2] Add external sync integration test in `backend/app/src/test/java/com/dappunq/integration/JugadorSyncIntegrationTest.java`
- [ ] T019 [P] [US2] Add historical fallback test for offline provider behavior in `backend/app/src/test/java/com/dappunq/integration/JugadorFallbackIntegrationTest.java`

### Implementation for User Story 2

- [ ] T020 [P] [US2] Implement external provider normalization and mapping in `backend/app/src/main/java/com/dappunq/sync/PlayerSyncService.java`
- [ ] T021 [US2] Persist player snapshots with timestamps and source tracking in `backend/app/src/main/java/com/dappunq/model/SnapshotEstadistico.java` and `backend/app/src/main/java/com/dappunq/persistence/SnapshotEstadisticoRepository.java`
- [ ] T022 [US2] Add local-cache lookup logic to prefer persisted snapshots when external providers are unavailable in `backend/app/src/main/java/com/dappunq/service/JugadorService.java`
- [ ] T023 [US2] Validate provider data integrity before persisting in `backend/app/src/main/java/com/dappunq/model/Jugador.java`
- [ ] T024 [US2] Add source-aware error handling and retry-safe sync behavior in `backend/app/src/main/java/com/dappunq/exception/GlobalExceptionHandler.java`

**Checkpoint**: At this point, User Story 2 should persist local historical data and tolerate external outages.

---

## Phase 5: User Story 3 - Validar la integridad de la información de cada jugador (Priority: P3)

**Goal**: Ensure each record is unique, complete, and consistent with league/team associations before it is returned to clients.

**Independent Test**: Create invalid or duplicate player data and verify that the API rejects it with a clear validation or domain error while preserving the integrity of the dataset.

### Tests for User Story 3

- [ ] T025 [P] [US3] Add duplicate and incomplete-record validation test in `backend/app/src/test/java/com/dappunq/integration/JugadorIntegridadIntegrationTest.java`
- [ ] T026 [P] [US3] Add domain-level validation unit tests in `backend/app/src/test/java/com/dappunq/unit/JugadorValidationTest.java`

### Implementation for User Story 3

- [ ] T027 [P] [US3] Enforce unique identifier and required field validation in `backend/app/src/main/java/com/dappunq/model/Jugador.java`
- [ ] T028 [US3] Implement duplicate detection and invalid dataset rejection in `backend/app/src/main/java/com/dappunq/service/JugadorService.java`
- [ ] T029 [US3] Add explicit validation exceptions and messages in `backend/app/src/main/java/com/dappunq/exception/DuplicatePlayerException.java` and `backend/app/src/main/java/com/dappunq/exception/InvalidPlayerDataException.java`
- [ ] T030 [US3] Add repository-level uniqueness checks in `backend/app/src/main/java/com/dappunq/persistence/JugadorRepository.java`
- [ ] T031 [US3] Verify league/team consistency and query-level empty results in `backend/app/src/main/java/com/dappunq/controller/JugadorControllerRest.java`

**Checkpoint**: At this point, User Story 3 should guarantee dataset integrity and clear business errors.

---

## Phase 6: User Story 4 - Integrar el catálogo de jugadores dentro de la API existente (Priority: P4)

**Goal**: Integrate the football player domain into the same REST service without breaking existing user-related behavior.

**Independent Test**: Run the old user-related endpoints and the new player endpoints together to confirm they coexist without regressions or contract changes for the current API.

### Tests for User Story 4

- [ ] T032 [P] [US4] Add end-to-end API coexistence test in `backend/app/src/test/java/com/dappunq/e2e/JugadorApiE2ETest.java`
- [ ] T033 [P] [US4] Add regression smoke test for existing user endpoints in `backend/app/src/test/java/com/dappunq/e2e/UserApiRegressionE2ETest.java`

### Implementation for User Story 4

- [ ] T034 [P] [US4] Ensure the new player routes are wired into the existing Spring Boot app in `backend/app/src/main/java/com/dappunq/controller/JugadorControllerRest.java`
- [ ] T035 [US4] Validate compatibility with current user routes and shared security configuration in `backend/app/src/main/java/com/dappunq/security/SecurityConfig.java`
- [ ] T036 [US4] Confirm the app still starts and serves both domains in `backend/app/src/main/java/com/dappunq/DappApplication.java`
- [ ] T037 [US4] Validate API documentation and request examples against the contract in `specs/002-football-player-data/contracts/players-api.yaml`

**Checkpoint**: At this point, the player catalog should coexist cleanly with the existing API and user flows.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Final cleanup, documentation, and validation across the whole feature.

- [ ] T038 [P] Update backend documentation and runbook in `backend/README.md` and `specs/002-football-player-data/quickstart.md`
- [ ] T039 [P] Review and harden error messages and HTTP status mapping in `backend/app/src/main/java/com/dappunq/exception/GlobalExceptionHandler.java`
- [ ] T040 [P] Add or refine unit coverage for validation edge cases in `backend/app/src/test/java/com/dappunq/unit/`
- [ ] T041 Run the full backend test suite with Testcontainers in `backend/app/src/test/java/`
- [ ] T042 Validate player queries against the contract in `specs/002-football-player-data/contracts/players-api.yaml`
- [ ] T043 Run local quickstart validation for `GET /players` and `GET /players/{id}` from `specs/002-football-player-data/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion; it blocks all user stories.
- **User Story 1 (Phase 3)**: Depends on Foundational completion; it is the MVP story.
- **User Story 2 (Phase 4)**: Depends on Foundational completion and can build on validated player data from US1.
- **User Story 3 (Phase 5)**: Depends on Foundational completion and should validate data integrity after US1.
- **User Story 4 (Phase 6)**: Depends on US1 and the preserved API runtime after Foundational.
- **Polish (Final Phase)**: Depends on all desired user stories being complete.

### User Story Dependencies

- **US1 (P1)**: Starts after Phase 2; no dependencies on other stories.
- **US2 (P2)**: Starts after Phase 2; may run in parallel with US1 when sync and persistence are stable.
- **US3 (P3)**: Starts after Phase 2; can run in parallel with US2 once the base model exists.
- **US4 (P4)**: Starts after US1 is stable and should finish once integration has been proven with existing API routes.

### Parallel Opportunities

- Setup tasks T002 and T003 can run in parallel.
- Foundational tasks T005, T006, T007, and T008 can run in parallel once T004 creates the base model.
- Story tests for each user story are independent and can be executed concurrently.
- Repository, DTO, and controller tasks within a single story are parallelizable when they touch different files.
- Different user stories can be implemented by separate developers once the foundational phase is complete.

---

## Parallel Example: User Story 1

```bash
# Ejecutar tests del listado y detalle del jugador en paralelo
Task: "Add filtered list integration test in backend/app/src/test/java/com/dappunq/integration/JugadorListadoIntegrationTest.java"
Task: "Add player-by-id integration test in backend/app/src/test/java/com/dappunq/integration/JugadorDetalleIntegrationTest.java"

# Implementar repositorio y DTOs del dominio del jugador en paralelo
Task: "Add repository query methods for liga, equipo, and posicion filtering in backend/app/src/main/java/com/dappunq/persistence/JugadorRepository.java"
Task: "Define API/DTO contract and validation records in backend/app/src/main/java/com/dappunq/dto/JugadorRequestDTO.java and backend/app/src/main/java/com/dappunq/dto/JugadorResponseDTO.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup.
2. Complete Phase 2: Foundational.
3. Complete Phase 3: User Story 1.
4. Stop and validate the user story independently using the quickstart examples.
5. Only then continue with US2, US3, and US4.

### Incremental Delivery

1. Setup + Foundational -> base API and persistence ready.
2. Add User Story 1 -> deliver filtered player catalog (MVP).
3. Add User Story 2 -> add sync and offline resilience.
4. Add User Story 3 -> enforce integrity and validation.
5. Add User Story 4 -> integrate with existing service without regression.
6. Finish with polish and regression validation.

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together.
2. Developer A: User Story 1
3. Developer B: User Story 2
4. Developer C: User Story 3
5. Developer D: User Story 4 and polish validation
6. Merge only after all story checkpoints pass.

---

## Notes

- [P] tasks are independent and should be assigned to different files or components.
- Each user story is independently testable and reviewable.
- Test tasks are intentionally included because the feature spec defines independent test criteria and requires integration/E2E validation with Testcontainers.
- The implementation must respect the layered architecture required by the constitution: controller -> service -> model/persistence.
- Do not widen scope beyond the football player catalog required by this feature.
