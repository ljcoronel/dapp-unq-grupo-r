# Tasks: Catálogo de jugadores por liga

**Input**: Design documents from `/frontend/specs/002-catalogo-jugadores/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/players-api.yaml

**Tests**: No automated frontend tests are configured for this feature; validation is via `npm run lint` and `npm run build` plus the manual quickstart scenarios in `quickstart.md`.

**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirm the existing frontend structure and the API/client contracts required for the feature.

- [X] T001 [P] Confirm the existing React route and Home wiring in `frontend/src/App.jsx` and `frontend/src/pages/HomePage.jsx` before implementing the player catalog
- [X] T002 [P] Validate the shared Axios client in `frontend/src/services/apiClient.js` and confirm `baseURL` remains `http://localhost:8080` for `GET /players`
- [X] T003 [P] Review the auth/session boundary in `frontend/src/context/AuthContext.jsx` to keep the public players request independent from the JWT flow

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Implement the reusable player fetch and table rendering primitives that all user stories depend on.

**Checkpoint**: Foundation ready - the catalog can now render data and present league sections in a reusable way.

- [X] T004 Implement the public players API adapter in `frontend/src/services/playerService.js` using `apiClient.get('/players')` and returning the five league arrays without attaching JWT headers
- [X] T005 [P] Create the reusable league table component in `frontend/src/components/PlayerLeagueTable.jsx` to render a single league section with the required Spanish column labels in the correct order
- [X] T006 [P] Add the initial data-loading state and error/empty handling in `frontend/src/pages/HomePage.jsx` for `GET /players` when the page is mounted

---

## Phase 3: User Story 1 - Consultar jugadores de las ligas (Priority: P1) 🎯 MVP

**Goal**: Mostrar el catálogo completo con las cinco ligas, sus diez jugadores y los encabezados requeridos en español.

**Independent Test**: Abrir Home y verificar que aparecen las cinco ligas solicitadas con diez filas por tabla y los siete encabezados en español.

### Implementation for User Story 1

- [X] T007 [P] [US1] Wire `fetchPlayers` from `frontend/src/services/playerService.js` into `frontend/src/pages/HomePage.jsx` and map the five league arrays to the rendered sections
- [X] T008 [US1] Render the five league sections in `frontend/src/pages/HomePage.jsx` with labels: Premier League (Inglaterra), Bundesliga (Alemania), Primera División (España), Serie A (Italia) y Ligue 1 (Francia)
- [X] T009 [P] [US1] Render the player table structure in `frontend/src/components/PlayerLeagueTable.jsx` with columns Nombre completo, Posición, Equipo, Partidos jugados, Goles, Asistencias y Penales in that specific order
- [X] T010 [US1] Ensure Home preserves the response ordering and does not invent or pad missing players when the data source returns fewer than ten rows per league

**Checkpoint**: At this point, User Story 1 should be fully functional and independently testable.

---

## Phase 4: User Story 2 - Mostrar y ocultar tablas por liga (Priority: P2)

**Goal**: Permitir expandir y contraer cada tabla de liga sin afectar el estado de las otras tablas.

**Independent Test**: Abrir Home, confirmar que las cinco tablas comienzan expandidas y alternar cada encabezado para comprobar que solo cambia la sección asociada.

### Implementation for User Story 2

- [X] T011 [P] [US2] Add per-league expansion state in `frontend/src/pages/HomePage.jsx` with all five sections initially expanded and isolated toggles per league
- [X] T012 [P] [US2] Implement the toggle behavior in `frontend/src/components/PlayerLeagueTable.jsx` so clicking the league name expands or collapses only that table
- [X] T013 [US2] Keep the remainder of the catalog intact while a league is collapsed, leaving the league header visible and actionable in `frontend/src/pages/HomePage.jsx`

**Checkpoint**: At this point, User Stories 1 and 2 work independently and can be validated together.

---

## Phase 5: User Story 3 - Interpretar datos incompletos (Priority: P2)

**Goal**: Mostrar valores faltantes como `-` sin perder los datos disponibles ni alterar los ceros válidos.

**Independent Test**: Revisar registros con campos nulos o ausentes y confirmar que cada campo sin dato muestra `-`, mientras que `0` sigue visible como valor real.

### Implementation for User Story 3

- [X] T014 [P] [US3] Add missing-value normalization in `frontend/src/components/PlayerLeagueTable.jsx` so `null`, `undefined`, and missing values render as `-` for `nombre`, `seccion`, `equipo`, `partidosJugados`, `goles`, `asistencias`, and `penaltis`
- [X] T015 [US3] Preserve zero values and other valid data in `frontend/src/pages/HomePage.jsx` and `frontend/src/components/PlayerLeagueTable.jsx` by distinguishing `0` from missing values during rendering
- [X] T016 [US3] Ensure empty league arrays and failure responses are still displayed distinctly in Spanish in `frontend/src/pages/HomePage.jsx` rather than appearing as a successful table state

**Checkpoint**: All user stories are independently functional and the catalog behaves correctly with missing data.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final quality review across the entire catalog feature.

- [X] T017 [P] Review Spanish copy, spacing, and table readability in `frontend/src/pages/HomePage.jsx` and `frontend/src/components/PlayerLeagueTable.jsx`
- [X] T018 [P] Run `npm run lint` in `frontend/` and fix any issues introduced by the catalog implementation
- [X] T019 [P] Run `npm run build` in `frontend/` and resolve output or bundle issues before finishing the feature
- [X] T020 Run the manual validation scenarios from `frontend/specs/002-catalogo-jugadores/quickstart.md` and confirm the behavior matches the feature requirements

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion; blocks all story work.
- **User Story 1 (Phase 3)**: Depends on Foundational completion; this is the MVP story.
- **User Story 2 (Phase 4)**: Depends on User Story 1 completion.
- **User Story 3 (Phase 5)**: Depends on User Story 1 completion.
- **Polish (Phase 6)**: Depends on all desired stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: Can start after the foundational fetch/table tasks are complete.
- **User Story 2 (P2)**: Builds on the initial league rendering from US1 and should be independently testable.
- **User Story 3 (P2)**: Builds on the same data mapping and should validate missing-value behavior independently.

### Parallel Opportunities

- Phase 1 tasks can run in parallel because they touch different existing files and do not change runtime behavior.
- Phase 2 tasks can run in parallel once the API contract is confirmed.
- US1 implementation tasks can be parallelized across service, page, and component responsibilities.
- US2 and US3 can proceed in parallel after US1 baseline rendering is complete, as long as the same HomePage state is coordinated carefully.
- The polish tasks are independent and can be executed once all stories are done.

## Implementation Strategy

### MVP First

1. Complete Setup and Foundational work.
2. Implement User Story 1 only.
3. Stop and validate the five league tables, ordering, and required Spanish labels.
4. Add User Story 2 and User Story 3 incrementally.
5. Finish with polish and lint/build validation.

### Incremental Delivery

1. Build fetch + render baseline.
2. Add expansion/collapse control per league.
3. Add missing-value rendering and state differentiation.
4. Finish with runtime validation and quality checks.

### Suggested MVP Scope

The recommended MVP is User Story 1 only: the page loads the five league groups and renders the complete catalog with all required Spanish labels and columns. User Stories 2 and 3 can be delivered immediately after, since they are additive and independently testable.

## Format validation

- All tasks use the required markdown checkbox format (`- [ ]`).
- Each task has a sequential ID (`T001` ... `T020`).
- Parallelizable tasks include the `[P]` marker.
- Story tasks include the correct `[US1]`, `[US2]`, or `[US3]` label.
- Every task description includes a concrete file path under `frontend/`.
