---

description: "Task list for protecting order creation with JWT authentication"
---

# Tasks: Protección JWT de órdenes

**Input**: Design documents from `backend/specs/015-jwt-order-protection/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/orders-openapi.yaml`, `quickstart.md`

**Tests**: The feature specification defines independent acceptance scenarios and the quickstart requires focused E2E, security, and contract validation.

**Organization**: Tasks are grouped by user story so each increment can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirm the existing backend test/build structure used by this feature.

- [X] T001 Confirm the Gradle test and resource-copy entry points in `backend/app/build.gradle` and record the focused commands from `backend/specs/015-jwt-order-protection/quickstart.md`
- [X] T002 [P] Confirm the existing JWT, order E2E, and OpenAPI source locations under `backend/app/src` and `backend/specs/005-swagger/contracts/openapi.yaml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish the shared authenticated-identity contract before changing order endpoints.

**⚠️ CRITICAL**: No user story implementation can be completed until the authenticated principal and existing JWT filter behavior are understood and preserved.

- [X] T003 [P] Verify `JwtAuthenticationFilter` continues to validate signature, expiration, and user existence in `backend/app/src/main/java/com/dappunq/security/JwtAuthenticationFilter.java` and `backend/app/src/main/java/com/dappunq/security/JwtService.java`
- [X] T004 Verify the order request and persistence invariants used by both order services in `backend/app/src/main/java/com/dappunq/dto/order/CrearOrdenRequest.java`, `backend/app/src/main/java/com/dappunq/service/order/CrearOrdenCompraService.java`, `backend/app/src/main/java/com/dappunq/service/order/CrearOrdenVentaService.java`, and `backend/app/src/main/java/com/dappunq/persistence/order/OrdenEntity.java`

**Checkpoint**: Existing JWT validation and order business rules are identified and ready for route protection without duplicating authentication logic.

---

## Phase 3: User Story 1 - Proteger la creación de órdenes (Priority: P1) 🎯 MVP

**Goal**: Require a valid JWT for `POST /orders/buy` and `POST /orders/sell`, attribute each order to the authenticated user, and preserve existing functional validation and effects for authenticated requests.

**Independent Test**: Using `OrderManagementControllerE2ETest` with PostgreSQL Testcontainers and MockMvc security, verify that valid buy/sell requests retain their current results, missing/malformed/invalid/expired/deleted-user tokens return 401, mismatched `usuarioId` cannot create an order, and rejected requests leave orders, balances, holdings, and reservations unchanged.

### Tests for User Story 1

- [X] T005 [P] [US1] Add JWT token creation and authenticated-request helpers to `backend/app/src/test/java/com/dappunq/e2e/OrderManagementControllerE2ETest.java` using the existing `JwtService` and `UserPrincipal` test context
- [X] T006 [P] [US1] Add buy and sell rejection scenarios for missing, empty, non-Bearer, malformed, invalid, expired, and deleted-user tokens in `backend/app/src/test/java/com/dappunq/e2e/OrderManagementControllerE2ETest.java`
- [X] T007 [P] [US1] Add authenticated buy/sell, principal/request identity mismatch, and functional-validation regression scenarios in `backend/app/src/test/java/com/dappunq/e2e/OrderManagementControllerE2ETest.java`
- [X] T008 [P] [US1] Extend `backend/app/src/test/java/com/dappunq/security/JwtAuthenticationFilterTest.java` to verify the authenticated principal exposes the loaded user identity and invalid bearer tokens leave the security context unauthenticated

### Implementation for User Story 1

- [X] T009 [US1] Expose the persisted user identifier from `UserPrincipal` without adding persistence annotations in `backend/app/src/main/java/com/dappunq/security/UserPrincipal.java`
- [X] T010 [US1] Update `OrderControllerRest` to receive the validated authenticated principal for buy and sell, reject a `CrearOrdenRequest.usuarioId` that differs from the principal ID, and pass only an authenticated, attributed request to the existing order services in `backend/app/src/main/java/com/dappunq/controller/OrderController.java`
- [X] T011 [US1] Change only `POST /orders/buy` and `POST /orders/sell` from public access to `authenticated()` while preserving all other authorization matchers and stateless JWT filter ordering in `backend/app/src/main/java/com/dappunq/security/SecurityConfig.java`
- [X] T012 [US1] Make the shared unauthorized response in `backend/app/src/main/java/com/dappunq/security/SecurityConfig.java` generic for JWT and API-key authentication failures without exposing user, balance, holding, or order existence
- [X] T013 [US1] Run the focused order and JWT tests and correct any integration issues while preserving existing order service validation and transaction behavior in `backend/app/src/test/java/com/dappunq/e2e/OrderManagementControllerE2ETest.java` and `backend/app/src/test/java/com/dappunq/security/JwtAuthenticationFilterTest.java`

**Checkpoint**: User Story 1 is independently functional: only valid JWT requests reach order creation, identity cannot be spoofed through the body, and rejected requests have no business side effects.

---

## Phase 4: User Story 2 - Documentar el requisito de autenticación (Priority: P2)

**Goal**: Publish the JWT bearer requirement and 401 response for both order-creation operations in the canonical OpenAPI contract and served static resource.

**Independent Test**: Inspect both OpenAPI files and verify `bearerAuth` is an HTTP bearer JWT scheme, both POST paths declare `security: [{ bearerAuth: [] }]`, and both document HTTP 401 while retaining request and functional response schemas.

### Tests for User Story 2

- [X] T014 [P] [US2] Add contract assertions for the bearer scheme, buy/sell security requirements, 401 responses, and preserved order schemas in `backend/app/src/test/java/com/dappunq/contract/OrdersOpenApiContractTest.java`

### Implementation for User Story 2

- [X] T015 [US2] Add `/orders/buy` and `/orders/sell` POST operations, bearer security requirements, 401 responses, and order request/response references to the canonical contract in `backend/specs/005-swagger/contracts/openapi.yaml`
- [X] T016 [US2] Run the existing OpenAPI copy task from `backend/app/build.gradle` so `backend/app/src/main/resources/static/openapi.yaml` is regenerated from the canonical contract rather than edited independently
- [X] T017 [US2] Verify the generated resource in `backend/app/src/main/resources/static/openapi.yaml` matches the canonical contract and passes `OrdersOpenApiContractTest.java`

**Checkpoint**: User Story 2 is independently complete: API consumers can identify the Bearer JWT header, protected operations, and 401 behavior from the served contract.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Validate the complete feature and preserve repository-wide behavior.

- [X] T018 [P] Run `.\gradlew.bat :app:compileTestJava` from `backend` and resolve compilation or type-safety issues in changed Java files
- [X] T019 [P] Run `.\gradlew.bat :app:test --tests com.dappunq.e2e.OrderManagementControllerE2ETest --tests com.dappunq.security.JwtAuthenticationFilterTest --tests com.dappunq.contract.OrdersOpenApiContractTest` from `backend` and verify all acceptance scenarios pass
- [X] T020 Run `.\gradlew.bat :app:processResources` from `backend` and verify the generated OpenAPI resource remains synchronized with `backend/specs/005-swagger/contracts/openapi.yaml`
- [X] T021 Run `.\gradlew.bat :app:test` from `backend` to confirm unrelated endpoints and existing behavior remain compatible
- [X] T022 [P] Update feature-linked API descriptions or test comments in `backend/specs/015-jwt-order-protection/quickstart.md` only if implementation details or expected unauthorized wording changed

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; T001 and T002 can run in parallel.
- **Foundational (Phase 2)**: Depends on Setup; T003 and T004 can run in parallel and block story implementation.
- **User Story 1 (Phase 3)**: Depends on Foundational; T005-T008 are test preparation and can run in parallel, then T009-T012 implement the authenticated identity and protected routes, followed by T013.
- **User Story 2 (Phase 4)**: Depends on the existing OpenAPI structure and can proceed after Setup; T014 should be created before T015-T017, while it can run in parallel with User Story 1 after the shared structure is confirmed.
- **Polish (Phase 5)**: Depends on the desired user stories being implemented; T018-T021 are final validation, and T022 is conditional documentation cleanup.

### User Story Dependencies

- **User Story 1 (P1)**: Depends on Phase 2 only; this is the MVP increment.
- **User Story 2 (P2)**: Depends on Phase 1 and the canonical OpenAPI structure; it does not require User Story 1 implementation, but final validation should run after both stories.

### Within Each User Story

- Tests are prepared before implementation and must fail for the missing behavior before the implementation tasks are considered complete.
- Principal identity exposure precedes controller attribution; controller attribution and security route changes precede the focused integration run.
- The canonical OpenAPI contract is updated before copying the generated static resource.

### Parallel Opportunities

- T001-T002 can run in parallel during Setup.
- T003-T004 can run in parallel during Foundational.
- T005-T008 can run in parallel because they update independent test concerns in the existing test files.
- After Foundational, User Story 1 and User Story 2 can be assigned to different developers; T014 can be developed alongside T005-T013.
- T018, T019, and T022 can run in parallel once implementation is complete; T020 follows the resource-copy operation.

---

## Parallel Example: User Story 1

```text
Task: "Add JWT token creation and authenticated-request helpers in backend/app/src/test/java/com/dappunq/e2e/OrderManagementControllerE2ETest.java"
Task: "Add invalid-token and no-side-effect scenarios in backend/app/src/test/java/com/dappunq/e2e/OrderManagementControllerE2ETest.java"
Task: "Extend principal behavior tests in backend/app/src/test/java/com/dappunq/security/JwtAuthenticationFilterTest.java"
```

## Parallel Example: User Story 2

```text
Task: "Add OpenAPI contract assertions in backend/app/src/test/java/com/dappunq/contract/OrdersOpenApiContractTest.java"
Task: "Update canonical order paths and security declarations in backend/specs/005-swagger/contracts/openapi.yaml"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup.
2. Complete Phase 2: Foundational.
3. Implement and validate Phase 3: User Story 1.
4. Run the focused E2E and JWT tests and verify no rejected request changes persistence.
5. Stop for an MVP/demo checkpoint before adding contract documentation.

### Incremental Delivery

1. Deliver authenticated buy/sell behavior as User Story 1.
2. Add canonical and served OpenAPI documentation as User Story 2.
3. Run focused tests, resource synchronization, and then the full `:app:test` suite.

### Parallel Team Strategy

1. Complete Setup and Foundational together.
2. Assign User Story 1 security/order changes to one developer.
3. Assign User Story 2 contract and contract-test changes to another developer.
4. Integrate both stories and complete Phase 5 validation.

## Notes

- Every task uses the required `- [ ] T### [P?] [Story?]` checklist format and names the file or command path it affects.
- `[P]` marks work that can proceed in parallel without depending on incomplete work in another task.
- User Story 1 is independently testable without relying on the OpenAPI update; User Story 2 is independently testable from the canonical and generated contract files.
- The canonical contract at `backend/specs/005-swagger/contracts/openapi.yaml` is the source of truth; `backend/app/src/main/resources/static/openapi.yaml` must be generated through Gradle.
