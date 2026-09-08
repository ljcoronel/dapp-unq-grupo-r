# Tasks: Autenticacion y perfil de usuario

**Input**: Design documents from `/specs/001-user-auth/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Inicializar la base del backend y la estructura del monorepo.

- [X] T001 Create backend project structure per implementation plan in `backend/src/main/java/com/dappunq/`, `backend/src/test/java/com/dappunq/`, and `backend/src/main/resources/`
- [X] T002 Initialize Java 25 + Spring Boot 4 project in `backend/build.gradle`, `backend/settings.gradle`, and `backend/gradlew`
- [X] T003 [P] Configure shared project conventions and formatting in `backend/build.gradle` and `backend/.editorconfig`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Preparar infraestructura base que debe completarse antes de cualquier historia de usuario.

- [X] T004 Configure local PostgreSQL profile and datasource settings in `backend/src/main/resources/application-local.properties`
- [X] T005 [P] Create Testcontainers database bootstrap for integration tests in `backend/src/test/java/com/dappunq/config/TestcontainersConfig.java`
- [X] T006 [P] Implement global API error mapping in `backend/src/main/java/com/dappunq/exception/GlobalExceptionHandler.java`
- [X] T007 Create persistent user entity and repository contracts in `backend/src/main/java/com/dappunq/model/User.java` and `backend/src/main/java/com/dappunq/persistence/UserRepository.java`
- [X] T008 Implement JWT and password security infrastructure in `backend/src/main/java/com/dappunq/security/JwtService.java`, `backend/src/main/java/com/dappunq/security/JwtAuthenticationFilter.java`, and `backend/src/main/java/com/dappunq/security/SecurityConfig.java`
- [X] T009 Add application bootstrapping and environment readiness checks in `backend/src/main/java/com/dappunq/DappApplication.java` and `backend/src/main/resources/application.properties`

**Checkpoint**: La base funcional y de infraestructura está lista para que comiencen las historias de usuario.

---

## Phase 3: User Story 1 - Consultar perfil por id (Priority: P1) 🎯 MVP

**Goal**: Permitir que un usuario existente consulte su nombre por identificador sin depender de sesión activa.

**Independent Test**: Validar con una solicitud `GET /users/{id}/` usando un usuario ya registrado; confirmar `200` con `{"nombre":"string"}` y `404` con `"Usuario no encontrado"` para IDs inexistentes.

### Tests for User Story 1

- [X] T010 [P] [US1] Add integration test for profile lookup in `backend/src/test/java/com/dappunq/integration/UserProfileIntegrationTest.java`

### Implementation for User Story 1

- [X] T011 [P] [US1] Create `UserResponseDTO` record in `backend/src/main/java/com/dappunq/dto/UserResponseDTO.java`
- [X] T012 [P] [US1] Create user lookup service contract in `backend/src/main/java/com/dappunq/service/UserService.java`
- [X] T013 [US1] Implement profile retrieval logic and not-found handling in `backend/src/main/java/com/dappunq/service/UserService.java`
- [X] T014 [US1] Implement REST endpoint in `backend/src/main/java/com/dappunq/controller/UserControllerRest.java`
- [X] T015 [US1] Add domain exceptions and controller mapping for missing user cases in `backend/src/main/java/com/dappunq/exception/UserNotFoundException.java` and `backend/src/main/java/com/dappunq/exception/GlobalExceptionHandler.java`

**Checkpoint**: User Story 1 queda funcional y verificable de forma independiente.

---

## Phase 4: User Story 2 - Iniciar sesión con credenciales válidas (Priority: P1)

**Goal**: Permitir autenticar un usuario válido y devolver token JWT en la cabecera `Authorization` junto con el nombre.

**Independent Test**: Validar con `POST /login` con nombre y password correctos; confirmar 200, payload `{"nombre":"string"}` y header `Authorization: Bearer ...`; verificar además que credenciales inválidas respondan 400 con `"Credenciales inválidas"`.

### Tests for User Story 2

- [X] T016 [P] [US2] Add login integration test in `backend/src/test/java/com/dappunq/integration/AuthLoginIntegrationTest.java`

### Implementation for User Story 2

- [X] T017 [P] [US2] Create request DTO for auth payloads in `backend/src/main/java/com/dappunq/dto/UserRequestDTO.java`
- [X] T018 [P] [US2] Implement credential validation and BCrypt hashing in `backend/src/main/java/com/dappunq/service/AuthService.java`
- [X] T019 [US2] Implement JWT generation and token claims in `backend/src/main/java/com/dappunq/security/JwtService.java`
- [X] T020 [US2] Implement authentication controller in `backend/src/main/java/com/dappunq/controller/AuthControllerRest.java`
- [X] T021 [US2] Add invalid credentials and validation exception mapping in `backend/src/main/java/com/dappunq/exception/InvalidCredentialsException.java` and `backend/src/main/java/com/dappunq/exception/GlobalExceptionHandler.java`

**Checkpoint**: User Story 2 quedó habilitado y puede probarse sin depender de otras historias.

---

## Phase 5: User Story 3 - Crear una cuenta nueva (Priority: P1)

**Goal**: Permitir registrar nuevos usuarios con nombre único, contraseña en hash y respuesta JSON consistente.

**Independent Test**: Validar con `POST /register` para un nombre nuevo (200 y payload `{"nombre":"string"}`) y con nombre duplicado (400 y `"Usuario existente"`).

### Tests for User Story 3

- [X] T022 [P] [US3] Add registration integration test in `backend/src/test/java/com/dappunq/integration/AuthRegisterIntegrationTest.java`

### Implementation for User Story 3

- [X] T023 [P] [US3] Add domain validation rules for unique usernames and password hashing in `backend/src/main/java/com/dappunq/model/User.java`
- [X] T024 [US3] Implement registration flow in `backend/src/main/java/com/dappunq/service/AuthService.java`
- [X] T025 [US3] Add duplicate-user checks and error handling in `backend/src/main/java/com/dappunq/exception/UserAlreadyExistsException.java` and `backend/src/main/java/com/dappunq/exception/GlobalExceptionHandler.java`
- [X] T026 [US3] Ensure request sanitization and empty input validation in `backend/src/main/java/com/dappunq/dto/UserRequestDTO.java` and `backend/src/main/java/com/dappunq/controller/AuthControllerRest.java`

**Checkpoint**: Las tres historias de usuario quedan funcionales y cada una puede validarse por separado.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Validación final, documentación y calidad transversal.

- [X] T027 [P] Add end-to-end API smoke coverage in `backend/src/test/java/com/dappunq/e2e/UserAuthE2ETest.java`
- [X] T028 [P] Update the API contract and local validation doc in `docs/postman/UserAuth.postman_collection.json` and `backend/README.md`
- [X] T029 Run the full backend validation with `./gradlew test` and quickstart smoke checks from `specs/001-user-auth/quickstart.md`
- [X] T030 Refine code quality, naming, logging, and final cleanup across `backend/src/main/java/com/dappunq/` and `backend/src/test/java/com/dappunq/`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion; blocks all user stories.
- **User Story 1 (Phase 3)**: Depends on Foundational completion.
- **User Story 2 (Phase 4)**: Depends on Foundational completion.
- **User Story 3 (Phase 5)**: Depends on Foundational completion.
- **Polish (Phase 6)**: Depends on completion of all desired stories.

### User Story Dependencies

- **US1**: Can proceed after Phase 2; no dependency on US2 or US3.
- **US2**: Can proceed after Phase 2; depends on shared auth/security foundation and shared DTOs.
- **US3**: Can proceed after Phase 2; depends on same security foundation and unique-user rules.

### Parallel Opportunities

- All tasks marked `[P]` in Phase 1 and Phase 2 can run in parallel.
- User Story tests for each story can run in parallel with each other once foundational tasks are complete.
- `UserResponseDTO`, `UserRequestDTO`, and service contracts can be created in parallel as independent files.
- Different stories can be implemented by different developers after the foundation is ready.

---

## Parallel Example: Story 1

```bash
# Run profile tests and DTO creation in parallel after Phase 2:
Task: "Add integration test for profile lookup in backend/src/test/java/com/dappunq/integration/UserProfileIntegrationTest.java"
Task: "Create UserResponseDTO record in backend/src/main/java/com/dappunq/dto/UserResponseDTO.java"
Task: "Create user lookup service contract in backend/src/main/java/com/dappunq/service/UserService.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup.
2. Complete Phase 2: Foundational.
3. Complete Phase 3: User Story 1.
4. Stop and validate the `/users/{id}/` flow independently.
5. Deploy or demo only after the story is green.

### Incremental Delivery

1. Complete Setup + Foundational to establish the application shell and security foundation.
2. Deliver User Story 1 for profile reads.
3. Deliver User Story 2 for login and JWT issuance.
4. Deliver User Story 3 for registration.
5. Finish with cross-cutting polish and validation.

### Parallel Team Strategy

- Developer A: User Story 1
- Developer B: User Story 2
- Developer C: User Story 3
- Shared validation and docs: Phase 6

---

## Notes

- [P] tasks are independent and target different files or components.
- [US#] labels map every task to the relevant story for traceability.
- Story tasks are intentionally designed to be independently testable.
- Validation must happen against local PostgreSQL for dev and Testcontainers for automated tests.
- The implementation must keep controller logic thin and business logic in services/domain models per the constitution.

