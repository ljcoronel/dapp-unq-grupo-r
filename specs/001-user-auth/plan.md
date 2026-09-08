# Implementation Plan: Autenticacion y perfil de usuario

**Branch**: `001-user-auth` | **Date**: 2026-09-08 | **Spec**: `/specs/001-user-auth/spec.md`

**Input**: Feature specification from `/specs/001-user-auth/spec.md`

## Summary

Se crea un backend REST de autenticación y perfil de usuario bajo una arquitectura en capas, con Java 25 + Spring Boot 4, PostgreSQL local para desarrollo y Testcontainers para pruebas. La solución expone los endpoints `GET /users/{id}/`, `POST /login` y `POST /register`, incorpora `UserControllerRest`, `AuthControllerRest`, `GlobalExceptionHandler`, DTOs de record y validación por capas según la constitución.

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Spring Boot 4, Spring Web, Spring Data JPA, Spring Validation, Spring Security, JWT library, PostgreSQL Driver, Testcontainers, JUnit 5

**Storage**: PostgreSQL en `jdbc:postgresql://localhost:5432/dappfc` para entorno local; Testcontainers para pruebas de integración y E2E con MockMvc

**Testing**: JUnit 5, MockMvc, Testcontainers, Spring Boot Test

**Target Platform**: Local development and CI runner; backend service deployed in JVM runtime with PostgreSQL backing store

**Project Type**: Web service with monorepo architecture (backend + frontend)

**Performance Goals**: API response under 200ms p95 for simple profile and auth operations in local environment; support small to medium user base in development and test setups

**Constraints**: Must follow layered architecture, model-rich domain, DTO validation, exception mapping; no business logic in controllers; tests must never hit the local profile for DB setup during automated validation

**Scale/Scope**: MVP of user auth and profile retrieval for a single application domain; not a multi-tenant or distributed system

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Pass: Monorepo with backend/frontend separation is compatible with the constitution.
- Pass: Architecture in layers is enforced via controller/service/model/persistence separation.
- Pass: Model-rich logic will live in domain model, while persistence uses entities and services orchestrate mapping.
- Pass: Validation by layers is required: DTO sanitization, service existence/availability checks, domain invariants in model.
- Pass: Testing strategy is aligned with JUnit 5 + Testcontainers + MockMvc E2E separation.
- Pass: Local credentials and database URL are explicitly flagged for dev setup only; automated tests must use Testcontainers.

## Project Structure

### Documentation (this feature)

```text
specs/001-user-auth/
├── spec.md              # Feature contract and acceptance criteria
├── plan.md              # This file
├── research.md          # Decisions and rationale
├── data-model.md        # Entities and validation rules
├── quickstart.md        # Local run and verification guide
├── contracts/
│   └── user-auth-api.yaml
└── tasks.md             # Not created by /speckit-plan; generated in implementation phase
```

### Source Code (repository root)

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/dappunq/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       │   ├── AuthControllerRest.java
│   │   │       │   └── UserControllerRest.java
│   │   │       ├── dto/
│   │   │       │   ├── UserRequestDTO.java
│   │   │       │   └── UserResponseDTO.java
│   │   │       ├── exception/
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       ├── model/
│   │   │       ├── persistence/
│   │   │       ├── security/
│   │   │       └── service/
│   │   └── resources/
│   │       └── application-local.properties
│   └── test/
│       └── java/
│           ├── integration/
│           ├── unit/
│           └── e2e/
├── build.gradle
├── gradlew
└── docker-compose.yml

frontend/
├── src/
│   ├── components/
│   ├── pages/
│   └── services/
├── package.json
└── vite.config.js
```

**Structure Decision**: This feature is implemented as a backend-first REST API inside a monorepo. The backend owns the domain, controllers, security, DTOs and persistence logic. The frontend remains an optional consumer of the API and is intentionally not part of the MVP contracts.

## Complexity Tracking

No constitution violations identified. The project is a straightforward layered web service with standard auth and persistence requirements; no exception justification is required.
