# Implementation Plan: Valuacion de tokens de jugadores

**Branch**: `012-token-valuation` | **Date**: 2026-10-16 | **Spec**: `backend/specs/012-token-valuation/spec.md`

## Summary

Se incorporaran dos estrategias configurables por Usuario, con pesos no negativos que
suman 100%, normalizacion de metricas a `[0,1]`, redondeo monetario a dos decimales y
estado actual/pendiente. La Estrategia A usara exclusivamente `goals`, `assists`,
`passSuccessPercentage`, `aerialsWon` y `timesManOfTheMatch`; la Estrategia B usara
exclusivamente `passSuccessPercentage`, `aerialsWon`, `minutesPlayed`, `appearances` y
`position`, con `position` fija al 5% y valor 1 solo para `Defender`. La valuacion vigente
se persistira por Usuario y Jugador,
`TenenciaEntity` representara la cantidad agregada de tokens y cada ejecucion generara
un historial inmutable de los tokens poseidos. Un scheduler dominical
actualizara 50 jugadores mediante `WhoScoredScrapingService.scrape(String)` con `"50"` y, solo si el
snapshot es valido, aplicara estrategias pendientes y valuara todas las tenencias en una
transaccion idempotente.

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Spring Boot 4, Spring Data JPA, Spring Scheduling, PostgreSQL, Jackson, JUnit 5, Testcontainers

**Storage**: PostgreSQL mediante entidades JPA y repositorios Spring Data

**Testing**: JUnit 5, pruebas unitarias de dominio/normalizacion, integracion PostgreSQL con Testcontainers y E2E MockMvc

**Target Platform**: Servicio Spring Boot en JVM, local y CI con Chromium de Playwright disponible

**Project Type**: Web service REST dentro de un monorepo backend/frontend

**Performance Goals**: Completar una ventana con hasta 50 jugadores y todas las tenencias sin duplicar escrituras en reintentos

**Constraints**: Zona `America/Argentina/Buenos_Aires`; pesos suman exactamente 100 incluyendo el 5% fijo de `position` en B; valor con dos decimales; metrica faltante usa valor normalizado 0; no valuacion parcial tras scraping invalido; una pendiente por Usuario; sin mezclar transferencias con valuacion; `POST /quotes/recalculate` es publico y sin proteccion

**Scale/Scope**: 50 jugadores por actualizacion, 100 tokens por jugador, todos los Usuarios y sus tenencias; exactamente dos estrategias

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Monorepo**: PASS. La funcionalidad se implementa en `backend`; el frontend conserva su independencia.
- **Layered Architecture**: PASS WITH DESIGN ACTION. Los modelos de dominio contendran reglas; repositorios mapearan entidades; servicios orquestaran; controladores mapearan DTOs.
- **Rich Model**: PASS. Las entidades JPA estaran separadas de modelos sin anotaciones, y las invariantes de pesos, tokens y redondeo viviran en el dominio.
- **Layered Validation**: PASS. DTO valida formato, servicio resuelve Usuario/Jugador y el modelo valida pesos y estados.
- **Testing and Quality**: PASS WITH DESIGN ACTION. Se agregaran pruebas unitarias, integracion Testcontainers, E2E MockMvc y contrato OpenAPI; se actualizara Postman.
- **Spanish documentation**: PASS. Documentacion y mensajes nuevos en espanol; identificadores sin acentos ni `ñ`.
- **Public manual recalculation**: PASS WITH DESIGN ACTION. `POST /quotes/recalculate` se documentara como endpoint publico y reutilizara el mismo caso de uso atomico e idempotente del Scheduler.

**Re-evaluacion post-diseno**: PASS. `data-model.md` conserva las fronteras de dominio
y persistencia, `contracts/openapi.yaml` define las superficies REST, y `quickstart.md`
incluye validaciones de rollback, idempotencia, redondeo y scheduler sin introducir
violaciones constitucionales.

## Project Structure

### Documentation

```text
backend/specs/012-token-valuation/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/openapi.yaml
```

### Source Code

```text
backend/
├── app/src/main/java/com/dappunq/
│   ├── controller/       # endpoints y mapeo DTO
│   ├── dto/              # requests/responses de estrategia y tenencia
│   ├── model/            # reglas de dominio de estrategia y valuacion
│   ├── persistence/      # Tenencia, configuracion, valuacion, historial y repositorios JPA
│   ├── service/          # configuracion, inventario, valuacion y scheduler
│   └── config/           # scheduling y zona horaria
└── app/src/test/java/com/dappunq/
    ├── unit/
    ├── service/
    ├── integration/
    └── e2e/
```

**Structure Decision**: Se conserva el layout existente de Spring Boot. La tenencia
agregada se persiste en `TenenciaEntity` con `TenenciaRepository`; la valuacion vigente
se mantiene por Usuario/Jugador y cada calculo agrega un snapshot inmutable mediante un
repositorio de historial, con cantidad poseida y valor unitario individual. El inventario
inicial garantiza 100 tokens por jugador bajo el Superusuario.
`WhoScoredScrapingService` conservara su firma actual `scrape(String)`; el scheduler
invocara `scrape("50")`, manteniendo la validacion existente del parametro.

## Complexity Tracking

No se identifican violaciones constitucionales que requieran complejidad adicional.
