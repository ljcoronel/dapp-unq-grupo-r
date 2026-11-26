# Implementation Plan: Ranking global de jugadores

**Branch**: `016-ranking-jugadores` | **Date**: 2026-11-26 | **Spec**: `backend/specs/016-ranking-jugadores/spec.md`

**Input**: Feature specification from `/specs/016-ranking-jugadores/spec.md`

## Summary

Se incorporara `GET /players/ranking` para consultar, sin crear ni dar de alta jugadores, el catalogo ya persistido por el scraping existente. Un Adapter JDBC ejecutara una consulta PostgreSQL que filtre primero, calcule el maximo de cotizaciones vigentes asociadas a estrategias ACTUALES, aplique el fallback a la cotizacion base o 1 credito, agregue tenencias solo de inversores y aplique los cuatro desempates antes de paginar. Cada consulta leera los datos vigentes, sin cache, para reflejar inmediatamente los cambios de `POST /quotes/scrape`. El Controller validara los parametros y mapeara respuestas; el Service mantendra la consolidacion fuera de HTTP y persistencia.

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Spring Boot 4, Spring Data JPA, Spring JDBC/JdbcTemplate, PostgreSQL, Jakarta Validation, springdoc OpenAPI 3.1; contrato existente en `docs/openapi.yaml`

**Storage**: PostgreSQL; entidades JPA existentes `players`, `teams`, `leagues`, `valuaciones`, `tenencias`, `usuarios`, `configuraciones_estrategia`

**Testing**: JUnit 5, Mockito, MockMvc, Testcontainers PostgreSQL

**Target Platform**: Servicio Spring Boot REST dentro del monorepo backend/frontend

**Project Type**: API web REST

**Performance Goals**: 95% de las consultas del catalogo activo completo bajo 2 segundos

**Constraints**: ranking solo lectura de jugadores persistidos; no crear jugadores desde la feature; poblar/actualizar el catalogo invocando `POST /quotes/scrape`; sin duplicados; filtros antes de rank/paginacion; paginacion estable; precision vigente de cotizaciones; 500 uniforme ante fallos; limite maximo 100; `docs/openapi.yaml` como contrato OpenAPI unico

**Scale/Scope**: cinco ligas y catalogo activo completo

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Monorepo**: PASS. Solo se modifica el backend y sus entregables documentales.
- **Layered Architecture**: PASS WITH DESIGN ACTION. El Controller solo valida/mapea DTO; Service orquesta modelos; Repository/Adapter ejecuta SQL y mapea filas; no hay dependencias de Controller en persistencia.
- **Rich Model**: PASS. `RankingFiltros`, `RankingJugador` y `RankingPagina` validaran sus invariantes sin dependencias JPA.
- **Layered Validation**: PASS. Request/Controller valida formato y rango; Service valida viabilidad; modelo valida reglas de ranking.
- **Testing and Quality**: PASS WITH DESIGN ACTION. Se agregaran unitarias, integracion Testcontainers y MockMvc E2E. El E2E llamara `POST /quotes/scrape` con un doble determinista del `ScrapingService` que persiste fixtures; se actualizara `docs/openapi.yaml` y Postman.
- **Spanish documentation**: PASS. Mensajes y artefactos en espanol; identificadores sin tildes ni `ñ`.

**Re-evaluacion post-diseno**: PASS. La consulta es read-only respecto de jugadores y no usa cache. El endpoint de scraping existente es el unico flujo de carga del catalogo en los escenarios de verificacion. `docs/openapi.yaml` es la fuente contractual unica; no se mantiene una especificacion OpenAPI de feature duplicada.

## Project Structure

### Documentation

```text
backend/specs/016-ranking-jugadores/
├── plan.md
├── research.md
├── data-model.md
└── quickstart.md
```

El contrato REST del ranking se incorpora en `docs/openapi.yaml`.

### Source Code

```text
backend/app/src/main/java/com/dappunq/
├── controller/
│   └── PlayerControllerRest.java
├── dto/ranking/
├── adapter/
│   └── RankingRepositoryAdapter.java
├── repository/
│   └── RankingRepository.java
├── service/
│   ├── RankingService.java
│   └── RankingServiceImpl.java
├── model/ranking/
│   ├── RankingFiltros.java
│   ├── RankingJugador.java
│   └── RankingPagina.java
└── persistence/
    ├── PlayerEntity.java
    ├── ValuationEntity.java
    ├── HoldingEntity.java
    └── schema.sql

backend/app/src/test/java/com/dappunq/
├── unit/
├── service/
├── integration/
└── e2e/
```

**Structure Decision**: Se mantiene Spring Boot y el layout por capas existente, agregando paquetes `repository`, `adapter` y `model/ranking` para separar la consolidacion SQL del HTTP y la persistencia JPA. No se agrega un flujo de creacion de jugadores al ranking; los E2E llaman la ruta real de scraping y sustituyen solo el servicio externo por fixtures deterministas persistidos por el test double.

## Complexity Tracking

No se identifican violaciones constitucionales que requieran complejidad adicional.
