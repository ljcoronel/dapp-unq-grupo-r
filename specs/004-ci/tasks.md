---
description: "Task list for backend CI workflow implementation"
---

# Tasks: CI del backend

**Input**: Design documents from `/specs/004-ci/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, quickstart.md

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Initialize the CI workflow and verify the backend wrapper is ready for GitHub-hosted execution.

- [x] T001 [P] Crear la estructura del workflow de CI en .github/workflows/ci-backend.yml
- [x] T002 [P] Verificar y dejar ejecutable el wrapper de Gradle para Linux en backend/gradlew

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Build the backend CI workflow skeleton and the validation path before any story can be delivered.

- [x] T003 Definir triggers de `push` y `pull_request` para `main` con filtros de rutas en .github/workflows/ci-backend.yml
- [x] T004 Configurar el job `build-and-test` en `ubuntu-latest` con Docker disponible para Testcontainers en .github/workflows/ci-backend.yml
- [x] T005 Establecer `defaults.run.working-directory: ./backend` y `actions/checkout@v4` + `actions/setup-java@v4` con Temurin Java 25 y caché de Gradle en .github/workflows/ci-backend.yml
- [x] T006 Agregar la etapa de compilación `./gradlew clean compileTestJava` y bloquear la continuación si falla en .github/workflows/ci-backend.yml
- [x] T007 Agregar la etapa de pruebas `./gradlew test` para unitarias, integración y E2E y mantener el resultado fallido del job en .github/workflows/ci-backend.yml
- [x] T008 Agregar la publicación de resultados JUnit XML y la carga de artefactos con `if: always()` en .github/workflows/ci-backend.yml

**Checkpoint**: La base del pipeline esta lista; la validación de cambios del backend puede comenzar.

---

## Phase 3: User Story 1 - Validar cambios del backend (Priority: P1) 🎯 MVP

**Goal**: Trigger the backend CI workflow only when the backend or the CI workflow itself changes on main.

**Independent Test**: Hacer un push o PR a `main` con cambios bajo `backend/` o `.github/workflows/ci-backend.yml` y comprobar que el workflow se ejecuta; luego confirmar que un cambio solo en `frontend/` no lo activa.

### Implementation for User Story 1

- [x] T009 [US1] Configurar activación en `push` y `pull_request` con rama destino `main` y rutas `backend/**` + `.github/workflows/ci-backend.yml` en .github/workflows/ci-backend.yml
- [x] T010 [US1] Definir el nombre del job `build-and-test` y el runner `ubuntu-latest` sin servicios de PostgreSQL declarados en .github/workflows/ci-backend.yml
- [x] T011 [US1] Configurar los pasos de checkout, Java y directorio de trabajo del backend para que el pipeline valide solo el monorepo backend en .github/workflows/ci-backend.yml

**Checkpoint**: User Story 1 should be fully functional and testable independently.

---

## Phase 4: User Story 2 - Bloquear cambios que no superan las comprobaciones (Priority: P1)

**Goal**: Force a failed workflow when compilation or tests fail, without masking the root failure.

**Independent Test**: Introducir por separado un fallo de compilación y una prueba fallida, y verificar que el job termina en `failure` y que no se reporta como exitoso.

### Implementation for User Story 2

- [x] T012 [US2] Ordenar la compilación antes de pruebas y hacer que un fallo de `compileTestJava` detenga la ejecución sin continuar a `test` en .github/workflows/ci-backend.yml
- [x] T013 [US2] Dejar la etapa de pruebas como condición de fallo del job y conservar el estado fallido final aunque la publicación de resultados también ejecute en .github/workflows/ci-backend.yml

**Checkpoint**: At this point, User Story 2 should be independently functional and enforce quality gates.

---

## Phase 5: User Story 3 - Consultar los resultados de las pruebas (Priority: P2)

**Goal**: Publish JUnit XML reports even after failed steps so the team can inspect the exact failing tests.

**Independent Test**: Forzar una ejecución con pruebas fallidas y confirmar que el reportero y el artefacto se ejecutan aunque la suite falle.

### Implementation for User Story 3

- [x] T014 [US3] Configurar `dorny/test-reporter@v1` con `reporter: java-junit`, ruta `backend/app/build/test-results/test/*.xml` y `fail-on-empty: false` en .github/workflows/ci-backend.yml
- [x] T015 [US3] Agregar `actions/upload-artifact@v4` para `backend/app/build/test-results/test/*.xml` con `if: always()` y `if-no-files-found: ignore` en .github/workflows/ci-backend.yml
- [x] T016 [US3] Omitir el Check Run del reportero para PRs desde forks sin permisos de escritura y conservar la carga de artefactos sin convertir fallos en éxito en .github/workflows/ci-backend.yml

**Checkpoint**: All user stories should now be independently functional and observable through CI reports.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final validation, documentation alignment, and correctness checks across the workflow.

- [x] T017 [P] Revisar la cobertura de triggers y rutas para asegurarse de que `frontend/` no active el workflow en .github/workflows/ci-backend.yml
- [x] T018 [P] Validar el flujo de trabajo contra los escenarios de `specs/004-ci/quickstart.md` y los comandos locales de Gradle en backend/
- [x] T019 Confirmar que el workflow mantiene el orden de pasos: checkout, Java, compilación, pruebas y publicación en .github/workflows/ci-backend.yml

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion - blocks all story work.
- **User Story 1 (Phase 3)**: Depends on Foundational - validates backend trigger logic.
- **User Story 2 (Phase 4)**: Depends on Foundational and story 1 trigger validation.
- **User Story 3 (Phase 5)**: Depends on Foundational and the main CI execution path.
- **Polish (Phase 6)**: Depends on all user stories being complete.

### User Story Dependencies

- **US1**: No dependency on other stories; validates the workflow trigger behavior.
- **US2**: Depends on the backend compile/test execution path defined in the foundation.
- **US3**: Depends on the successful execution path and the generated JUnit XML files.

### Parallel Opportunities

- Setup tasks T001 and T002 can run in parallel.
- Once the workflow file exists, US1, US2, and US3 can be implemented in parallel if several developers work on the same file while keeping changes isolated by sections.
- Polish tasks T017 and T018 can run in parallel after the workflow is complete.

---

## Parallel Example: User Story 1

```bash
# Disparadores y configuracion del job
Task: "Configurar activación en push/pull_request con rama main y rutas de backend/workflow"
Task: "Definir el job build-and-test y runner ubuntu-latest"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: User Story 1
4. Validate that backend and workflow changes trigger the CI job correctly
5. Stop and confirm the trigger behavior before expanding to failure handling or reporting

### Incremental Delivery

1. Setup + Foundational -> workflow skeleton ready
2. Add US1 -> backend trigger validation and job definition
3. Add US2 -> compile/test gating and failure handling
4. Add US3 -> result reporting and artifact publication
5. Finish with polish checks and quickstart validation

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup and Foundational together.
2. Developer A focuses on US1 trigger logic.
3. Developer B focuses on US2 fail-fast behavior.
4. Developer C focuses on US3 reporting and artifacts.
5. Final validation checks ensure the workflow is consistent across all stories.

---

## Notes

- [P] tasks = different files or clearly independent workstreams.
- [Story] labels map tasks to specific user stories for traceability.
- Each user story should be independently testable in GitHub Actions.
- Validation occurs through push/PR scenarios and workflow execution outcomes.
- Preserve failure state even when reporting or artifact upload steps are executed.
