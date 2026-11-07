# Implementation Plan: Formato de respuesta de jugadores

**Branch**: `010-players-response-format` | **Date**: 2026-10-10 | **Spec**: `backend/specs/010-players-response-format/spec.md`

## Summary

Se reemplazara el listado agrupado por liga de `GET /players` por una lista plana
de `PlayerResponse`. El record reflejara todos los campos de `PlayerEntity`
excepto `id`, con un `TeamResponse` para `TeamEntity` y la relacion publica de
liga vigente. `PlayerCatalogReadService.findCatalog()` usara `playerRepository.findAll()`
sin `leagueRepository`; el detalle por identificador y los errores actuales se
conservaran.

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Spring Boot 4, Spring Data JPA, Jackson, JUnit 5, MockMvc

**Storage**: PostgreSQL mediante entidades JPA y repositorios Spring Data

**Testing**: JUnit 5, pruebas de servicio con Mockito y pruebas E2E MockMvc

**Target Platform**: Servicio web Spring Boot dentro del backend del monorepo

**Project Type**: Web service REST

**Performance Goals**: Una lectura `findAll()` para construir el listado, sin una
consulta adicional por cada liga; el orden queda determinado por el repositorio.

**Constraints**: La respuesta excluye ids internos; debe conservar nulabilidad,
ceros validos, acceso publico, error 503 y comportamiento de `GET /players/{id}`.
El contrato anterior se reemplaza de manera incompatible.

**Scale/Scope**: DTOs de jugadores/equipo, servicio de lectura, controlador,
OpenAPI y pruebas del endpoint.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Monorepo**: PASS. El cambio se limita al backend y sus artefactos de
  especificacion; frontend conserva su independencia.
- **Layered Architecture**: PASS WITH JUSTIFICATION. La constitucion preferiria
  que el servicio devolviera modelos y el controlador mapeara DTOs, pero el
  requisito exige explicitamente `List<PlayerResponse>` en `findCatalog()` y el
  servicio actual ya expone DTOs. Se conserva la frontera vigente sin introducir
  una refactorizacion no solicitada.
- **Rich Model**: PASS. Solo se agregan records DTO y mapeo de lectura; no se
  agregan reglas ni anotaciones al dominio.
- **Layered Validation**: PASS. La persistencia resuelve las relaciones y el
  servicio conserva las validaciones de id y disponibilidad.
- **Testing and Quality**: PASS WITH DESIGN ACTION. Se actualizaran las pruebas
  E2E y se agregaran pruebas de servicio para `findAll`, ausencia de uso de
  `LeagueRepository`, respuesta vacia, nulos, ceros y error.
- **Spanish documentation**: PASS. La documentacion funcional y los mensajes
  nuevos permanecen en espanol; los identificadores Java siguen las convenciones
  existentes.

## Project Structure

### Documentation

```text
backend/specs/010-players-response-format/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/
   └── players-api.yaml
```

### Source Code

```text
backend/app/src/main/java/com/dappunq/
├── controller/PlayerControllerRest.java
├── dto/PlayerResponse.java
├── dto/TeamResponse.java
├── service/PlayerCatalogReadService.java
└── persistence/
   ├── PlayerEntity.java
   ├── TeamEntity.java
   ├── LeagueEntity.java
   └── PlayerRepository.java

backend/app/src/main/resources/static/openapi.yaml

backend/app/src/test/java/com/dappunq/
├── e2e/PlayerCatalogE2ETest.java
├── e2e/PlayerCatalogGroupingE2ETest.java
├── e2e/PlayerIncompleteDataE2ETest.java
├── e2e/PlayerByIdE2ETest.java
└── service/PlayerCatalogReadServiceTest.java
```

**Structure Decision**: Se conserva la estructura Spring Boot existente. El
controlador mantiene `GET /players/{id}`, el servicio realiza el nuevo listado
plano y los records DTO encapsulan la forma publica sin exponer entidades JPA.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|---|---|---|
| Servicio retorna DTO | Es parte del cambio solicitado y ya es el contrato interno vigente | Refactorizar a modelos y mover el mapeo al controlador ampliaria el alcance y alteraria mas endpoints |
