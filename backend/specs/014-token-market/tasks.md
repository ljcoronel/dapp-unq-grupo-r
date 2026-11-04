---

description: "Task list for the token order market feature"
---

# Tasks: Mercado de ordenes de tokens

**Input**: Design documents from `backend/specs/014-token-market/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/orders-api.md, quickstart.md

**Tests**: Test tasks are included because the feature specification and project constitution require domain, integration, and MockMvc coverage.

**Organization**: Tasks are grouped by user story so each increment can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Establish the files and dependencies needed by the order-market implementation.

- [ ] T001 Confirm the backend module and package layout for order-market code under `backend/app/src/main/java/com/dappunq/`
- [ ] T002 [P] Add the order-market test package structure under `backend/app/src/test/java/com/dappunq/unit/`, `backend/app/src/test/java/com/dappunq/integration/`, and `backend/app/src/test/java/com/dappunq/e2e/`
- [ ] T003 [P] Verify the existing Gradle dependencies and Testcontainers PostgreSQL configuration in `backend/app/build.gradle` for JUnit 5, Mockito, Spring Boot test, and PostgreSQL integration tests
- [ ] T004 [P] Create the feature Postman collection skeleton at `backend/postman/token-market.postman_collection.json` with the base URL and public-request conventions

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Implement shared domain, persistence, error, and transaction primitives required by every user story.

**Warning**: No user-story work can begin until this phase is complete.

- [ ] T005 [P] Define order operation and lifecycle enums in `backend/app/src/main/java/com/dappunq/model/order/TipoOperacionOrden.java` and `backend/app/src/main/java/com/dappunq/model/order/EstadoOrden.java`
- [ ] T006 [P] Implement money and token quantity invariant helpers as framework-free domain objects in `backend/app/src/main/java/com/dappunq/model/order/DineroOrden.java` and `backend/app/src/main/java/com/dappunq/model/order/CantidadTokens.java`
- [ ] T007 Implement the framework-free `Orden` aggregate with immutable issuance data, pending/executed/cancelled transitions, inclusive price ranges, complete-match validation, and domain-specific errors in `backend/app/src/main/java/com/dappunq/model/order/Orden.java`
- [ ] T008 [P] Add order-domain exceptions following the backend error convention in `backend/app/src/main/java/com/dappunq/model/order/exception/`
- [ ] T009 Define the JPA order entity and monetary/quantity column precision in `backend/app/src/main/java/com/dappunq/persistence/order/OrdenEntity.java`
- [ ] T010 [P] Define order repository interfaces and Spring Data locking queries for pending opposite orders and owner-filtered history in `backend/app/src/main/java/com/dappunq/persistence/order/OrdenRepository.java`
- [ ] T011 Implement the repository mapper between `Orden` and `OrdenEntity` without placing persistence annotations or mapping logic in the Service layer in `backend/app/src/main/java/com/dappunq/persistence/order/OrdenRepositoryImpl.java`
- [ ] T012 Add shared DTO validation and order response mapping primitives, including ISO-8601 dates and two-decimal monetary serialization, in `backend/app/src/main/java/com/dappunq/dto/order/`
- [ ] T013 Add transaction-safe balance and holding reservation/transfer ports or adapters that reuse the existing User, Player, Holding, and Strategy services in `backend/app/src/main/java/com/dappunq/service/order/OrderAssetService.java`
- [ ] T014 Add the standard order error-to-HTTP response mapping without exposing persistence entities in `backend/app/src/main/java/com/dappunq/controller/OrderExceptionHandler.java`

**Checkpoint**: Domain invariants, repository mapping, existing Strategy/Holding integration points, and transaction/error primitives are ready for story implementation.

---

## Phase 3: User Story 1 - Comprar tokens de un jugador (Priority: P1) 🎯 MVP

**Goal**: Create buy orders, reserve credits, match compatible sells atomically, and add tokens using the buyer's valuation strategy.

**Independent Test**: Create a valid buy order with sufficient credits and an active strategy, verify the pending reservation, then create a compatible sell and verify one atomic execution, credit/token transfer, order states, and holding valuation.

### Tests for User Story 1

- [ ] T015 [P] [US1] Test `Orden` buy ranges, inclusive tolerance, maximum quantity, and execution transitions in `backend/app/src/test/java/com/dappunq/unit/order/OrdenCompraTest.java`
- [ ] T016 [P] [US1] Test buy validation for missing strategy, insufficient available credits, invalid money precision, and quantities above 100 in `backend/app/src/test/java/com/dappunq/integration/order/CompraOrdenIntegrationTest.java`
- [ ] T017 [P] [US1] Add MockMvc coverage for `POST /orders/buy`, including pending response and validation errors, in `backend/app/src/test/java/com/dappunq/e2e/OrderBuyControllerE2ETest.java`

### Implementation for User Story 1

- [ ] T018 [P] [US1] Create buy request and response DTOs with Jakarta validation for user, player, integer quantity, non-negative price, non-negative tolerance, and maximum two decimals in `backend/app/src/main/java/com/dappunq/dto/order/CrearOrdenRequest.java` and `backend/app/src/main/java/com/dappunq/dto/order/OrdenResponse.java`
- [ ] T019 [US1] Implement buy-order creation, active-strategy validation, credit reservation of `price + tolerance`, and pending persistence in `backend/app/src/main/java/com/dappunq/service/order/CrearOrdenCompraService.java`
- [ ] T020 [US1] Implement compatible pending-sell lookup and deterministic inclusive-range price selection in `backend/app/src/main/java/com/dappunq/service/order/OrderMatchingService.java`
- [ ] T021 [US1] Implement atomic buy settlement, including buyer debit, seller credit, reservation surplus release, order execution metadata, and rollback-safe holding update in `backend/app/src/main/java/com/dappunq/service/order/OrderSettlementService.java`
- [ ] T022 [US1] Implement holding receipt logic that preserves an existing unit value or calculates a new holding value through the active Strategy, never from the settlement price, in `backend/app/src/main/java/com/dappunq/service/order/OrderHoldingService.java`
- [ ] T023 [US1] Add the public `POST /orders/buy` endpoint and map Service domain models to response DTOs in `backend/app/src/main/java/com/dappunq/controller/OrderController.java`
- [ ] T024 [US1] Add buy-order examples and pending/executed assertions to `backend/postman/token-market.postman_collection.json`

**Checkpoint**: A buyer can create a valid pending order or complete a compatible purchase with correct reservations, settlement, and holding valuation.

---

## Phase 4: User Story 2 - Vender tokens de un jugador (Priority: P1)

**Goal**: Create sell orders, reserve free tokens, and settle compatible trades without partial executions or self-trades.

**Independent Test**: Create a valid sell order with enough free tokens and an active strategy, verify the token reservation, then create a compatible buy and verify settlement and both executed orders.

### Tests for User Story 2

- [ ] T025 [P] [US2] Test `Orden` sell ranges, lower tolerance bounds, pending cancellation eligibility, and invalid transitions in `backend/app/src/test/java/com/dappunq/unit/order/OrdenVentaTest.java`
- [ ] T026 [P] [US2] Test sell validation for missing strategy, insufficient free tokens, already-blocked tokens, invalid quantity, and invalid money precision in `backend/app/src/test/java/com/dappunq/integration/order/VentaOrdenIntegrationTest.java`
- [ ] T027 [P] [US2] Add MockMvc coverage for `POST /orders/sell`, including pending response and validation errors, in `backend/app/src/test/java/com/dappunq/e2e/OrderSellControllerE2ETest.java`
- [ ] T028 [P] [US2] Add PostgreSQL concurrency coverage proving only one complete matching settlement consumes compatible orders in `backend/app/src/test/java/com/dappunq/integration/order/OrderMatchingConcurrencyTest.java`

### Implementation for User Story 2

- [ ] T029 [US2] Implement sell-order creation, active-strategy validation, free-token reservation, and pending persistence in `backend/app/src/main/java/com/dappunq/service/order/CrearOrdenVentaService.java`
- [ ] T030 [US2] Extend matching and settlement to reject same-owner buy/sell pairs, lock selected orders, transfer tokens and credits atomically, and preserve the no-partial-execution invariant in `backend/app/src/main/java/com/dappunq/service/order/OrderMatchingService.java` and `backend/app/src/main/java/com/dappunq/service/order/OrderSettlementService.java`
- [ ] T031 [US2] Add the public `POST /orders/sell` endpoint using the shared request/response contract in `backend/app/src/main/java/com/dappunq/controller/OrderController.java`
- [ ] T032 [US2] Add sell-order examples and incompatible-range/self-trade assertions to `backend/postman/token-market.postman_collection.json`

**Checkpoint**: Buyers and sellers can trade complete orders with explicit token/credit locks, atomic transfers, and no self-matching.

---

## Phase 5: User Story 3 - Ejecucion prioritaria con el Superusuario (Priority: P1)

**Goal**: Give valid user buys priority access to Superuser inventory while keeping insufficient inventory pending and preventing Superuser self-trades.

**Independent Test**: Seed enough free Superuser tokens and create a buy to verify immediate creation/execution of a zero-tolerance Superuser sell; repeat with insufficient inventory to verify no sell is created and the buy remains pending.

### Tests for User Story 3

- [ ] T033 [P] [US3] Test immediate Superuser matching, zero tolerance, inclusive limits, insufficient inventory fallback, and Superuser self-trade rejection in `backend/app/src/test/java/com/dappunq/integration/order/SuperuserLiquidityIntegrationTest.java`
- [ ] T034 [P] [US3] Add MockMvc coverage for priority execution and pending fallback in `backend/app/src/test/java/com/dappunq/e2e/SuperuserOrderE2ETest.java`

### Implementation for User Story 3

- [ ] T035 [US3] Implement Superuser identity resolution and free-token availability lookup using existing identity rules in `backend/app/src/main/java/com/dappunq/service/order/SuperuserLiquidityService.java`
- [ ] T036 [US3] Implement creation of an internal Superuser sell order with zero tolerance and immediate matching before ordinary sell lookup in `backend/app/src/main/java/com/dappunq/service/order/SuperuserLiquidityService.java`
- [ ] T037 [US3] Integrate the priority path into the transactional buy workflow, ensuring failed priority creation rolls back without leaving a sale or inconsistent locks in `backend/app/src/main/java/com/dappunq/service/order/CrearOrdenCompraService.java`
- [ ] T038 [US3] Add priority-liquidity requests and expected immediate/pending responses to `backend/postman/token-market.postman_collection.json`

**Checkpoint**: Superuser liquidity is consumed first when available, otherwise ordinary matching remains available and all self-trades are rejected.

---

## Phase 6: User Story 4 - Gestionar y consultar ordenes (Priority: P2)

**Goal**: Let each user list only their pending orders, cancel their own pending orders with full lock release, and inspect executed history.

**Independent Test**: Create pending buy and sell orders, list them, cancel each own order and verify released locks, then execute a trade and verify only that user's executed history is returned; attempts against another user's order are rejected.

### Tests for User Story 4

- [ ] T039 [P] [US4] Test pending cancellation transitions, owner checks, executed/cancelled rejection, and complete credit/token lock release in `backend/app/src/main/java/com/dappunq/model/order/Orden.java` via `backend/app/src/test/java/com/dappunq/unit/order/OrdenCancelacionTest.java`
- [ ] T040 [P] [US4] Test owner-filtered pending and executed repository queries, ordering, cancellation persistence, and released balances in `backend/app/src/test/java/com/dappunq/integration/order/OrderQueryCancellationIntegrationTest.java`
- [ ] T041 [P] [US4] Add MockMvc coverage for `GET /users/{id}/orders`, `GET /users/{id}/transactions`, and the owner-only cancellation endpoint in `backend/app/src/test/java/com/dappunq/e2e/OrderManagementControllerE2ETest.java`

### Implementation for User Story 4

- [ ] T042 [US4] Add pending-order and executed-transaction response DTOs with state-specific date and settlement fields in `backend/app/src/main/java/com/dappunq/dto/order/OrdenPendienteResponse.java` and `backend/app/src/main/java/com/dappunq/dto/order/TransaccionEjecutadaResponse.java`
- [ ] T043 [US4] Implement owner-filtered pending and executed order queries sorted by issuance/execution date descending in `backend/app/src/main/java/com/dappunq/service/order/ConsultarOrdenesService.java`
- [ ] T044 [US4] Implement owner-checked cancellation of pending orders and full credit/token lock release in a transaction in `backend/app/src/main/java/com/dappunq/service/order/CancelarOrdenService.java`
- [ ] T045 [US4] Add `GET /users/{id}/orders`, `GET /users/{id}/transactions`, and `DELETE /orders/{orderId}?usuarioId={id}` endpoints with strict owner filtering in `backend/app/src/main/java/com/dappunq/controller/OrderController.java`
- [ ] T046 [US4] Update the HTTP contract to document cancellation and the complete response shapes in `backend/specs/014-token-market/contracts/orders-api.md`
- [ ] T047 [US4] Add pending-list, history, cancellation, ownership, and lock-release scenarios to `backend/postman/token-market.postman_collection.json`

**Checkpoint**: Users can safely manage their own pending orders and inspect their own executed history without seeing or changing another user's orders.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Complete quality gates, documentation, and end-to-end validation across all stories.

- [ ] T048 [P] Add Spanish validation and domain error messages for all order and settlement failure paths in `backend/app/src/main/java/com/dappunq/model/order/exception/` and `backend/app/src/main/java/com/dappunq/controller/OrderExceptionHandler.java`
- [ ] T049 [P] Add database indexes and uniqueness/locking constraints for owner, state, player, quantity, and execution lookup in `backend/app/src/main/resources/db/migration/`
- [ ] T050 [P] Review order entities and mappers for separation of domain models from JPA annotations in `backend/app/src/main/java/com/dappunq/model/order/` and `backend/app/src/main/java/com/dappunq/persistence/order/`
- [ ] T051 Run the full backend test suite, including PostgreSQL Testcontainers and MockMvc, with `backend/gradlew.bat :app:test`
- [ ] T052 Run the backend application startup smoke check and validate all quickstart scenarios from `backend/specs/014-token-market/quickstart.md`
- [ ] T053 Verify all four public API flows and cancellation in `backend/postman/token-market.postman_collection.json`, including no JWT/API key requirement and ISO-8601 responses

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; T001-T004 can start immediately.
- **Foundational (Phase 2)**: Depends on Setup; T005-T014 block all user stories.
- **User Story 1 (Phase 3)**: Depends on Foundational; delivers the MVP buy and settlement path.
- **User Story 2 (Phase 4)**: Depends on Foundational and reuses the matching/settlement primitives from US1; it should not start until T020-T022 are available.
- **User Story 3 (Phase 5)**: Depends on US1 buy creation and settlement (T019-T023); it extends the buy path with priority liquidity.
- **User Story 4 (Phase 6)**: Depends on the order persistence and lifecycle from US1/US2 and the executed state from settlement; it can begin after T021 and T030.
- **Polish (Phase 7)**: Depends on all desired stories being complete.

### User Story Dependencies

- **US1 (P1)**: Foundational only; no dependency on another user story.
- **US2 (P1)**: Foundational plus shared matching/settlement implementation from US1; independently validates selling and a complete trade.
- **US3 (P1)**: US1 buy workflow and US2 settlement primitives; independently validates Superuser liquidity behavior.
- **US4 (P2)**: Shared order lifecycle and settlement behavior from US1/US2; independently validates ownership, cancellation, and queries.

### Parallel Opportunities

- T002-T004 can run in parallel after the module layout is confirmed.
- T005-T006, T008, T010, T012, and T014 can run in parallel before dependent foundational implementation.
- Within US1, T015-T018 can run in parallel; T020 and T022 can proceed in parallel once T007/T013 are ready.
- Within US2, T025-T028 can run in parallel; T029 and T032 can proceed in parallel after shared primitives are available.
- Within US3, T033-T034 can run in parallel, followed by T035-T036.
- Within US4, T039-T042 can run in parallel; T046-T047 can run in parallel with controller implementation.
- T048-T050 can run in parallel after the story implementations stabilize.
- After Foundational, separate developers can work on US1 and the independent sell/query test preparation, while respecting the shared matching files.

## Parallel Example: User Story 1

```text
Task: "Test Orden buy ranges in backend/app/src/test/java/com/dappunq/unit/order/OrdenCompraTest.java"
Task: "Test buy validation in backend/app/src/test/java/com/dappunq/integration/order/CompraOrdenIntegrationTest.java"
Task: "Add MockMvc POST /orders/buy coverage in backend/app/src/test/java/com/dappunq/e2e/OrderBuyControllerE2ETest.java"
Task: "Create buy DTOs in backend/app/src/main/java/com/dappunq/dto/order/CrearOrdenRequest.java and backend/app/src/main/java/com/dappunq/dto/order/OrdenResponse.java"
```

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 Setup.
2. Complete Phase 2 Foundational.
3. Complete Phase 3 User Story 1.
4. Run the independent US1 tests and validate pending reservations plus a compatible executed trade.
5. Demonstrate the buy endpoint before adding Superuser priority, cancellation, and query enhancements.

### Incremental Delivery

1. Add US2 to complete ordinary user selling and atomic two-sided settlement.
2. Add US3 to provide prioritized Superuser liquidity.
3. Add US4 to provide cancellation and user-scoped visibility.
4. Complete Phase 7 quality gates and Postman/quickstart validation.

## Notes

- Every task uses the required `- [ ] T###` checklist format and includes a concrete file path.
- `[P]` marks tasks that can usefully run in parallel without editing the same incomplete dependency.
- Story labels map to the four prioritized stories in `spec.md`.
- Tests must remain in their designated unit, integration, or e2e packages and existing tests must not be modified or removed.
