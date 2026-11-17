# Tasks: Mercado de Tokens de Jugadores

**Input**: Design documents from `/frontend/specs/003-mercado-tokens/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: No test framework or explicit TDD requirement is declared. Independent Test criteria are provided in each story phase and quickstart.md.

**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1-US10)
- Every task includes an exact frontend file path

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare dependencies and confirm the existing frontend baseline.

- [ ] T001 Install Recharts from `frontend/` with `pnpm add recharts` and update `frontend/package.json`
- [ ] T002 [P] Review existing authentication, routing, player, and profile implementations in `frontend/src/App.jsx`, `frontend/src/context/AuthContext.jsx`, `frontend/src/services/playerService.js`, and `frontend/src/components/ProfileMenu.jsx`
- [ ] T003 [P] Confirm the feature contracts and shared conventions in `frontend/specs/003-mercado-tokens/contracts/api-services.md` and `frontend/specs/003-mercado-tokens/contracts/component-contracts.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Add the shared JWT, API, routing, layout, and service infrastructure required before any user story can be completed.

**CRITICAL**: No user story work can begin until this phase is complete.

- [ ] T004 Create Base64url JWT payload parsing in `frontend/src/utils/jwt.js`
- [ ] T005 Update session state to expose `authToken`, `userId`, and existing authentication feedback in `frontend/src/context/AuthContext.jsx`
- [ ] T006 Add the JWT request interceptor to `frontend/src/services/apiClient.js`
- [ ] T007 [P] Create `frontend/src/services/orderService.js` for buy, sell, pending-order, transaction, and cancel operations
- [ ] T008 [P] Create `frontend/src/services/strategyService.js` for strategy retrieval and configuration
- [ ] T009 [P] Create `frontend/src/services/holdingService.js` for portfolio holdings
- [ ] T010 [P] Create `frontend/src/services/userService.js` for user profile retrieval
- [ ] T011 [P] Extend valuation methods in `frontend/src/services/playerService.js`
- [ ] T012 Create shared loading, error, and empty-state presentation utilities in `frontend/src/components/FeedbackState.jsx`
- [ ] T013 Configure protected nested routes and `/app` layout routing in `frontend/src/App.jsx`

**Checkpoint**: Shared JWT, API, routing, and feedback foundations are ready.

---

## Phase 3: User Story 1 - Navegar mediante sidebar (Priority: P1) MVP

**Goal**: Keep an accessible permanent sidebar beside routed application content.

**Independent Test**: Authenticate, open every `/app/*` route, and verify that the sidebar remains visible and the active section is highlighted.

- [ ] T014 [P] [US1] Create the permanent responsive sidebar in `frontend/src/components/Sidebar.jsx`
- [ ] T015 [US1] Update `frontend/src/App.jsx` with protected `/app/catalogo`, `/app/comerciar`, `/app/portfolio`, `/app/estrategia`, and `/app/perfil` child routes
- [ ] T016 [US1] Replace the old `/home` redirect behavior with `/app/catalogo` in `frontend/src/App.jsx`
- [ ] T017 [US1] Integrate `ProfileMenu` into `AppLayout` so it remains available across routed sections in `frontend/src/components/AppLayout.jsx`
- [ ] T018 [US1] Add responsive sidebar collapse behavior for viewports from 320px upward in `frontend/src/components/Sidebar.jsx`

**Checkpoint**: User Story 1 is complete: authenticated navigation and the permanent layout work independently.

---

## Phase 4: User Story 2 - Consultar el catálogo unificado (Priority: P1)

**Goal**: Display all players in one sortable and filterable table.

**Independent Test**: Load `/app/catalogo`, verify the nine required columns, filter by league/team/nationality, and sort filtered results in both directions.

- [ ] T019 [P] [US2] Create `frontend/src/pages/CatalogoPage.jsx` to fetch all players and manage loading, error, filters, and sorting
- [ ] T020 [P] [US2] Create league/team/nationality dropdown controls in `frontend/src/components/PlayerFilters.jsx`
- [ ] T021 [P] [US2] Create the unified nine-column sortable table in `frontend/src/components/PlayerTable.jsx`
- [ ] T022 [US2] Normalize the existing player response shape and extract filter options in `frontend/src/pages/CatalogoPage.jsx`
- [ ] T023 [US2] Implement client-side combined filtering and ascending/descending sorting in `frontend/src/pages/CatalogoPage.jsx`
- [ ] T024 [US2] Replace `frontend/src/pages/HomePage.jsx` or remove its grouped-table usage so the catalog route uses `CatalogoPage`
- [ ] T025 [US2] Add loading, error/retry, empty-result, and horizontal-scroll states to `frontend/src/components/PlayerTable.jsx`

**Checkpoint**: User Story 2 is complete: the unified catalog works independently.

---

## Phase 5: User Story 3 - Emitir una orden de compra (Priority: P1)

**Goal**: Let strategy-configured users calculate and submit valid buy orders.

**Independent Test**: Open the buy modal with and without a strategy, select a player, verify price calculations and quantity/tolerance validation, and submit a valid order.

- [ ] T026 [P] [US3] Create the buy modal in `frontend/src/components/BuyModal.jsx`
- [ ] T027 [P] [US3] Create reusable integer, tolerance, and credit validation helpers in `frontend/src/utils/orderValidation.js`
- [ ] T028 [US3] Load the current valuation for the selected player through `frontend/src/services/playerService.js`
- [ ] T029 [US3] Implement quantity 1-100, two-decimal tolerance, credit, and submit validation in `frontend/src/components/BuyModal.jsx`
- [ ] T030 [US3] Display unit price, price without tolerance, and price with tolerance in `frontend/src/components/BuyModal.jsx`
- [ ] T031 [US3] Submit buy orders through `frontend/src/services/orderService.js` and handle success/error feedback in `frontend/src/components/BuyModal.jsx`
- [ ] T032 [US3] Prevent opening purchase when no strategy exists and show an actionable message in `frontend/src/pages/ComerciarPage.jsx`

**Checkpoint**: User Story 3 is complete: valid buy orders can be issued and invalid ones blocked independently.

---

## Phase 6: User Story 4 - Emitir una orden de venta (Priority: P1)

**Goal**: Let strategy-configured users calculate and submit sell orders against available, unblocked holdings.

**Independent Test**: Open the sell modal with holdings and active SELL orders, verify available-token limits and tolerance calculations, and submit a valid order.

- [ ] T033 [P] [US4] Create the sell modal in `frontend/src/components/SellModal.jsx`
- [ ] T034 [US4] Compute available tokens as holdings minus active SELL quantities in `frontend/src/components/SellModal.jsx`
- [ ] T035 [US4] Load the current valuation for the selected player through `frontend/src/services/playerService.js`
- [ ] T036 [US4] Implement quantity, available-token, tolerance, and submit validation in `frontend/src/components/SellModal.jsx`
- [ ] T037 [US4] Display unit price, price without tolerance, and price with total tolerance deduction in `frontend/src/components/SellModal.jsx`
- [ ] T038 [US4] Submit sell orders through `frontend/src/services/orderService.js` and refresh blocked availability in `frontend/src/pages/ComerciarPage.jsx`
- [ ] T039 [US4] Prevent opening sales when no strategy exists and show an actionable message in `frontend/src/pages/ComerciarPage.jsx`

**Checkpoint**: User Story 4 is complete: sell orders respect available-token blocking independently.

---

## Phase 7: User Story 5 - Ver órdenes activas (Priority: P1)

**Goal**: Show active BUY and SELL orders and expose buy/sell actions.

**Independent Test**: Visit `/app/comerciar` with and without pending orders and verify both lists, empty states, and cancel actions.

- [ ] T040 [P] [US5] Create the pending BUY/SELL order list in `frontend/src/components/OrderList.jsx`
- [ ] T041 [US5] Create `frontend/src/pages/ComerciarPage.jsx` to load strategy, holdings, players, and pending orders
- [ ] T042 [US5] Split pending orders into BUY and SELL groups and connect them to `OrderList` in `frontend/src/pages/ComerciarPage.jsx`
- [ ] T043 [US5] Add clear empty-order states and loading/error feedback to `frontend/src/pages/ComerciarPage.jsx`
- [ ] T044 [US5] Add cancel-order behavior through `frontend/src/services/orderService.js` and refresh pending orders in `frontend/src/pages/ComerciarPage.jsx`

**Checkpoint**: User Story 5 is complete: active trading orders are visible and manageable.

---

## Phase 8: User Story 6 - Consultar el portfolio (Priority: P2)

**Goal**: Show holdings and concreted transaction history for the authenticated user.

**Independent Test**: Visit `/app/portfolio` with and without holdings and verify holdings metrics, green/red history, and empty states.

- [ ] T045 [P] [US6] Create `frontend/src/pages/PortfolioPage.jsx` to load holdings, transactions, and players
- [ ] T046 [P] [US6] Create holdings table rendering with joined player names in `frontend/src/components/HoldingList.jsx`
- [ ] T047 [P] [US6] Create buy/sell-coded transaction history in `frontend/src/components/TransactionHistory.jsx`
- [ ] T048 [US6] Display quantity, unit/value fallback, and current total value in `frontend/src/components/HoldingList.jsx`
- [ ] T049 [US6] Display operation type, player, quantity, settled total, and date in `frontend/src/components/TransactionHistory.jsx`
- [ ] T050 [US6] Add portfolio empty, loading, and error states to `frontend/src/pages/PortfolioPage.jsx`

**Checkpoint**: User Story 6 is complete: holdings and transaction history are independently usable.

---

## Phase 9: User Story 7 - Configurar estrategia de valuación (Priority: P2)

**Goal**: Let users view and configure Strategy A or Strategy B with exact weight validation.

**Independent Test**: Visit `/app/estrategia`, create/change a strategy, test sums of 1 and 0.95, and verify current strategy display.

- [ ] T051 [P] [US7] Create `frontend/src/pages/EstrategiaPage.jsx` to load and refresh strategy state
- [ ] T052 [P] [US7] Create current strategy presentation in `frontend/src/components/StrategyDisplay.jsx`
- [ ] T053 [P] [US7] Create the Strategy A/B configuration modal in `frontend/src/components/StrategyForm.jsx`
- [ ] T054 [US7] Implement Strategy A weight inputs and sum-of-1 validation in `frontend/src/components/StrategyForm.jsx`
- [ ] T055 [US7] Implement Strategy B weight inputs and sum-of-0.95 validation in `frontend/src/components/StrategyForm.jsx`
- [ ] T056 [US7] Submit and refresh strategy configuration through `frontend/src/services/strategyService.js`
- [ ] T057 [US7] Add no-strategy, configured-strategy, loading, and error states to `frontend/src/pages/EstrategiaPage.jsx`

**Checkpoint**: User Story 7 is complete: valid strategy configurations are enforced independently.

---

## Phase 10: User Story 8 - Ver perfil de usuario (Priority: P2)

**Goal**: Navigate to a profile view that displays the authenticated user's name.

**Independent Test**: Open the profile menu and verify `/app/perfil` navigation and the displayed username.

- [ ] T058 [P] [US8] Create `frontend/src/pages/ProfilePage.jsx` to load the profile through `userService`
- [ ] T059 [US8] Update `frontend/src/components/ProfileMenu.jsx` to navigate to `/app/perfil` and preserve logout behavior
- [ ] T060 [US8] Add profile loading, error, retry, and null-name states to `frontend/src/pages/ProfilePage.jsx`

**Checkpoint**: User Story 8 is complete: profile navigation and name display work independently.

---

## Phase 11: User Story 10 - Visualizar distribución del portfolio (Priority: P3)

**Goal**: Render a pie chart of portfolio value, grouping holdings beyond ten players into "Otros".

**Independent Test**: Open Portfolio with no holdings, fewer than ten holdings, and more than ten holdings and verify chart display and grouping.

- [ ] T061 [P] [US10] Create `frontend/src/components/PortfolioPieChart.jsx` with Recharts
- [ ] T062 [US10] Join holdings to player names and build pie-chart data in `frontend/src/components/PortfolioPieChart.jsx`
- [ ] T063 [US10] Group the remaining holdings behind "Otros" after the ten largest values in `frontend/src/components/PortfolioPieChart.jsx`
- [ ] T064 [US10] Integrate the chart below holdings/history and hide or explain it for empty portfolios in `frontend/src/pages/PortfolioPage.jsx`

**Checkpoint**: User Story 10 is complete: portfolio distribution is visualized independently.

---

## Phase 12: Polish & Cross-Cutting Concerns

**Purpose**: Finish responsive behavior, consistency, and validation across all completed stories.

- [ ] T065 [P] Audit all new pages and components at 320px, tablet, desktop widths in `frontend/src/components/` and `frontend/src/pages/`
- [ ] T066 [P] Replace remaining technical or raw API errors with Spanish user feedback in `frontend/src/services/` and `frontend/src/pages/`
- [ ] T067 [P] Remove or deprecate the old grouped catalog components and `/home` route references in `frontend/src/pages/HomePage.jsx`, `frontend/src/components/PlayerLeagueTable.jsx`, and `frontend/src/App.jsx`
- [ ] T068 Run a production build and lint pass from `frontend/` with `pnpm build` and `pnpm lint`
- [ ] T069 Execute all scenarios in `frontend/specs/003-mercado-tokens/quickstart.md` and document any backend contract gaps
- [ ] T070 Review final API and route behavior against `frontend/specs/003-mercado-tokens/contracts/api-services.md` and `frontend/specs/003-mercado-tokens/contracts/component-contracts.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- Setup (Phase 1): No dependencies.
- Foundational (Phase 2): Depends on Setup completion and blocks every user story.
- User Stories (Phases 3-11): Depend on Foundational completion.
- Polish (Phase 12): Depends on all requested user stories being complete.

### User Story Dependencies

- User Story 1 (P1): No dependency on another story after Foundational.
- User Story 2 (P1): No dependency on another story.
- User Story 3 (P1): Depends on shared strategy state and order/player services; can proceed after Foundational.
- User Story 4 (P1): Depends on shared holdings, strategy state, and order/player services; can proceed after Foundational.
- User Story 5 (P1): Depends on shared order/player/strategy services; it integrates buy/sell modals but remains testable independently.
- User Story 6 (P2): Depends on shared holdings, transaction, and player services.
- User Story 7 (P2): Depends on shared strategy service and blocks trading actions.
- User Story 8 (P2): Depends on shared JWT `userId` and user service.
- User Story 10 (P3): Depends on holdings/player data from User Story 6.

### Within Each User Story

- Models and data contracts before presentation.
- Shared components before page integration.
- Core implementation before integration or cancellation behavior.
- Complete and independently validate each story before moving to the next priority.

---

## Parallel Opportunities

- T002-T003 can run alongside Setup dependency review.
- T007-T012 API and feedback modules can run in parallel after route/JWT foundations are determined.
- Catalog components T020-T021 can run in parallel before page integration.
- Buy and sell modal work T026-T027 and T033-T034 can run in parallel.
- Portfolio holdings, history, and chart components T046-T047 and T061-T063 can run in parallel after response contracts are confirmed.
- Strategy form/display T052-T053 can run in parallel.
- Profile, strategy, and catalog page work can proceed in separate workstreams after Foundational.

---

## Parallel Example: User Story 3

```text
T026 Create BuyModal in frontend/src/components/BuyModal.jsx
T027 Create order validation helpers in frontend/src/utils/orderValidation.js
T028-T030 Load valuation and implement buy modal calculations
T031-T032 Connect confirmation and no-strategy blocking
```

---

## Parallel Example: User Story 6

```text
T046 Create HoldingList in frontend/src/components/HoldingList.jsx
T047 Create TransactionHistory in frontend/src/components/TransactionHistory.jsx
T045 Integrate PortfolioPage in frontend/src/pages/PortfolioPage.jsx
T061 Create PortfolioPieChart in frontend/src/components/PortfolioPieChart.jsx
```

---

## Implementation Strategy

### MVP First (User Story 1)

1. Complete Phases 1-2.
2. Complete Phase 3: permanent sidebar and nested routes.
3. Stop and validate authenticated navigation independently.
4. Continue with P1 catalog and trading increments.

### Incremental Delivery

1. Foundation first: JWT, API, routes, layout, and services.
2. Add US1 sidebar navigation.
3. Add US2 unified catalog.
4. Add US3-US5 buy, sell, and active orders.
5. Add US6-US8 portfolio, strategy, and profile.
6. Add US10 pie chart.
7. Finish with responsive, lint, build, and quickstart validation.

---

## Notes

- Tasks marked [P] affect different files or independent work units.
- [Story] labels map tasks to their originating user story for traceability.
- No test tasks are generated because tests were not requested explicitly and no testing framework is declared.
- Commit after each task or cohesive logical group.
