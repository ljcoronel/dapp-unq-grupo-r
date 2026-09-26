# Implementation Plan: Player catalog backed by football-data

**Branch**: `003-player-catalog` | **Date**: 2026-09-25 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/003-player-catalog/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

Almacenar al arrancar la aplicación los máximos goleadores de las cinco ligas configuradas,
obtenidos desde football-data.org mediante `RestClient`, y exponerlos exclusivamente desde
PostgreSQL a través de `GET /players`. El payload externo se modelará con records Java,
se normalizará al modelo de dominio y se persistirá en entidades separadas; cada liga
conservará entre cero y diez jugadores según la respuesta recibida.

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Spring Boot 4, Spring Web `RestClient`, Spring Data JPA, Spring Validation, Jackson, PostgreSQL driver

**Storage**: PostgreSQL; Hibernate `ddl-auto=update` in the existing local setup

**Testing**: JUnit 5, Spring Boot Test, MockMvc, Testcontainers PostgreSQL; Mockito for isolated client/service tests

**Target Platform**: Backend Spring Boot on the existing development/runtime environment

**Project Type**: Backend REST web service within the existing monorepo

**Performance Goals**: `GET /players` performs only one database read path and never calls football-data; startup performs one request per configured league

**Constraints**: `X-Auth-Token` must come from an environment-backed configuration property; no token in source control; requests to football-data must use HTTPS; each successful non-empty startup response replaces the existing snapshot for that league; the persisted player primary key is the external `player.id` and is never generated locally; failures preserve the previous snapshot; empty responses do not alter stored rows; numeric zero remains distinct from null

**Scale/Scope**: Five fixed leagues, up to 50 player rows per refresh, seven display fields plus integer identity, and two public read endpoints without JWT authentication

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Monorepo**: PASS. This implementation changes only `backend/` and the
  feature documentation under `specs/`; no frontend source code is in scope.
- **Arquitectura en capas**: PASS. External records/client, service, domain model, persistence entities/repositories, controller and response DTOs stay separated.
- **Modelo rico**: PASS. Domain objects enforce valid league/player invariants and expose the truncation/normalization behavior without framework dependencies.
- **Validacion por niveles**: PASS. External payload validation/mapping occurs at the integration boundary; service validates availability and refresh policy; domain validates non-empty identity and bounded collections.
- **Testing y calidad**: PASS. Unit tests cover mapping/rules, integration tests use Testcontainers PostgreSQL, and MockMvc E2E tests cover `GET /players`; Postman collection is updated.
- **Acceso público**: PASS. `GET /players` y `GET /players/{id}` no requieren JWT; el token queda restringido al cliente interno de football-data.

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)
```text
backend/
└── app/
    └── src/
        ├── main/java/com/dappunq/
        │   ├── controller/       # GET /players
        │   ├── dto/              # public response records and external records
        │   ├── model/            # domain league/player/statistics
        │   ├── persistence/      # JPA entities and repositories
        │   └── service/           # refresh, mapping and read orchestration
        ├── main/resources/       # datasource and football-data configuration
        └── test/java/com/dappunq/
            ├── unit/              # model and mapping tests
            ├── integration/       # service/repository + Testcontainers
            └── e2e/               # MockMvc endpoint tests

```

**Structure Decision**: Mantener el monorepo existente, pero limitar esta
implementación a `backend/`. El backend concentra la integración, dominio,
persistencia, pruebas y contrato REST. El frontend queda fuera del alcance y no
se modificarán sus archivos; el contrato documentado queda disponible para un
consumidor futuro. Los archivos de especificación viven en
`specs/003-player-catalog/`.

## Complexity Tracking

No hay violaciones de la constitución que requieran justificación.

## Re-evaluación posterior al diseño

- **Monorepo y capas**: PASS. El contrato, cliente externo, dominio, entidades,
  repositorios, servicio y controller tienen ubicaciones y responsabilidades
  separadas.
- **Modelo y validación**: PASS. `Integer` conserva `null` frente a cero, el
  agregado limita a diez jugadores, valida el `player.id` externo y descarta
  identidades no publicables.
- **Persistencia y disponibilidad**: PASS. El endpoint lee solo PostgreSQL,
  reemplaza snapshots por liga en cada arranque exitoso, conserva ligas vacías y
  mantiene snapshots anteriores cuando falla una liga.
- **Testing y entregables**: PASS. El quickstart exige unit, integración con
  Testcontainers, MockMvc E2E y actualización de Postman.
