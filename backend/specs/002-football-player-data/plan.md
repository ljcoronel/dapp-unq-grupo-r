# Implementation Plan: Datos de jugadores de futbol

**Branch**: `002-football-player-data` | **Date**: 2026-09-15 | **Spec**: `/specs/002-football-player-data/spec.md`

**Input**: Feature specification from `/specs/002-football-player-data/spec.md`

## Summary

Se incorpora al backend REST existente un módulo de jugadores de fútbol con soporte de listado filtrable por liga, equipo y posición, y detalle por id. La solución usará Java 25 + Spring Boot 4, PostgreSQL local para desarrollo y Testcontainers para integración, con un flujo de sincronización desde Football-Data.org y WhoScored que normaliza los datos y los persiste localmente conservando historial por snapshot. Se siguen los principios de arquitectura en capas, DTOs records, manejo centralizado de errores y validación por niveles definidos por la constitución.

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Spring Boot 4, Spring Web, Spring Data JPA, Spring Validation, Spring Security, PostgreSQL Driver, JWT, Testcontainers, JUnit 5

**Storage**: PostgreSQL en `jdbc:postgresql://localhost:5432/dappfc` para entorno local; Testcontainers para pruebas de integración y E2E con MockMvc

**Testing**: JUnit 5, MockMvc, Testcontainers, Spring Boot Test

**Target Platform**: Local development and CI; JVM backend service with PostgreSQL persistence

**Project Type**: Web service with monorepo architecture (backend + frontend)

**Performance Goals**: Respuesta REST por debajo de 200ms p95 para listados simples y consultas por id en entorno local; soporte para catálogo de jugadores en 5 ligas y datos históricos de snapshot

**Constraints**: Debe respetar arquitectura en capas; no se permite lógica de negocio en controllers; los DTOs deben validar y sanitizar inputs; las pruebas automatizadas no pueden depender del perfil local de PostgreSQL; la sincronización externa debe tolerar fallos y usar datos persistidos locales

**Scale/Scope**: MVP del catálogo de jugadores dentro de la API existente; 5 ligas soportadas y un dominio de datos deportivo que incluye estadísticas, historial y filtros combinados por query params

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Pass: El repositorio sigue siendo un monorepo donde backend y frontend conviven en el mismo proyecto, pero el módulo de jugadores se implementa en backend.
- Pass: Se respetará la arquitectura en capas con controller, service, model y persistence; el controller solo serializa/deserializa DTOs y delega en el service.
- Pass: El dominio de jugador se modelará con invariantes de negocio y validaciones en el modelo, mientras que la capa de persistence gestionará entidades y snapshots.
- Pass: La validación de formato y sanitización se realizará en los DTO records `JugadorRequestDTO` y `JugadorResponseDTO`; la comprobación de existencia y consistencia de datos será responsabilidad del service.
- Pass: Las pruebas de integración y E2E se ejecutarán con Testcontainers y MockMvc, sin depender del perfil local de desarrollo.
- Pass: El perfil local con credenciales `postgres` / `root` se documenta como configuración de desarrollo; los tests no usarán ese perfil.

## Project Structure

### Documentation (this feature)

```text
specs/002-football-player-data/
├── spec.md              # Feature contract and acceptance criteria
├── plan.md              # This file
├── research.md          # Decisions and rationale
├── data-model.md        # Entities, validation rules, and contracts mapping
├── quickstart.md        # Local run and verification guide
├── contracts/
│   └── players-api.yaml # OpenAPI contract for /players and /players/{id}
└── tasks.md             # Not created by /speckit-plan; generated in implementation phase
```

### Source Code (repository root)

```text
backend/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/dappunq/
│   │   │   │   ├── controller/
│   │   │   │   │   ├── JugadorControllerRest.java
│   │   │   │   │   └── UserControllerRest.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── JugadorRequestDTO.java
│   │   │   │   │   ├── JugadorResponseDTO.java
│   │   │   │   │   ├── UserRequestDTO.java
│   │   │   │   │   └── UserResponseDTO.java
│   │   │   │   ├── exception/
│   │   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   │   └── custom domain exceptions
│   │   │   │   ├── model/
│   │   │   │   │   └── Jugador.java
│   │   │   │   ├── persistence/
│   │   │   │   │   └── JugadorRepository.java
│   │   │   │   ├── service/
│   │   │   │   │   └── JugadorService.java
│   │   │   │   └── sync/
│   │   │   │       └── PlayerSyncService.java
│   │   │   └── resources/
│   │   │       └── application-local.properties
│   │   └── test/
│   │       ├── integration/
│   │       ├── e2e/
│   │       └── unit/
│   └── build.gradle
├── README.md
└── gradlew

frontend/
├── src/
│   ├── components/
│   ├── pages/
│   └── services/
└── package.json
```

**Structure Decision**: The feature is implemented as a backend-first REST expansion inside the existing monorepo. The existing Spring Boot backend will host the new player domain, DTOs, persistence, sync integration and controller layer; the frontend remains a consumer and does not define a separate backend contract for this iteration.

## Complexity Tracking

No constitution violations identified. The project already follows the layered and testing conventions required by the constitution, so no exception justification is required for this feature.
