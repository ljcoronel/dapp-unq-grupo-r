---

description: "Task list for implementing frontend user authentication"
---

# Tasks: Autenticación de usuario en frontend

**Input**: Design documents from `frontend/specs/001-user-auth/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/auth-api.yaml`, `quickstart.md`

**Tests**: No automated test tasks are included because the plan specifies the existing `npm run lint` and `npm run build` commands plus manual end-to-end validation against the local backend.

**Organization**: Tasks are grouped by user story so each increment can be implemented and manually validated independently.

## Path Conventions

- Frontend source: `frontend/src/`
- Configuration and dependencies: `frontend/package.json`, `frontend/tailwind.config.js`, `frontend/postcss.config.js`
- Feature validation: `frontend/specs/001-user-auth/quickstart.md`

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add the libraries and styling foundation required by the React SPA.

- [x] T001 Add `axios`, `react-router`, `tailwindcss`, `postcss`, and `autoprefixer` dependencies in `frontend/package.json` and refresh `frontend/package-lock.json`
- [x] T002 [P] Configure Tailwind content scanning for `frontend/index.html` and `frontend/src/**/*.{js,jsx}` in `frontend/tailwind.config.js`
- [x] T003 [P] Configure PostCSS with TailwindCSS and Autoprefixer plugins in `frontend/postcss.config.js`
- [x] T004 [P] Replace starter Vite styles with Tailwind entry directives and base layout styles in `frontend/src/index.css`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish shared API, validation, session, routing, and presentation boundaries before implementing individual flows.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T005 Create the Axios client with base URL `http://localhost:8080` and JSON defaults in `frontend/src/services/apiClient.js`
- [x] T006 [P] Implement shared credential validation in `frontend/src/utils/validation.js`, enforcing `nombre` as required, 4-16 characters inclusive, and no spaces, and `password` as required and 4-16 characters inclusive with all character types accepted
- [x] T007 [P] Create the shared authentication service module boundary in `frontend/src/services/authService.js`, keeping all login and registration HTTP calls outside presentation components
- [x] T008 Create the global authentication context in `frontend/src/context/AuthContext.jsx`, initializing `authToken` from `localStorage`, exposing `isAuthenticated`, `isLoading`, `error`, login, registration, and logout operations, and surfacing network, HTTP, null-response, and missing-token failures
- [x] T009 [P] Create the reusable authentication form shell in `frontend/src/components/AuthForm.jsx`, rendering username/password fields, always-visible rules, submit loading state, validation feedback, and a configurable primary action
- [x] T010 [P] Create the protected-route wrapper in `frontend/src/components/ProtectedRoute.jsx`, redirecting unauthenticated users to `/login` and rendering protected content only when a token exists
- [x] T011 Remove starter component wiring and mount the authentication provider and router entry point from `frontend/src/main.jsx`

**Checkpoint**: Shared API, validation, session, form, and route infrastructure are ready for independent story implementation.

---

## Phase 3: User Story 1 - Crear una cuenta nueva (Priority: P1) 🎯 MVP

**Goal**: Let a new user submit valid credentials to the real `POST /register` endpoint, receive clear validation/error feedback, and reach `/login` after successful registration.

**Independent Test**: At `/register`, verify the permanent rules, confirm empty, short, long, and spaced usernames are blocked before a request, submit valid credentials to the backend, and confirm navigation to `/login` after a successful 200 response.

### Implementation for User Story 1

- [x] T012 [US1] Implement `register(nombre, password)` in `frontend/src/services/authService.js` as `POST /register` with JSON `{ nombre, password }`, returning the expected `UserResponse` and preserving Axios failures for the UI
- [x] T013 [US1] Create the registration page in `frontend/src/pages/RegisterPage.jsx` using `AuthForm`, shared validation, visible Spanish rules, a `Crear cuenta` submit action, loading feedback, and actionable validation/backend error messages
- [x] T014 [US1] Add the existing-account navigation control from `frontend/src/pages/RegisterPage.jsx` to `/login` without a full page reload
- [x] T015 [US1] Wire registration submission in `frontend/src/pages/RegisterPage.jsx` through `AuthContext` and navigate to `/login` only after a successful response without creating an authenticated session

**Checkpoint**: Registration is independently usable, rejects invalid input locally, calls the real backend only for valid input, and redirects to login on success.

---

## Phase 4: User Story 2 - Iniciar sesión con una cuenta existente (Priority: P1)

**Goal**: Authenticate an existing user through `POST /login`, persist the JWT from the `Authorization` header under `localStorage.authToken`, and navigate to protected Home.

**Independent Test**: At `/login`, verify the same rules as registration, confirm invalid fields block submission, confirm invalid credentials show `Nombre de usuario o contraseña incorrectos`, and confirm valid credentials store the bearer token and navigate to Home.

### Implementation for User Story 2

- [ ] T016 [US2] Implement `login(nombre, password)` in `frontend/src/services/authService.js` as `POST /login` with JSON `{ nombre, password }`, extracting the JWT from the `Authorization` response header and rejecting missing or malformed token responses
- [ ] T017 [US2] Add login state transitions to `frontend/src/context/AuthContext.jsx`, persisting only the extracted token under `localStorage.authToken`, deriving `isAuthenticated`, and mapping invalid-credential, network, HTTP, and missing-token errors to visible Spanish messages
- [ ] T018 [US2] Create the login page in `frontend/src/pages/LoginPage.jsx` using `AuthForm`, shared validation, always-visible rules, an `iniciar sesión` submit action, loading feedback, and the required invalid-credentials message
- [ ] T019 [US2] Add the new-user navigation control from `frontend/src/pages/LoginPage.jsx` to `/register` without a full page reload
- [ ] T020 [US2] Wire successful login navigation from `frontend/src/pages/LoginPage.jsx` to the protected Home route and prevent duplicate submissions while `isLoading` is true

**Checkpoint**: Login is independently usable, persists a valid token from the response header, reports failures visibly, and reaches protected Home only after successful authentication.

---

## Phase 5: User Story 3 - Navegación entre pantallas de acceso (Priority: P2)

**Goal**: Make the register/login alternation explicit and consistent from either authentication screen.

**Independent Test**: Open either `/register` or `/login`, activate the alternate-flow action, and verify the other screen appears through client-side navigation with its own title, action label, and unchanged shared validation rules.

### Implementation for User Story 3

- [ ] T021 [US3] Configure client-side routes for `/register` and `/login`, including a sensible default route, in `frontend/src/App.jsx`
- [ ] T022 [US3] Align the register and login page headings, explanatory text, alternate-flow labels, and submit labels in `frontend/src/pages/RegisterPage.jsx` and `frontend/src/pages/LoginPage.jsx` so each screen is clearly distinguishable while retaining identical field rules
- [ ] T023 [US3] Verify route transitions preserve SPA behavior and do not submit or retain unfinished form operations when switching screens in `frontend/src/pages/RegisterPage.jsx` and `frontend/src/pages/LoginPage.jsx`

**Checkpoint**: Users can move between both access screens from either direction with clear actions and no full reload.

---

## Phase 6: User Story 4 - Navegación dentro de la pantalla de Home (Priority: P2)

**Goal**: Provide a protected, minimal Home with a right-aligned profile menu containing profile and logout actions.

**Independent Test**: Authenticate successfully, confirm Home shows only the navbar and generic profile icon, open the menu, verify `Ir a mi Perfil` and `Cerrar sesión`, then logout and confirm token removal plus redirection to `/login`; direct Home access without a token must also redirect.

### Implementation for User Story 4

- [ ] T024 [P] [US4] Create the profile menu component in `frontend/src/components/ProfileMenu.jsx`, exposing a generic profile icon and menu options `Ir a mi Perfil` and `Cerrar sesión`, with the profile option intentionally inert
- [ ] T025 [US4] Create the minimal Home page in `frontend/src/pages/HomePage.jsx` with a top navbar, right-aligned `ProfileMenu`, and no unrelated main content
- [ ] T026 [US4] Implement logout in `frontend/src/context/AuthContext.jsx` to remove `localStorage.authToken`, clear in-memory authentication/error state, and navigate to `/login`
- [ ] T027 [US4] Register the protected `/home` route in `frontend/src/App.jsx` through `ProtectedRoute` and ensure the authenticated login flow targets that route
- [ ] T028 [US4] Add the protected-route fallback and authenticated-session rehydration behavior in `frontend/src/App.jsx` and `frontend/src/context/AuthContext.jsx`, including redirecting direct unauthenticated Home access to `/login`

**Checkpoint**: Authenticated users reach a minimal protected Home, can inspect the profile menu, and can end the session safely.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Validate the completed flow and align documentation/configuration with the implemented behavior.

- [ ] T029 [P] Update `frontend/src/App.css` and any affected `frontend/src/index.css` rules to remove starter Vite styling and ensure the auth screens, navbar, menu, validation errors, and loading states remain readable and responsive with TailwindCSS
- [ ] T030 [P] Update `frontend/specs/001-user-auth/quickstart.md` with the final route names, token-header behavior, Spanish error states, and Home/logout checks if implementation details differ from the documented flow
- [ ] T031 Run `npm run lint` from `frontend/` and resolve all lint errors in changed source files
- [ ] T032 Run `npm run build` from `frontend/` and resolve any production bundling or route integration errors
- [ ] T033 Execute every manual scenario in `frontend/specs/001-user-auth/quickstart.md` against the local backend, including invalid input, duplicate user, invalid credentials, missing token, network failure, persistence after reload, and logout

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; dependency and Tailwind configuration can begin immediately.
- **Foundational (Phase 2)**: Depends on Setup; blocks all user story work.
- **User Story 1 (Phase 3)**: Depends on Foundational; delivers the registration MVP increment.
- **User Story 2 (Phase 4)**: Depends on Foundational and the shared route/form boundaries; it can be implemented in parallel with US1 after T009, but final navigation targets the protected route created by US4.
- **User Story 3 (Phase 5)**: Depends on the US1/US2 page components and foundational routing entry point.
- **User Story 4 (Phase 6)**: Depends on the AuthContext and ProtectedRoute from Phase 2 plus the login success flow from US2.
- **Polish (Phase 7)**: Depends on all desired user stories.

### User Story Dependencies

- **US1 (P1)**: No dependency on another user story after Foundational.
- **US2 (P1)**: No dependency on another user story after Foundational; Home route wiring is completed in US4.
- **US3 (P2)**: Depends on the register and login pages from US1 and US2.
- **US4 (P2)**: Depends on shared session infrastructure and the successful-login path from US2.

### Within Each User Story

- Shared validation and service boundaries precede page implementation.
- Service operations precede page submission wiring.
- Route wiring and integration follow the page components.
- Each checkpoint must pass its independent manual test before advancing.

### Parallel Opportunities

- T002, T003, and T004 can run in parallel after dependency installation.
- T006, T007, T009, and T010 can run in parallel within the foundational phase once setup is complete.
- US1 and US2 can be assigned in parallel after the foundational checkpoint, provided they coordinate changes to the shared `authService.js` and `AuthContext.jsx`.
- T024 and T025 can run in parallel within US4.
- T029 and T030 can run in parallel after feature implementation.

## Parallel Example: User Story 1

```text
Task: "Implement register(nombre, password) in frontend/src/services/authService.js"
Task: "Create the registration page in frontend/src/pages/RegisterPage.jsx"
```

These tasks can start together only after the foundational service boundary and `AuthForm` contract exist; T015 integrates their completed behavior.

## Parallel Example: User Story 2

```text
Task: "Implement login(nombre, password) in frontend/src/services/authService.js"
Task: "Create the login page in frontend/src/pages/LoginPage.jsx"
```

These tasks can start together after the foundational phase; T017 and T020 connect token persistence and navigation.

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup.
2. Complete Phase 2: Foundational.
3. Complete Phase 3: User Story 1.
4. Validate `/register` independently against the local backend and confirm redirect to `/login`.
5. Stop for an MVP demo before adding login and protected Home.

### Incremental Delivery

1. Add US1 registration and validate it independently.
2. Add US2 login and token persistence, then validate the login-to-Home transition.
3. Add US3 navigation polish and verify both access screens from either direction.
4. Add US4 protected Home, profile menu, persistence, and logout.
5. Complete Phase 7 lint, build, documentation, and full manual quickstart validation.

### Parallel Team Strategy

1. Complete Setup and Foundational together.
2. Assign US1 registration and US2 login to separate developers while avoiding simultaneous edits to the same service/context sections.
3. Assign US3 route/navigation integration after both access pages exist.
4. Assign US4 Home/profile work after the login session contract is stable.

## Notes

- Every task uses the required `- [ ] [TaskID] [P?] [Story?] description` checklist format.
- `[P]` marks work that can proceed in parallel without depending on incomplete tasks in another file.
- Story labels map to the four user stories in `spec.md`.
- No automated test tasks were generated because no frontend test runner or TDD requirement is present in the design documents.
