# Tasks: User Domain and Persistence Separation

**Input**: Design documents from `/specs/007-user-refactor/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Included because the feature specification defines user-story acceptance tests and the quickstart workflow requires focused unit, integration, and MockMvc verification.

**Organization**: Tasks are grouped by user story so each story can be implemented and tested independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirm the project baseline and prepare the domain/persistence/security package layout.

- [ ] T001 [P] Confirm the governance gate and existing baseline in `backend/.specify/memory/constitution.md`, `backend/specs/007-user-refactor/plan.md`, and `backend/specs/007-user-refactor/spec.md`
- [ ] T002 [P] Verify the package structure for `backend/app/src/main/java/com/dappunq/model/`, `backend/app/src/main/java/com/dappunq/persistence/`, `backend/app/src/main/java/com/dappunq/security/`, `backend/app/src/main/java/com/dappunq/service/`, and `backend/app/src/main/java/com/dappunq/controller/`
- [ ] T003 [P] Review the current compatibility baseline in `backend/app/src/main/java/com/dappunq/model/User.java`, `backend/app/src/main/java/com/dappunq/persistence/UserRepository.java`, `backend/app/src/main/java/com/dappunq/service/AuthService.java`, and `backend/app/src/main/java/com/dappunq/controller/AuthControllerRest.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Build the persistence and security boundary that all user stories depend on.

**Checkpoint**: The domain model is separated from persistence and security concerns, and the adapter boundary is ready for story-specific implementation.

- [ ] T004 Create `backend/app/src/main/java/com/dappunq/persistence/UserEntity.java` with the existing `usuarios` table mapping, generated ID, `nombre`, `password_hash`, and `created_at` metadata, and the persistence lifecycle behavior
- [ ] T005 [P] Implement `backend/app/src/main/java/com/dappunq/persistence/UserMapper.java` to convert between `User` and `UserEntity` without exposing persistence concerns to the domain model
- [ ] T006 [P] Create `backend/app/src/main/java/com/dappunq/persistence/JpaUserEntityRepository.java` and `backend/app/src/main/java/com/dappunq/persistence/UserRepositoryImpl.java` to delegate persistence while keeping the domain-facing repository contract intact
- [ ] T007 Create `backend/app/src/main/java/com/dappunq/security/UserPrincipal.java` to adapt a domain `User` to Spring Security's `UserDetails` contract without leaking framework dependencies into `User`
- [ ] T008 Update `backend/app/src/main/java/com/dappunq/service/UserService.java` and `backend/app/src/main/java/com/dappunq/service/AuthService.java` to work with domain `User` values and preserve the existing authentication/service boundaries

---

## Phase 3: User Story 1 - Enforce User domain rules (Priority: P1) 🎯 MVP

**Goal**: Keep `User` valid as a framework-independent domain object with its own invariants, independent of JPA and Spring Security.

**Independent Test**: Instantiate `User` directly and verify valid names are normalized while blank or null values are rejected without requiring persistence or framework services.

### Tests for User Story 1

- [ ] T009 [P] [US1] Add domain validation coverage in `backend/app/src/test/java/com/dappunq/unit/UserTest.java` for valid names, blank names, blank credentials, and whitespace normalization

### Implementation for User Story 1

- [ ] T010 [P] [US1] Refactor `backend/app/src/main/java/com/dappunq/model/User.java` to remove persistence annotations and `UserDetails` coupling while retaining the domain invariants and normalized `nombre`/credential checks
- [ ] T011 [US1] Add or adjust domain-specific validation and error handling in `backend/app/src/main/java/com/dappunq/model/User.java` and `backend/app/src/main/java/com/dappunq/exception/` so invalid persisted or constructed state is rejected consistently

**Checkpoint**: User Story 1 should produce a domain-valid `User` independent of persistence or security contracts and fail fast on invalid input.

---

## Phase 4: User Story 2 - Store and retrieve users without leaking persistence details (Priority: P1)

**Goal**: Keep persistence metadata on `UserEntity` while services and controllers exchange only domain `User` data.

**Independent Test**: Save a valid user through the repository boundary, retrieve it back, and confirm the generated ID, normalized name, and hashed credential are preserved without leaking persistence details.

### Tests for User Story 2

- [ ] T012 [P] [US2] Add mapper and repository save/retrieve coverage in `backend/app/src/test/java/com/dappunq/unit/UserMapperTest.java` and `backend/app/src/test/java/com/dappunq/integration/UserRepositoryIntegrationTest.java`

### Implementation for User Story 2

- [ ] T013 [P] [US2] Implement the bidirectional mapping in `backend/app/src/main/java/com/dappunq/persistence/UserMapper.java` to carry identity, normalized name, and encoded credential across `User` and `UserEntity`
- [ ] T014 [US2] Update `backend/app/src/main/java/com/dappunq/persistence/UserRepositoryImpl.java` and `backend/app/src/main/java/com/dappunq/persistence/JpaUserEntityRepository.java` so the repository adapter persists and reloads `UserEntity` while exposing the domain-facing contract to services
- [ ] T015 [US2] Preserve persistence-only metadata and lifecycle behavior in `backend/app/src/main/java/com/dappunq/persistence/UserEntity.java` without exposing it in the domain model or response DTOs

**Checkpoint**: User Story 2 should persist and reload `User` records without coupling the domain to persistence annotations or database lifecycle behavior.

---

## Phase 5: User Story 3 - Preserve existing user and authentication flows (Priority: P1)

**Goal**: Keep registration, login, profile lookup, and JWT behavior unchanged while the domain/persistence split is in place.

**Independent Test**: Run the existing registration, login, and profile scenarios against the new persistence boundary and confirm they return the same user names, errors, and authorization header behavior.

### Tests for User Story 3

- [ ] T016 [P] [US3] Add or update registration/login/profile integration tests in `backend/app/src/test/java/com/dappunq/integration/AuthRegisterIntegrationTest.java`, `backend/app/src/test/java/com/dappunq/integration/AuthLoginIntegrationTest.java`, and `backend/app/src/test/java/com/dappunq/integration/UserProfileIntegrationTest.java`
- [ ] T017 [P] [US3] Validate the end-to-end user flow in `backend/app/src/test/java/com/dappunq/e2e/UserAuthE2ETest.java` using the existing MockMvc contract

### Implementation for User Story 3

- [ ] T018 [US3] Update `backend/app/src/main/java/com/dappunq/service/AuthService.java` to keep name normalization, duplicate detection, password hashing, and credential validation behavior exactly as before while using the new repository boundary
- [ ] T019 [US3] Refactor `backend/app/src/main/java/com/dappunq/service/UserService.java` and `backend/app/src/main/java/com/dappunq/controller/UserControllerRest.java` to return and map domain `User` values to the established response DTOs without leaking persistence entities to controllers
- [ ] T020 [US3] Adapt the security boundaries in `backend/app/src/main/java/com/dappunq/security/UserPrincipal.java`, `backend/app/src/main/java/com/dappunq/security/JwtService.java`, and related security config to keep login and token generation compatible with the current authentication flow
- [ ] T021 [US3] Confirm the compatibility contract in `backend/specs/007-user-refactor/contracts/user-api.yaml` matches the existing external behavior for `/login`, `/register`, and `/users/{id}/`

**Checkpoint**: All user and auth flows should remain externally identical while the internal domain and persistence model is refactored.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Validate the architecture and quality gates across the full refactor.

- [ ] T022 [P] Run the focused domain and mapper checks from `backend/specs/007-user-refactor/quickstart.md` using `backend/gradlew.bat :app:test --tests "com.dappunq.unit.UserTest" --tests "com.dappunq.unit.UserMapperTest"`
- [ ] T023 [P] Run the PostgreSQL-backed integration and MockMvc E2E checks from `backend/specs/007-user-refactor/quickstart.md`, then compile the backend module with `backend/gradlew.bat :app:compileJava` and `backend/gradlew.bat :app:test`
- [ ] T024 [P] Review the final architectural safeguards across `backend/app/src/main/java/com/dappunq/model/User.java`, `backend/app/src/main/java/com/dappunq/persistence/UserEntity.java`, and `backend/app/src/main/java/com/dappunq/security/` to confirm no plaintext password persistence, no entity leakage, and no domain-layer persistence or security coupling

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion and is required before any user story implementation.
- **User Story 1 (Phase 3)**: Depends on the foundational adapter boundary and can start once the domain/persistence split is in place.
- **User Story 2 (Phase 4)**: Depends on Phase 2 and can run alongside US1 if staffed, but it should remain independently testable.
- **User Story 3 (Phase 5)**: Depends on Phase 2 and is the compatibility validation phase for the refactor.
- **Polish (Phase 6)**: Depends on all desired stories being complete.

### User Story Dependencies

- **User Story 1 (US1)**: No dependency on other stories; foundational work is the only blocker.
- **User Story 2 (US2)**: Depends on the shared mapper/repository boundary exposed in Phase 2 and can validate against the domain model independently.
- **User Story 3 (US3)**: Depends on US1 and US2 for the domain and persistence separation, but must keep the external API contract unchanged.

### Parallel Opportunities

- Task pairs marked `[P]` within the same phase can be developed in parallel when different files are involved.
- User Story 1 and User Story 2 can be advanced in parallel after the foundational boundary is complete.
- The registration/login/profile integration tests and E2E validation in User Story 3 can run in parallel once the repository and security adapters are in place.
- Final polish validation tasks can execute in parallel once all stories have passed their focused checks.

---

## Parallel Example: User Story 2

```bash
# Mapper and repository tests can be written in parallel once the boundary exists.
Task: "Add mapper and repository save/retrieve coverage in backend/app/src/test/java/com/dappunq/unit/UserMapperTest.java"
Task: "Add mapper and repository save/retrieve coverage in backend/app/src/test/java/com/dappunq/integration/UserRepositoryIntegrationTest.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 and Phase 2 so the persistence and security adapters are in place without leaking framework concerns into `User`.
2. Refactor `User` to enforce domain invariants and a trimmed nonblank name/credential state.
3. Validate that valid domain creation works independently of persistence and security infrastructure.
4. Extend to the repository mapping and external compatibility flows once the domain model is stable.

### Incremental Delivery

1. Domain separation and mapper/repository boundary.
2. Repository-backed persistence and security adaptation.
3. Service/controller compatibility for registration, login, and profile lookup.
4. Full regression verification and final architecture audit.

### Acceptance Gate

The feature is complete only when the focused unit, integration, and E2E checks from `backend/specs/007-user-refactor/quickstart.md` pass, the public API contract remains unchanged, and the domain model remains independent of persistence and framework-specific contracts.
