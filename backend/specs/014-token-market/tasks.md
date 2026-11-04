---

description: "Task list for the token order market feature"
---

# Tasks: Mercado de tokens de jugadores

**Input**: Design documents from `backend/specs/014-token-market/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/orders-api.md, quickstart.md

**Tests**: Unit, integration, and E2E tests are included because the plan and project constitution require coverage of domain invariants, PostgreSQL persistence/locking, and authenticated REST endpoints.

**Organization**: Tasks are grouped by user story so each increment can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Establish the source and test structure required by the order-market implementation.

- [ ] T001 Create the order-market package structure under `backend/app/src/main/java/com/dappunq/model/order/`, `backend/app/src/main/java/com/dappunq/dto/order/`, `backend/app/src/main/java/com/dappunq/persistence/`, `backend/app/src/main/java/com/dappunq/service/`, and `backend/app/src/main/java/com/dappunq/controller/`
- [ ] T002 [P] Create the matching test package structure under `backend/app/src/test/java/com/dappunq/unit/`, `backend/app/src/test/java/com/dappunq/integration/`, and `backend/app/src/test/java/com/dappunq/e2e/` for the market feature
- [ ] T003 [P] Add the `backend/postman/token-market.postman_collection.json` skeleton with authenticated buy, sell, pending-orders, transaction-history, cancellation, and rejection scenarios

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Add shared balances, holdings reservations, persistence primitives, and authenticated error handling before implementing individual order flows.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [ ] T004 Extend `backend/app/src/main/java/com/dappunq/model/User.java` and `backend/app/src/main/java/com/dappunq/persistence/UserEntity.java` with non-negative available/blocked credit balances, default initialization, and operations to reserve, release, debit, and credit funds
- [ ] T005 Extend `backend/app/src/main/java/com/dappunq/persistence/HoldingEntity.java` and its related mapper/service paths with available/blocked token quantities and atomic reserve, release, and transfer operations while preserving existing valuation behavior
- [ ] T006 [P] Define order domain types and shared validation exceptions in `backend/app/src/main/java/com/dappunq/model/order/Order.java`, `OrderType.java`, `OrderStatus.java`, and `OrderDomainException.java`, including money precision, positive integer quantity, inclusive price ranges, and legal state transitions
- [ ] T007 [P] Create order request/response DTOs and bean validation in `backend/app/src/main/java/com/dappunq/dto/order/OrderRequest.java`, `OrderResponse.java`, and `OrderDtoMapper.java`, including ISO-8601 dates and operation/counterparty fields
- [ ] T008 Create `backend/app/src/main/java/com/dappunq/persistence/OrderEntity.java` and `OrderRepository.java` with JPA mappings, decimal precision/scale, status/date invariants, pending/history queries, and pessimistic locking for candidate matching
- [ ] T009 [P] Add `backend/app/src/main/java/com/dappunq/exception/OrderException.java` and update `backend/app/src/main/java/com/dappunq/exception/GlobalExceptionHandler.java` to map validation, ownership, insufficient-balance, conflict, and not-found failures to the existing standard error response
- [ ] T010 Add repository/service transaction test fixtures in `backend/app/src/test/java/com/dappunq/config/TokenMarketTestDataConfig.java` and configure deterministic users, players, balances, holdings, and the existing authentication setup for market tests

**Checkpoint**: Shared balance/holding reservations, order persistence, DTO validation, and error mapping are ready for story implementation.

---

## Phase 3: User Story 1 - Comprar tokens de un jugador (Priority: P1) 🎯 MVP

**Goal**: Allow an authenticated user to create a valid buy order, reserve `price + tolerance`, and settle it atomically against a compatible sell order.

**Independent Test**: Create a buy order with sufficient credits and verify it is pending with the correct reservation; create a compatible sell order and verify one atomic transfer, executed states, settled price, and released excess reservation.

### Tests for User Story 1

- [ ] T011 [P] [US1] Add order-domain tests in `backend/app/src/test/java/com/dappunq/unit/OrderBuyTest.java` for buy ranges, zero tolerance, precision/quantity limits, state transitions, and reservation amount
- [ ] T012 [P] [US1] Add buy/sell settlement integration tests in `backend/app/src/test/java/com/dappunq/integration/BuyOrderIntegrationTest.java` using PostgreSQL/Testcontainers for insufficient credits, compatible matching, incompatible ranges, same-owner rejection, atomic transfer, and concurrent consumption
- [ ] T013 [P] [US1] Add authenticated buy endpoint tests in `backend/app/src/test/java/com/dappunq/e2e/BuyOrderE2ETest.java` covering `POST /orders/buy`, validation errors, pending response, and immediate execution response

### Implementation for User Story 1

- [ ] T014 [US1] Implement buy-order creation, credit reservation, pending-order lookup, and validation orchestration in `backend/app/src/main/java/com/dappunq/service/OrderService.java`
- [ ] T015 [US1] Implement deterministic compatible-order matching and atomic price selection/liquidation in `backend/app/src/main/java/com/dappunq/service/OrderMatchingService.java`, including pessimistic candidate locking and excess-credit release
- [ ] T016 [US1] Implement `POST /orders/buy` in `backend/app/src/main/java/com/dappunq/controller/OrderControllerRest.java` using the authenticated principal instead of a request user id
- [ ] T017 [US1] Wire player/user/holding/order repository lookups and persistence mappers in `backend/app/src/main/java/com/dappunq/persistence/OrderPersistenceMapper.java` and related repository interfaces, preserving transaction boundaries
- [ ] T018 [US1] Complete the buy scenarios and expected pending/executed responses in `backend/postman/token-market.postman_collection.json`

**Checkpoint**: A buyer can create and settle a complete order independently, with no partial transfer or inconsistent lock.

---

## Phase 4: User Story 2 - Vender tokens de un jugador (Priority: P1)

**Goal**: Allow an authenticated holder to create a sell order, reserve tokens, and settle it atomically against a compatible buy order.

**Independent Test**: Create a sell order with enough free tokens and verify the token reservation; create a compatible buy order and verify token/credit transfer, settled metadata, and executed states; reject insufficient free tokens without changing balances.

### Tests for User Story 2

- [ ] T019 [P] [US2] Add sell-domain tests in `backend/app/src/test/java/com/dappunq/unit/OrderSellTest.java` for non-negative money, token quantity, lower sell range, reservation, and legal transitions
- [ ] T020 [P] [US2] Add sell-order persistence and settlement tests in `backend/app/src/test/java/com/dappunq/integration/SellOrderIntegrationTest.java` for insufficient holdings, blocked tokens, compatible/incompatible buyers, same-owner rejection, and rollback on transfer failure
- [ ] T021 [P] [US2] Add authenticated sell endpoint tests in `backend/app/src/test/java/com/dappunq/e2e/SellOrderE2ETest.java` covering `POST /orders/sell`, valid pending output, validation failures, and token-lock behavior

### Implementation for User Story 2

- [ ] T022 [US2] Extend `backend/app/src/main/java/com/dappunq/service/OrderService.java` with sell-order creation, free-token validation, token reservation, and matching invocation
- [ ] T023 [US2] Implement `POST /orders/sell` in `backend/app/src/main/java/com/dappunq/controller/OrderControllerRest.java` with authenticated seller identity and standard validation/error responses
- [ ] T024 [US2] Complete token reservation, token transfer, seller crediting, and rollback-safe persistence paths in `backend/app/src/main/java/com/dappunq/service/OrderMatchingService.java` and `backend/app/src/main/java/com/dappunq/persistence/HoldingEntity.java`
- [ ] T025 [US2] Add sell, settlement, and insufficient-token examples to `backend/postman/token-market.postman_collection.json`

**Checkpoint**: A seller can create and settle a complete order independently, and blocked tokens are always released or transferred exactly once.

---

## Phase 5: User Story 3 - Ejecución prioritaria con el Superusuario (Priority: P1)

**Goal**: Give a new buy order priority access to sufficient free Superuser inventory by creating and immediately executing a zero-tolerance sell order.

**Independent Test**: Seed the Superuser with enough free tokens and create a buy order; verify an executed Superuser sell order and immediate atomic settlement. Repeat with insufficient inventory and verify the buy remains pending.

### Tests for User Story 3

- [ ] T026 [P] [US3] Add Superuser priority unit tests in `backend/app/src/test/java/com/dappunq/unit/SuperuserLiquidityTest.java` for sufficient inventory, insufficient inventory, zero tolerance, and same-owner exclusion
- [ ] T027 [P] [US3] Add Superuser priority integration tests in `backend/app/src/test/java/com/dappunq/integration/SuperuserOrderIntegrationTest.java` for generated sell orders, immediate settlement, inventory reservation, and fallback to ordinary pending matching
- [ ] T028 [P] [US3] Add Superuser priority E2E tests in `backend/app/src/test/java/com/dappunq/e2e/SuperuserOrderE2ETest.java` for authenticated buy responses and resulting order/history visibility

### Implementation for User Story 3

- [ ] T029 [US3] Implement Superuser identification and free-inventory lookup in `backend/app/src/main/java/com/dappunq/service/SuperuserOrderLiquidityService.java` using `SuperuserInitializationService` identity rules
- [ ] T030 [US3] Integrate the priority Superuser sell creation and immediate matching before ordinary sell lookup in `backend/app/src/main/java/com/dappunq/service/OrderService.java` and `backend/app/src/main/java/com/dappunq/service/OrderMatchingService.java`
- [ ] T031 [US3] Ensure generated Superuser orders are persisted with zero tolerance, cannot match another Superuser order, and expose correct buyer/seller metadata through `backend/app/src/main/java/com/dappunq/persistence/OrderEntity.java` and `backend/app/src/main/java/com/dappunq/dto/order/OrderDtoMapper.java`
- [ ] T032 [US3] Add sufficient/insufficient Superuser inventory flows to `backend/postman/token-market.postman_collection.json`

**Checkpoint**: Eligible buys execute against the Superuser before ordinary matching; insufficient inventory leaves the buy pending without phantom orders.

---

## Phase 6: User Story 4 - Gestionar y consultar órdenes (Priority: P2)

**Goal**: Let each user list only their pending orders, cancel their own pending orders with complete lock release, and view their executed history with settlement details.

**Independent Test**: Create pending buy and sell orders, list them, cancel each, and verify released balances; execute an order and verify it appears only in the authenticated user's history. Attempt another user's endpoints and verify rejection.

### Tests for User Story 4

- [ ] T033 [P] [US4] Add cancellation and query service tests in `backend/app/src/test/java/com/dappunq/unit/OrderManagementTest.java` for ownership, pending-only cancellation, full lock release, ordering, and executed-history mapping
- [ ] T034 [P] [US4] Add order-management integration tests in `backend/app/src/test/java/com/dappunq/integration/OrderManagementIntegrationTest.java` for pending/history filters, cancellation persistence, balance release, and unauthorized access
- [ ] T035 [P] [US4] Add authenticated MockMvc tests in `backend/app/src/test/java/com/dappunq/e2e/OrderManagementE2ETest.java` for `GET /users/{id}/orders`, `GET /users/{id}/transactions`, cancellation, and cross-user rejection

### Implementation for User Story 4

- [ ] T036 [US4] Implement ownership-checked cancellation and complete credit/token unlock in `backend/app/src/main/java/com/dappunq/service/OrderService.java`
- [ ] T037 [US4] Implement pending and executed repository queries, descending date ordering, and authorization checks in `backend/app/src/main/java/com/dappunq/service/OrderQueryService.java` and `backend/app/src/main/java/com/dappunq/persistence/OrderRepository.java`
- [ ] T038 [US4] Implement `GET /users/{id}/orders`, `GET /users/{id}/transactions`, and the cancellation route in `backend/app/src/main/java/com/dappunq/controller/OrderControllerRest.java` and `backend/app/src/main/java/com/dappunq/controller/UserControllerRest.java`
- [ ] T039 [US4] Add pending, cancellation, history, and cross-user rejection examples to `backend/postman/token-market.postman_collection.json`

**Checkpoint**: Users can safely manage their own pending orders and inspect complete executed history without seeing another user's orders.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Verify the complete feature, documentation, concurrency behavior, and operational readiness.

- [ ] T040 [P] Update `backend/app/src/main/java/com/dappunq/exception/GlobalExceptionHandler.java` and controller response annotations to document all market error/status responses consistently
- [ ] T041 [P] Add database/index and locking review for `backend/app/src/main/java/com/dappunq/persistence/OrderEntity.java`, `OrderRepository.java`, and balance/holding entities to support short serializable transactions
- [ ] T042 [P] Add complete market invariant coverage to `backend/app/src/test/java/com/dappunq/integration/TokenMarketInvariantIntegrationTest.java` for conservation of credits/tokens, rollback, duplicate concurrent matches, and two-decimal/integer persistence
- [ ] T043 Run the documented scenarios and full backend test suite from `backend/quickstart.md` using `backend/gradlew.bat :app:test`, including Testcontainers when Docker is available
- [ ] T044 Update `backend/specs/014-token-market/quickstart.md` and `backend/postman/token-market.postman_collection.json` with the final endpoint payloads, authentication setup, cancellation route, and observed validation behavior

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; T001-T003 can start immediately.
- **Foundational (Phase 2)**: Depends on Setup; T004-T010 block all user stories.
- **User Stories 1-3 (Phases 3-5)**: Depend on the foundational phase. US1 and US2 are independently testable but share the transaction/matching foundation; US3 depends on the buy flow from US1.
- **User Story 4 (Phase 6)**: Depends on the order model and service from US1/US2 and the executed metadata from US3, while remaining independently testable once those APIs exist.
- **Polish (Phase 7)**: Depends on all desired stories being complete.

### User Story Dependencies

- **US1 (P1)**: Foundational only; MVP starting point.
- **US2 (P1)**: Foundational only for sell creation, but uses the shared settlement engine introduced by US1.
- **US3 (P1)**: Depends on US1 buy creation and US2/settlement holding-transfer paths.
- **US4 (P2)**: Depends on order persistence and services from US1-US3 for complete pending/executed/cancelled views.

### Parallel Opportunities

- T002-T003 can run in parallel after T001 establishes the structure.
- T006-T009 can run in parallel with each other after the shared package structure exists; T004-T005 can proceed independently.
- Within each story, all marked test tasks can run in parallel; implementation tasks touching different files can be split after the domain/repository contracts are agreed.
- US1 and the sell-specific portions of US2 can be staffed in parallel after foundational work, but shared `OrderService.java` and `OrderMatchingService.java` changes must be sequenced or merged carefully.
- T040-T042 are independent polish tasks; T043-T044 follow implementation and test completion.

## Parallel Example: User Story 1

```text
After Phase 2:
Task: "T011 [US1] Order buy domain tests in backend/app/src/test/java/com/dappunq/unit/OrderBuyTest.java"
Task: "T012 [US1] Buy settlement integration tests in backend/app/src/test/java/com/dappunq/integration/BuyOrderIntegrationTest.java"
Task: "T013 [US1] Buy endpoint tests in backend/app/src/test/java/com/dappunq/e2e/BuyOrderE2ETest.java"

After the contracts are agreed:
Task: "T014 [US1] Buy creation and reservation in backend/app/src/main/java/com/dappunq/service/OrderService.java"
Task: "T015 [US1] Matching and liquidation in backend/app/src/main/java/com/dappunq/service/OrderMatchingService.java"
Task: "T016 [US1] POST /orders/buy in backend/app/src/main/java/com/dappunq/controller/OrderControllerRest.java"
```

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 Setup.
2. Complete Phase 2 Foundational; this is the blocking reservation/persistence layer.
3. Complete Phase 3 US1, including its unit, integration, and E2E tests.
4. Stop and validate buy reservation, compatible settlement, rollback, and response behavior independently.
5. Demonstrate the MVP before adding sell, Superuser liquidity, or management queries.

### Incremental Delivery

1. Add US2 to provide the ordinary sell side and complete user-to-user exchange.
2. Add US3 to provide prioritized Superuser liquidity.
3. Add US4 to provide cancellation and pending/history visibility.
4. Complete Phase 7 hardening and run the full quickstart suite.

### Parallel Team Strategy

1. Complete Setup and Foundational together.
2. Assign US1 buy flow, US2 sell flow, and their tests to separate contributors while coordinating shared service files.
3. Assign US3 to the contributor owning settlement and Superuser initialization.
4. Assign US4 and Postman/documentation updates once response contracts stabilize.

## Notes

- Every task uses the required `- [ ] [TaskID] [P?] [Story?]` checklist format and includes an exact repository path.
- `[P]` marks only work that can proceed independently without incomplete-file dependencies.
- Tests must be written to fail for the new behavior before implementation where practical, then run with the existing Gradle/Testcontainers setup.
- Preserve existing authentication, valuation, user, player, and holding behavior while adding explicit reservation state.
