---

description: "Lista de tareas para implementar el catálogo de jugadores por liga"
---

# Tasks: Catálogo de jugadores por liga

**Input**: Documentos de diseño de `/specs/003-player-catalog/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/players-api.yaml` y `quickstart.md`

**Scope**: La implementación modifica únicamente `backend/`; el frontend queda fuera del alcance de esta feature. El contrato REST generado permite que el frontend implemente las tablas expandibles y la representación visual de valores nulos.

**Tests**: Se incluyen porque la especificación y el quickstart exigen pruebas unitarias, de integración con PostgreSQL/Testcontainers y E2E con MockMvc.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar dependencias, configuración y estructura para la integración externa y persistencia del catálogo.

- [X] T001 [P] Add Spring Data JPA, PostgreSQL, validation, Jackson, Testcontainers y Mockito dependencies in `backend/app/build.gradle`
- [X] T002 [P] Configure PostgreSQL datasource, `ddl-auto=update`, football-data base URL and `${FOOTBALL_DATA_AUTH_TOKEN}` placeholder in `backend/app/src/main/resources/application.properties`
- [X] T003 [P] Configure local development datasource and football-data timeout/profile overrides without storing credentials in `backend/app/src/main/resources/application-local.properties`
- [X] T004 Create package structure for `controller`, `dto`, `model`, `persistence`, `service`, `config` and feature tests under `backend/app/src/main/java/com/dappunq` and `backend/app/src/test/java/com/dappunq`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish shared domain, league configuration, error handling and persistence primitives required by every user story.

**Critical**: No user story implementation can begin until this phase is complete.

- [X] T005 [P] Create immutable fixed-league configuration for `PL`, `BL1`, `PD`, `SA` and `FL1`, including Spanish display names and countries, in `backend/app/src/main/java/com/dappunq/config/LeagueCatalogProperties.java`
- [X] T006 [P] Create domain `PlayerStatistics` and `Player` value/domain objects with positive external ID, required name/team, nullable statistics and zero-preserving invariants in `backend/app/src/main/java/com/dappunq/model/Player.java` and `backend/app/src/main/java/com/dappunq/model/PlayerStatistics.java`
- [X] T007 [P] Create domain `League` aggregate enforcing non-empty identity and a maximum of ten ordered players in `backend/app/src/main/java/com/dappunq/model/League.java`
- [X] T008 Create external football-data records for competition, scorer, player and team payloads in `backend/app/src/main/java/com/dappunq/dto/footballdata`
- [X] T009 Create public `PlayerResponse` and error response records preserving nullable statistics and external `id` in `backend/app/src/main/java/com/dappunq/dto/PlayerResponse.java` and `backend/app/src/main/java/com/dappunq/dto/ErrorResponse.java`
- [X] T010 [P] Create `LeagueEntity` and `PlayerEntity` JPA mappings, including ordered one-to-many relationship, orphan removal, nullable statistics and player ID as non-generated primary key, in `backend/app/src/main/java/com/dappunq/persistence`
- [X] T011 [P] Create Spring Data repositories with ordered league/player queries and lookup by player ID in `backend/app/src/main/java/com/dappunq/persistence/LeagueRepository.java` and `backend/app/src/main/java/com/dappunq/persistence/PlayerRepository.java`
- [X] T012 Implement domain, persistence and external mapping utilities with explicit filtering of invalid records and preservation of null versus zero in `backend/app/src/main/java/com/dappunq/service/PlayerMapper.java`
- [X] T013 Extend shared exception handling for invalid positive IDs, missing players, external integration failures and unavailable catalog responses in `backend/app/src/main/java/com/dappunq/exception/GlobalExceptionHandler.java`

**Checkpoint**: Domain, persistence mappings, public DTOs, configuration and error responses are ready for story implementation.

---

## Phase 3: User Story 1 - Consultar el catálogo completo por liga (Priority: P1) 🎯 MVP

**Goal**: Expose a stable public snapshot containing the five configured leagues, up to ten valid players per league, and the requested fields in a fixed order.

**Independent Test**: Seed PostgreSQL with the five configured leagues and players, call `GET /players` without JWT, and verify exactly five ordered lists, no more than ten players per list, correct league membership and all response fields.

### Tests for User Story 1

- [ ] T014 [P] [US1] Add unit tests for external payload mapping, invalid player filtering, ten-player truncation and null-versus-zero preservation in `backend/app/src/test/java/com/dappunq/unit/PlayerMapperTest.java`
- [ ] T015 [P] [US1] Add MockMvc contract tests for public `GET /players`, fixed five-list ordering, empty leagues and response field shape in `backend/app/src/test/java/com/dappunq/e2e/PlayerCatalogE2ETest.java`
- [ ] T016 [P] [US1] Add PostgreSQL Testcontainers tests for league/player persistence and ordered reads in `backend/app/src/test/java/com/dappunq/integration/PlayerCatalogPersistenceIntegrationTest.java`

### Implementation for User Story 1

- [ ] T017 Implement `RestClient` football-data client with HTTPS base URL, finite timeouts, configured `X-Auth-Token`, `/competitions/{code}/scorers` requests and explicit non-2xx/deserialization failures in `backend/app/src/main/java/com/dappunq/service/FootballDataClient.java`
- [ ] T018 Implement snapshot refresh service that processes all five leagues, discards invalid records, limits valid players to ten and atomically replaces only successful non-empty league snapshots in `backend/app/src/main/java/com/dappunq/service/PlayerCatalogRefreshService.java`
- [ ] T019 Implement PostgreSQL-only read service returning all five leagues in fixed order and mapping entities to `PlayerResponse` without invoking the external client in `backend/app/src/main/java/com/dappunq/service/PlayerCatalogReadService.java`
- [ ] T020 Implement public `GET /players` controller with response serialization and catalog-unavailable error mapping in `backend/app/src/main/java/com/dappunq/controller/PlayerControllerRest.java`
- [ ] T021 Implement `ApplicationRunner` startup synchronization with per-league failure logging, snapshot preservation and no credential values in logs in `backend/app/src/main/java/com/dappunq/service/PlayerCatalogStartupRunner.java`
- [ ] T022 [P] [US1] Update the Postman collection with a public `GET /players` request and representative five-list response expectations in `docs/postman/UserAuth.postman_collection.json`

**Checkpoint**: `GET /players` is a complete independently testable MVP and startup refresh does not make the endpoint depend on football-data availability.

---

## Phase 4: User Story 2 - Expandir y contraer tablas (Priority: P1)

**Goal**: Preserve an independently identifiable, stable league grouping contract so the consumer can render five independent expandable sections, all initially expanded.

**Independent Test**: Verify the `GET /players` response always contains the five leagues in fixed order, including empty lists, so a consumer can toggle one league without changing any other league's data or identity.

### Tests for User Story 2

- [ ] T023 [P] [US2] Add MockMvc assertions that every successful catalog response contains all five fixed league positions, including empty arrays, in `backend/app/src/test/java/com/dappunq/e2e/PlayerCatalogGroupingE2ETest.java`

### Implementation for User Story 2

- [ ] T024 [US2] Keep league ordering and empty-list representation explicit in the read service and controller contract in `backend/app/src/main/java/com/dappunq/service/PlayerCatalogReadService.java` and `backend/app/src/main/java/com/dappunq/controller/PlayerControllerRest.java`
- [ ] T025 [US2] Document five independent initially-expanded sections, league-name toggle behavior and accessibility state requirements in `specs/003-player-catalog/contracts/players-api.yaml`

**Checkpoint**: The backend contract supplies stable independent league sections; frontend-only expand/collapse state remains outside the planned backend scope.

---

## Phase 5: User Story 3 - Interpretar datos incompletos (Priority: P2)

**Goal**: Return incomplete player statistics as JSON nulls while preserving zero, required identity fields, row alignment and clear unavailable-catalog errors for the consumer.

**Independent Test**: Persist players with null, empty/invalid and zero values, call both public endpoints, and verify null-valued fields remain distinguishable from numeric zero while invalid identities are excluded.

### Tests for User Story 3

- [ ] T026 [P] [US3] Add unit tests for null, empty and zero statistic mapping and required name/team rejection in `backend/app/src/test/java/com/dappunq/unit/PlayerIncompleteDataTest.java`
- [ ] T027 [P] [US3] Add MockMvc tests for null-versus-zero JSON output and a comprehensible unavailable-catalog error in `backend/app/src/test/java/com/dappunq/e2e/PlayerIncompleteDataE2ETest.java`

### Implementation for User Story 3

- [ ] T028 [US3] Normalize blank/uninterpretable external values to nullable domain fields while retaining integer zero in `backend/app/src/main/java/com/dappunq/service/PlayerMapper.java`
- [ ] T029 [US3] Ensure `PlayerResponse` JSON includes nullable statistics and required identity fields without substituting display dashes in the backend in `backend/app/src/main/java/com/dappunq/dto/PlayerResponse.java`
- [ ] T030 [US3] Add catalog failure response with Spanish message and no partial-success payload in `backend/app/src/main/java/com/dappunq/exception/GlobalExceptionHandler.java`

**Checkpoint**: Consumers can render `null` as `-` and numeric zero as `0` without ambiguity or malformed rows.

---

## Phase 6: User Story 4 - Consultar un jugador por identificador (Priority: P2)

**Goal**: Provide a public PostgreSQL-backed lookup by the football-data `player.id`, returning 404 for absent records and never falling back to the external API.

**Independent Test**: Persist a player with external ID, call `GET /players/{id}` without JWT, verify the exact ID and fields, then call an unknown or invalid ID and verify the documented error status.

### Tests for User Story 4

- [ ] T031 [P] [US4] Add MockMvc tests for public `GET /players/{id}`, exact external ID propagation, unknown ID 404 and invalid path IDs in `backend/app/src/test/java/com/dappunq/e2e/PlayerByIdE2ETest.java`
- [ ] T032 [P] [US4] Add integration tests for repository lookup by non-generated player primary key and no external-client fallback in `backend/app/src/test/java/com/dappunq/integration/PlayerByIdIntegrationTest.java`

### Implementation for User Story 4

- [ ] T033 [US4] Implement PostgreSQL-only player lookup by positive integer ID and explicit not-found exception in `backend/app/src/main/java/com/dappunq/service/PlayerCatalogReadService.java`
- [ ] T034 [US4] Add `GET /players/{id}` controller route with positive-ID validation, public access and `PlayerResponse` serialization in `backend/app/src/main/java/com/dappunq/controller/PlayerControllerRest.java`
- [ ] T035 [US4] Map missing players to HTTP 404 and invalid identifiers to the existing Spanish error response in `backend/app/src/main/java/com/dappunq/exception/GlobalExceptionHandler.java`
- [ ] T036 [P] [US4] Add a public `GET /players/{id}` request without Authorization headers to `docs/postman/UserAuth.postman_collection.json`
- [ ] T037 [P] [US4] Extend the OpenAPI contract with positive integer validation, 404 response and exact `id` semantics for `GET /players/{id}` in `specs/003-player-catalog/contracts/players-api.yaml`

**Checkpoint**: Both public endpoints use the persisted external identity and remain independent from football-data after startup.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Complete quality gates, documentation and delivery validation across all stories.

- [ ] T038 [P] Verify `GET /players` and `GET /players/{id}` remain permitted without JWT while the football-data token is restricted to internal client configuration in `backend/app/src/main/java/com/dappunq/security/SecurityConfig.java`
- [ ] T039 [P] Add startup refresh failure, empty-response preservation, non-empty replacement and obsolete-player deletion coverage in `backend/app/src/test/java/com/dappunq/integration/PlayerCatalogRefreshIntegrationTest.java`
- [ ] T040 [P] Update backend API and environment-variable usage documentation, excluding credentials and tokens, in `backend/README.md`
- [ ] T041 Run the feature quickstart and backend test suite from `backend/gradlew.bat` and record any required local PostgreSQL/Testcontainers setup in `specs/003-player-catalog/quickstart.md`
- [ ] T042 Review all feature code, tests and documentation for Spanish messages, layer separation, no generated player IDs, no secret literals and constitution compliance in `backend/` and `specs/003-player-catalog/`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; T001-T004 can run in parallel except any build-file coordination.
- **Foundational (Phase 2)**: Depends on Setup; T005-T013 block all user-story work.
- **User Story 1 (Phase 3)**: Depends on Phase 2 and is the MVP.
- **User Story 2 (Phase 4)**: Depends on the shared read contract from Phase 2; T023-T025 can proceed after the contract exists and do not require frontend source changes.
- **User Story 3 (Phase 5)**: Depends on the mapping and DTOs from Phase 2; it can proceed in parallel with US2 after the foundation.
- **User Story 4 (Phase 6)**: Depends on persistence, DTOs and error handling from Phase 2; it can proceed in parallel with US2 and US3.
- **Polish (Phase 7)**: Depends on all selected user stories being complete.

### User Story Dependencies

- **US1 (P1)**: Depends only on Foundational; delivers the MVP catalog and startup snapshot.
- **US2 (P1)**: Depends only on the fixed five-league read contract; independent from US3 and US4.
- **US3 (P2)**: Depends on the shared mapper/DTO; independent from US2 and US4.
- **US4 (P2)**: Depends on the shared persistence and error primitives; independent from US2 and US3.

### Parallel Opportunities

- T001-T003, T005-T011 and T014-T016 can be split by file/package once interfaces are agreed.
- After Phase 2, US2, US3 and US4 can be implemented in parallel with US1 by separate developers.
- Within US1, unit, MockMvc and persistence tests are parallel; client, refresh service, read service and controller work can proceed once their contracts are defined.
- Within US4, endpoint contract tests, persistence lookup tests, Postman updates and OpenAPI updates are parallel.
- T038-T040 are parallel polish tasks; T041 must run after implementation and test changes.

---

## Parallel Example: User Story 1

```text
Task: T014 PlayerMapper unit tests in backend/app/src/test/java/com/dappunq/unit/PlayerMapperTest.java
Task: T015 GET /players MockMvc tests in backend/app/src/test/java/com/dappunq/e2e/PlayerCatalogE2ETest.java
Task: T016 PostgreSQL persistence tests in backend/app/src/test/java/com/dappunq/integration/PlayerCatalogPersistenceIntegrationTest.java

Task: T017 FootballDataClient in backend/app/src/main/java/com/dappunq/service/FootballDataClient.java
Task: T019 PlayerCatalogReadService in backend/app/src/main/java/com/dappunq/service/PlayerCatalogReadService.java
Task: T022 Postman GET /players request in docs/postman/UserAuth.postman_collection.json
```

## Parallel Example: User Stories 2-4

```text
Task: T023 Stable five-league grouping test for US2
Task: T026 Incomplete-data unit tests for US3
Task: T031 GET /players/{id} MockMvc tests for US4
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 setup.
2. Complete Phase 2 foundational work.
3. Complete Phase 3 US1, including startup refresh, PostgreSQL read path, public `GET /players` and required tests.
4. Stop and validate the five-list catalog independently with `backend/gradlew.bat :app:test`.
5. Deploy/demo the catalog snapshot before adding individual lookup and cross-cutting refinements.

### Incremental Delivery

1. Add US2's stable grouping contract and accessibility documentation.
2. Add US3's null/zero behavior and unavailable-catalog handling.
3. Add US4's public lookup by persisted external ID.
4. Complete Phase 7 quality, security and quickstart validation.

### Definition of Done

The feature is complete when all required story tests pass, PostgreSQL/Testcontainers refresh and reads work, both endpoints compile and run publicly without JWT, Postman includes both requests, no credential is versioned, and every task below follows the checklist format.

## Notes

- `[P]` marks tasks that can run in parallel without sharing incomplete-file dependencies.
- `[US1]` through `[US4]` map directly to the prioritized stories and endpoint increment.
- Backend responses preserve `null`; the consumer renders unavailable values as `-` and zero as `0`.
- The frontend is intentionally not modified because `plan.md` limits this feature to `backend/`.
