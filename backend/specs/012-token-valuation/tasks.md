---

description: "Tareas de implementacion para la valuacion de tokens de jugadores"
---

# Tasks: Valuacion de tokens de jugadores

**Input**: Design documents from `/backend/specs/012-token-valuation/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/openapi.yaml`, `quickstart.md`

**Tests**: Obligatorios por la especificacion: unitarios, integracion PostgreSQL/Testcontainers y E2E MockMvc.

## Phase 1: Setup

**Purpose**: Preparar dependencias, configuracion y contratos compartidos.

- [ ] T001 [P] Registrar dependencias de scheduling, Jackson, persistencia y Testcontainers en `backend/app/build.gradle`
- [ ] T002 [P] Configurar zona `America/Argentina/Buenos_Aires`, cron dominical y propiedades de valuacion en `backend/app/src/main/resources/application.properties`
- [ ] T003 [P] Configurar overrides locales y de prueba para la ventana semanal en `backend/app/src/main/resources/application-local.properties`
- [ ] T004 [P] Publicar el contrato de endpoints, esquemas y errores en `backend/app/src/main/resources/static/openapi.yaml`
- [ ] T005 [P] Agregar requests autenticados y publico de la feature en `backend/postman/token-valuation.postman_collection.json`

## Phase 2: Foundational

**Purpose**: Crear las fronteras de dominio y persistencia que bloquean todas las historias.

**CRITICAL**: Ninguna historia puede comenzar hasta completar esta fase.

- [ ] T006 [P] Crear enums para estrategias, metricas, estados, tipo de ejecucion y estado de valuacion en `backend/app/src/main/java/com/dappunq/model/valuation`
- [ ] T007 [P] Implementar errores de dominio para pesos invalidos, estrategia pendiente sin actual, ventana invalida y fallo de snapshot en `backend/app/src/main/java/com/dappunq/model/valuation/ValuationDomainException.java`
- [ ] T008 [P] Crear entidades JPA `StrategyConfigEntity`, `HoldingEntity`, `ValuationEntity` y `ValuationHistoryEntity` con FK, restricciones y claves unicas en `backend/app/src/main/java/com/dappunq/persistence`
- [ ] T009 [P] Crear repositorios para configuraciones, tenencias, valuaciones e historial con consultas por usuario, estado, jugador y ventana en `backend/app/src/main/java/com/dappunq/persistence`
- [ ] T010 Implementar mapeos entre modelos de dominio y entidades JPA sin trasladar reglas de negocio al servicio en `backend/app/src/main/java/com/dappunq/persistence/ValuationPersistenceMapper.java`
- [ ] T011 [P] Crear DTOs y validaciones de requests/responses de estrategia, tenencia, historial y recalculo en `backend/app/src/main/java/com/dappunq/dto/valuation`
- [ ] T012 Implementar manejo HTTP explícito para validacion, conflicto, recurso inexistente y fallo operativo en `backend/app/src/main/java/com/dappunq/controller/ValuationExceptionHandler.java`
- [ ] T013 [P] Preparar fixtures y configuracion Testcontainers para usuarios, jugadores, tenencias y metricas en `backend/app/src/test/java/com/dappunq/config/ValuationTestDataConfig.java`

## Phase 3: User Story 1 - Configurar una estrategia y valuar tokens (Priority: P1) 🎯 MVP

**Goal**: Configurar por primera vez Estrategia A o B, calcular inmediatamente y consultar tenencias e historial.

**Independent Test**: Con un usuario que posee tokens, enviar pesos validos, comprobar estado `ACTUAL`, un unico valor por usuario/jugador, redondeo a dos decimales y snapshot historico.

### Tests for User Story 1

- [ ] T014 [P] [US1] Probar catalogo cerrado, metricas requeridas, pesos no negativos y suma exacta de cada estrategia en `backend/app/src/test/java/com/dappunq/unit/StrategyConfigTest.java`
- [ ] T015 [P] [US1] Probar normalizacion `[0,1]`, metricas faltantes como cero, `Defender` y redondeo monetario en `backend/app/src/test/java/com/dappunq/unit/PlayerValuationCalculatorTest.java`
- [ ] T016 [P] [US1] Probar persistencia de configuracion, unicidad de tenencia y snapshots inmutables con PostgreSQL en `backend/app/src/test/java/com/dappunq/integration/ValuationPersistenceIntegrationTest.java`
- [ ] T017 [P] [US1] Probar PUT/GET de estrategia y GET de tenencias e historial con MockMvc en `backend/app/src/test/java/com/dappunq/e2e/ValuationConfigurationE2ETest.java`

### Implementation for User Story 1

- [ ] T018 [P] [US1] Implementar `StrategyConfig` con validacion de pesos de A y B, incluyendo el 5% fijo de `position`, en `backend/app/src/main/java/com/dappunq/model/valuation/StrategyConfig.java`
- [ ] T019 [P] [US1] Implementar calculo determinista con min/max del snapshot, valores faltantes en cero, regla de posicion y `BigDecimal` en `backend/app/src/main/java/com/dappunq/model/valuation/PlayerValuationCalculator.java`
- [ ] T020 [US1] Implementar configuracion inicial como `ACTUAL` y valuacion inmediata de las tenencias del usuario en `backend/app/src/main/java/com/dappunq/service/StrategyConfigurationService.java`
- [ ] T021 [US1] Implementar actualizacion atomica de `valorUnitario`, `valorTotal` e historial sin modificar `tokens` en `backend/app/src/main/java/com/dappunq/service/HoldingsValuationService.java`
- [ ] T022 [US1] Implementar mapeo de estados, configuraciones, holdings, historial y resultado al contrato REST en `backend/app/src/main/java/com/dappunq/controller/ValuationDtoMapper.java`
- [ ] T023 [US1] Exponer los endpoints autenticados de estrategia, tenencias e historial reutilizando la autenticacion existente en `backend/app/src/main/java/com/dappunq/controller/ValuationControllerRest.java`

## Phase 4: User Story 2 - Actualizar la estrategia para la proxima valuacion (Priority: P1)

**Goal**: Mantener una sola configuracion pendiente, reemplazarla y aplicarla solo en la proxima ventana dominical.

**Independent Test**: Con una estrategia actual, confirmar dos cambios, comprobar que solo el segundo queda `PENDIENTE`, que el valor actual no cambia y que sin estrategia actual se devuelve conflicto.

### Tests for User Story 2

- [ ] T024 [P] [US2] Probar reemplazo de pendiente, rechazo sin actual y conservacion ante pesos invalidos en `backend/app/src/test/java/com/dappunq/service/StrategyConfigurationServiceTest.java`
- [ ] T025 [P] [US2] Probar unicidad de pendiente y fechas de vigencia con PostgreSQL/Testcontainers en `backend/app/src/test/java/com/dappunq/integration/PendingStrategyIntegrationTest.java`
- [ ] T026 [P] [US2] Probar respuestas 400/409 y estado `PENDIENTE` mediante MockMvc en `backend/app/src/test/java/com/dappunq/e2e/PendingStrategyE2ETest.java`

### Implementation for User Story 2

- [ ] T027 [US2] Extender `StrategyConfigurationService` para distinguir primera configuracion de cambios posteriores y exigir estrategia actual antes de crear pendiente en `backend/app/src/main/java/com/dappunq/service/StrategyConfigurationService.java`
- [ ] T028 [US2] Implementar reemplazo transaccional y consultas de configuracion actual/pendiente en `backend/app/src/main/java/com/dappunq/persistence/StrategyConfigRepository.java`
- [ ] T029 [US2] Exponer `ACTUAL`, `PENDIENTE`, `SIN_ESTRATEGIA` y `vigenteDesde` en `backend/app/src/main/java/com/dappunq/dto/valuation/StrategyStateResponse.java`
- [ ] T030 [US2] Asegurar que las modificaciones posteriores no invoquen valuacion inmediata en `backend/app/src/main/java/com/dappunq/controller/ValuationControllerRest.java`

## Phase 5: User Story 3 - Ejecutar la valuacion semanal de todos los usuarios (Priority: P1)

**Goal**: Actualizar jugadores, aplicar pendientes y valuar usuarios cada domingo; permitir el mismo flujo mediante POST publico.

**Independent Test**: Con reloj controlado, verificar `scrape("50")` antes del calculo, aplicacion de pendientes, snapshots, idempotencia, usuarios sin estrategia y rollback ante scrape fallido.

### Tests for User Story 3

- [ ] T031 [P] [US3] Probar orden scrape/calculo, usuarios sin estrategia y rollback ante fallo con mocks en `backend/app/src/test/java/com/dappunq/service/WeeklyValuationServiceTest.java`
- [ ] T032 [P] [US3] Probar claves unicas de ventana, aplicacion de pendiente y snapshots por usuario en `backend/app/src/test/java/com/dappunq/integration/WeeklyValuationIntegrationTest.java`
- [ ] T033 [P] [US3] Probar POST publico sin Authorization y respuestas 200/503 con MockMvc en `backend/app/src/test/java/com/dappunq/e2e/QuoteRecalculateE2ETest.java`
- [ ] T034 [P] [US3] Probar cron, zona horaria y delegacion al caso de uso comun en `backend/app/src/test/java/com/dappunq/integration/ValuationSchedulerTest.java`

### Implementation for User Story 3

- [ ] T035 [US3] Implementar servicio transaccional que ejecute `WhoScoredScrapingService.scrape("50")` y rechace snapshots incompletos en `backend/app/src/main/java/com/dappunq/service/WeeklyValuationService.java`
- [ ] T036 [US3] Aplicar pendientes antes de recalcular, omitir usuarios sin estrategia y preservar la ultima valuacion valida en `backend/app/src/main/java/com/dappunq/service/WeeklyValuationService.java`
- [ ] T037 [US3] Implementar identificador de ventana, tipo de ejecucion, restricciones unicas e idempotencia en `backend/app/src/main/java/com/dappunq/persistence/ValuationRepository.java` y `backend/app/src/main/java/com/dappunq/persistence/ValuationHistoryRepository.java`
- [ ] T038 [US3] Crear scheduler dominical a las 03:00 con zona de negocio en `backend/app/src/main/java/com/dappunq/config/ValuationScheduler.java`
- [ ] T039 [US3] Exponer `POST /quotes/recalculate` sin autenticacion y mapear exito o error 503 al flujo comun en `backend/app/src/main/java/com/dappunq/controller/QuoteControllerRest.java` y `backend/app/src/main/java/com/dappunq/config/SecurityConfig.java`
- [ ] T040 [US3] Mantener sincronizados el contrato publicado y el contrato de feature para recalculo, errores e idempotencia en `backend/app/src/main/resources/static/openapi.yaml` y `backend/specs/012-token-valuation/contracts/openapi.yaml`

## Phase 6: User Story 4 - Administrar la cantidad y tenencia inicial de tokens (Priority: P2)

**Goal**: Garantizar exactamente 100 tokens agregados por jugador bajo el Superusuario, sin alterar cantidades al valuar.

**Independent Test**: Inicializar jugadores, comprobar una tenencia de 100 por jugador bajo el Superusuario y verificar cantidad constante tras recalculos repetidos.

### Tests for User Story 4

- [ ] T041 [P] [US4] Probar inicializacion idempotente, Superusuario unico y 100 tokens por jugador en `backend/app/src/test/java/com/dappunq/service/InitialHoldingsServiceTest.java`
- [ ] T042 [P] [US4] Probar FK, unicidad usuario/jugador y formula `valorTotal = tokens * valorUnitario` en `backend/app/src/test/java/com/dappunq/integration/InitialHoldingsIntegrationTest.java`
- [ ] T043 [P] [US4] Probar que recalcular no crea/elimina tokens y conserva invariantes mediante MockMvc en `backend/app/src/test/java/com/dappunq/e2e/HoldingsInvariantE2ETest.java`

### Implementation for User Story 4

- [ ] T044 [US4] Implementar inicializacion idempotente de tenencias del Superusuario con 100 tokens por jugador en `backend/app/src/main/java/com/dappunq/service/InitialHoldingsService.java`
- [ ] T045 [US4] Integrar la inicializacion al alta o sincronizacion de jugadores sin duplicar tenencias en `backend/app/src/main/java/com/dappunq/service/PlayerCatalogReadService.java`
- [ ] T046 [US4] Implementar invariantes de cantidad y formula de valor en `backend/app/src/main/java/com/dappunq/model/valuation/Holding.java` y `backend/app/src/main/java/com/dappunq/persistence/HoldingEntity.java`
- [ ] T047 [US4] Inicializar la estrategia A del Superusuario con pesos 30/25/15/15/15 en `backend/app/src/main/java/com/dappunq/service/SuperuserInitializationService.java`

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Validar la feature completa y conservar documentación y arquitectura.

- [ ] T048 [P] Actualizar escenarios de rollback, idempotencia, pendientes y recalculo manual en `backend/specs/012-token-valuation/quickstart.md`
- [ ] T049 [P] Actualizar la coleccion Postman con ejemplos de errores y respuestas de todos los endpoints en `backend/postman/token-valuation.postman_collection.json`
- [ ] T050 Ejecutar `backend/gradlew.bat :app:test` y corregir fallos de compilacion, unitarios, Testcontainers y MockMvc en `backend/`
- [ ] T051 Revisar separacion Model/Service/Controller/Persistence, logs de errores operativos y mensajes en espanol en `backend/app/src/main/java/com/dappunq`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup**: T001-T005 no dependen de otras fases y pueden ejecutarse en paralelo.
- **Foundational**: depende de Setup; T006-T009, T011 y T013 pueden ejecutarse en paralelo. T010 depende de T008-T009 y T012 depende de T011.
- **User Stories**: todas dependen de Foundational. US1 es el MVP; US2 depende de la configuracion de US1; US3 depende de US1 y US2; US4 depende de Foundational y puede avanzar en paralelo con US1.
- **Polish**: depende de las historias seleccionadas; T048-T049 pueden ejecutarse en paralelo antes de T050-T051.

### User Story Dependencies

- **US1 (P1)**: Foundation solamente; primera configuracion y valuacion consultable.
- **US2 (P1)**: US1, porque reutiliza configuracion, estados y valuacion vigente.
- **US3 (P1)**: US1 y US2, porque recalcula tenencias y aplica pendientes.
- **US4 (P2)**: Foundation; sus tenencias son consumidas por US1 y US3.

### Parallel Opportunities

- Setup: T001-T005.
- Foundation: T006-T009, T011 y T013.
- US1: T014-T019.
- US2: T024-T026.
- US3: T031-T034.
- US4: T041-T043.
- US1 y US4 pueden implementarse simultaneamente tras Foundation si se coordinan cambios sobre tenencias y jugadores.

## Parallel Example: User Story 1

```text
Task: "T014 [US1] Probar invariantes de pesos"
Task: "T015 [US1] Probar normalizacion y redondeo"
Task: "T016 [US1] Probar persistencia"
Task: "T017 [US1] Probar endpoints MockMvc"
Task: "T018 [US1] Implementar StrategyConfig"
Task: "T019 [US1] Implementar PlayerValuationCalculator"
```

## Parallel Example: User Story 3

```text
Task: "T031 [US3] Probar servicio semanal"
Task: "T032 [US3] Probar idempotencia PostgreSQL"
Task: "T033 [US3] Probar POST /quotes/recalculate"
Task: "T034 [US3] Probar scheduler"
```

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Completar Setup y Foundational.
2. Implementar US1: configuracion inicial, calculador, tenencias, historial y endpoints autenticados.
3. Ejecutar T014-T017 y validar el criterio independiente de US1.
4. Detenerse para demostrar una valuacion personalizada consultable.

### Incremental Delivery

1. Agregar US2 para cambios pendientes sin alterar valores vigentes.
2. Agregar US3 para ventana dominical, rollback y recalculo manual publico.
3. Agregar US4 para inventario inicial exacto del Superusuario.
4. Ejecutar Polish y la suite completa.

## Notes

- Todas las tareas usan checkbox, ID secuencial y ruta concreta.
- Las tareas de historias incluyen exactamente una etiqueta `[USn]`; `[P]` solo indica trabajo paralelizable.
- Las pruebas se escriben antes de la implementacion y deben fallar por la funcionalidad ausente.
- Transferencias, compras y ventas quedan fuera de alcance.
