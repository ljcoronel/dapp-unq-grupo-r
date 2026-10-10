# Implementation Plan: Migracion del catalogo a WhoScored

**Branch**: `009-whoscored-team-player-migration` | **Date**: 2026-10-10 | **Spec**: `backend/specs/009-whoscored-team-player-migration/spec.md`

## Summary

Se convertiran `TeamEntity`, `NewLeagueEntity` y `WhoScoredEntity` en entidades JPA con identificadores internos generados y `externalId` unico. Se completara `WhoScoredScrapingService` para normalizar la respuesta real de WhoScored y persistir primero equipos y ligas, y luego jugadores con sus relaciones, sin agregar variables de instancia a las tres entidades. El flujo de catalogo dejara de inyectar o invocar FootballData; los componentes heredados permaneceran en el repositorio y quedaran deprecados. El endpoint `POST /scrape` continuara publico y se validara el caso feliz con la URL real y `numberOfPlayers=1`.

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Spring Boot 4, Spring Data JPA, PostgreSQL, Playwright 1.58.0, Jackson, JUnit 5, Testcontainers

**Storage**: PostgreSQL mediante entidades JPA y repositorios Spring Data

**Testing**: JUnit 5; unit tests para normalizacion; integracion con PostgreSQL real mediante Testcontainers; E2E MockMvc; prueba real de scraping con `numberOfPlayers=1`

**Target Platform**: Servicio web Spring Boot ejecutado localmente o en entorno Linux/CI con Chromium de Playwright instalado

**Project Type**: Web service REST dentro de un monorepo backend/frontend

**Performance Goals**: Persistir un lote valido sin truncarlo artificialmente y sin hacer una consulta de existencia por cada jugador cuando pueda resolverse por lote

**Constraints**: No agregar variables de instancia a `TeamEntity`, `NewLeagueEntity` ni `WhoScoredEntity`; `externalId` debe ser UNIQUE en las tres tablas; `/scrape` no requiere autenticacion; fallos o respuestas invalidas no deben sustituir el ultimo catalogo valido

**Scale/Scope**: Las cinco ligas configuradas y todos los registros validos devueltos por WhoScored para cada actualizacion; migracion acotada al catalogo y a los componentes FootballData relacionados

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Monorepo**: PASS. Solo se modifica el backend y sus artefactos de especificacion; frontend permanece independiente.
- **Layered Architecture**: PASS WITH DESIGN ACTION. Los repositorios encapsularan acceso y mapeo de persistencia; el servicio coordinara scraping, validacion y persistencia sin exponer DTOs al controlador.
- **Rich Model**: PASS. Las anotaciones se agregan exclusivamente a entidades de persistencia; no se agregan anotaciones a modelos de dominio.
- **Layered Validation**: PASS. Se validaran identificadores, registros vacios y `numberOfPlayers`; la ausencia de un equipo confiable excluira el jugador.
- **Testing and Quality**: PASS WITH DESIGN ACTION. Se agregaran pruebas de entidades/repositorios, servicio y MockMvc, conservando las pruebas existentes y agregando el escenario real solicitado.
- **Spanish documentation**: PASS. Artefactos y mensajes nuevos se documentan en español; identificadores de codigo permanecen sin acentos.

## Project Structure

### Documentation (this feature)

```text
backend/specs/009-whoscored-team-player-migration/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/
   └── scrape.md
```

### Source Code

```text
backend/
├── app/src/main/java/com/dappunq/
│   ├── controller/
│   │   └── ScrapingControllerRest.java
│   ├── dto/whoscored/
│   │   ├── PlayerStatDTO.java
│   │   └── PlayerStatisticsResponse.java
│   ├── persistence/
│   │   ├── NewLeagueEntity.java
│   │   ├── TeamEntity.java
│   │   ├── WhoScoredEntity.java
│   │   └── *Repository.java
│   └── service/
│       ├── WhoScoredScrapingService.java
│       ├── PlayerCatalogRefreshService.java
│       └── FootballDataClient.java (deprecado, no usado)
└── app/src/test/java/com/dappunq/
    ├── e2e/
    ├── integration/
    └── unit/
```

**Structure Decision**: Se conserva la estructura existente de Spring Boot y se incorporan repositorios JPA junto a las tres entidades nuevas. Los contratos públicos se documentan dentro de la carpeta de la feature.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|---|---|---|
| Mantener `FootballDataClient` y DTOs heredados | RF-010 exige conservar archivos físicos para una migracion gradual | Eliminarlos impediria detectar referencias y migrar historicos posteriormente |
