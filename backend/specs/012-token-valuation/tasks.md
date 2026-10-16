---

description: "Tareas de implementacion para la valuacion de tokens de jugadores"
---

# Tasks: Valuacion de tokens de jugadores

**Input**: Design documents from `/backend/specs/012-token-valuation/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/openapi.yaml, quickstart.md

**Tests**: Incluidos porque la especificacion exige pruebas unitarias, integracion con PostgreSQL/Testcontainers y E2E MockMvc.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar configuracion comun para persistencia, scheduling, documentacion y pruebas.

- [ ] T001 [P] Registrar dependencias de Spring Scheduling, Jackson y soporte de migraciones/persistencia requerido por la funcionalidad en `backend/app/build.gradle`
- [ ] T002 [P] Configurar zona horaria `America/Argentina/Buenos_Aires`, cron dominical y propiedades de valuacion en `backend/app/src/main/resources/application.properties`
- [ ] T003 [P] Configurar valores equivalentes para ejecucion local y pruebas en `backend/app/src/main/resources/application-local.properties`
- [ ] T004 [P] Incorporar los endpoints, esquemas y respuestas de la valuacion en `backend/app/src/main/resources/static/openapi.yaml`
- [ ] T005 [P] Agregar la coleccion de requests de estrategia, tenencias, historial y recalculo manual en `backend/postman/token-valuation.postman_collection.json`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Crear las fronteras de dominio, persistencia, errores y contexto de ejecucion que bloquean todas las historias.

**CRITICAL**: Ninguna historia puede comenzar hasta completar esta fase.

- [ ] T006 [P] Crear enums y value objects sin anotaciones para estrategias, metricas, estados de configuracion, tipo de ejecucion y resultado de valuacion en `backend/app/src/main/java/com/dappunq/model/valuation`
- [ ] T007 [P] Implementar excepciones de dominio para pesos invalidos, estrategia pendiente sin actual, jugador inexistente y ventana invalida en `backend/app/src/main/java/com/dappunq/model/valuation/ValuationDomainException.java`
- [ ] T008 [P] Crear entidades JPA y restricciones de esquema para configuraciones, tenencias, valuaciones e historial en `backend/app/src/main/java/com/dappunq/persistence/StrategyConfigEntity.java`, `backend/app/src/main/java/com/dappunq/persistence/HoldingEntity.java`, `backend/app/src/main/java/com/dappunq/persistence/ValuationEntity.java` y `backend/app/src/main/java/com/dappunq/persistence/ValuationHistoryEntity.java`
- [ ] T009 [P] Crear repositorios Spring Data y consultas por usuario, jugador, estado y ventana en `backend/app/src/main/java/com/dappunq/persistence/StrategyConfigRepository.java`, `backend/app/src/main/java/com/dappunq/persistence/HoldingRepository.java`, `backend/app/src/main/java/com/dappunq/persistence/ValuationRepository.java` y `backend/app/src/main/java/com/dappunq/persistence/ValuationHistoryRepository.java`
- [ ] T010 Implementar mappers entre modelos de dominio y entidades JPA, preservando la regla de que los servicios no realizan mapeo de persistencia, en `backend/app/src/main/java/com/dappunq/persistence/ValuationPersistenceMapper.java`
- [ ] T011 [P] Crear DTOs de requests/responses y validaciones de formato para estrategia, estado, tenencia, historial y resultado de recalculo en `backend/app/src/main/java/com/dappunq/dto/valuation`
- [ ] T012 Implementar manejo HTTP de errores de validacion, conflicto, usuario/jugador inexistente y fallo operativo sin ocultar excepciones en `backend/app/src/main/java/com/dappunq/controller/ValuationExceptionHandler.java`
- [ ] T013 [P] Preparar fixtures y configuracion de PostgreSQL/Testcontainers para las nuevas tablas y datos de jugadores/usuarios en `backend/app/src/test/java/com/dappunq/config/ValuationTestDataConfig.java`

**Checkpoint**: Las capas base y las restricciones de persistencia estan listas para implementar historias en paralelo.

---

## Phase 3: User Story 1 - Configurar una estrategia y valuar tokens (Priority: P1) 🎯 MVP

**Goal**: Permitir la primera configuracion valida de Estrategia A o B, calcular inmediatamente el valor por token y consultar tenencias e historial.

**Independent Test**: Crear un usuario con tenencias y jugadores, configurar pesos validos, verificar configuracion `ACTUAL`, un unico `valorUnitario` por usuario/jugador, redondeo a dos decimales y snapshot historico.

### Tests for User Story 1

- [ ] T014 [P] [US1] Probar invariantes de pesos, metricas requeridas, suma exacta 100 y catalogo de estrategias en `backend/app/src/test/java/com/dappunq/unit/StrategyConfigTest.java`
- [ ] T015 [P] [US1] Probar normalizacion `[0,1]`, metricas faltantes como cero, posicion Defender y redondeo monetario con `BigDecimal` en `backend/app/src/test/java/com/dappunq/unit/PlayerValuationCalculatorTest.java`
- [ ] T016 [P] [US1] Probar persistencia de configuracion, tenencia unica por usuario/jugador e historial inmutable con PostgreSQL/Testcontainers en `backend/app/src/test/java/com/dappunq/integration/ValuationPersistenceIntegrationTest.java`
- [ ] T017 [P] [US1] Probar configuracion inicial, consulta de estrategia, consulta de holdings y consulta de history mediante MockMvc en `backend/app/src/test/java/com/dappunq/e2e/ValuationConfigurationE2ETest.java`

### Implementation for User Story 1

- [ ] T018 [P] [US1] Implementar `StrategyConfig` y validacion de pesos por estrategia en `backend/app/src/main/java/com/dappunq/model/valuation/StrategyConfig.java`
- [ ] T019 [P] [US1] Implementar normalizacion y calculo determinista de cotizacion por jugador con valores faltantes en `backend/app/src/main/java/com/dappunq/model/valuation/PlayerValuationCalculator.java`
- [ ] T020 [US1] Implementar servicio de configuracion inicial y valuacion inmediata de todas las tenencias del usuario en `backend/app/src/main/java/com/dappunq/service/StrategyConfigurationService.java`
- [ ] T021 [US1] Implementar actualizacion atomica de `valorUnitario`, `valorTotal` e insercion de snapshots historicos sin modificar tokens en `backend/app/src/main/java/com/dappunq/service/HoldingsValuationService.java`
- [ ] T022 [US1] Implementar mapeos de estrategia, holdings, historial y resultados al contrato OpenAPI en `backend/app/src/main/java/com/dappunq/controller/ValuationDtoMapper.java`
- [ ] T023 [US1] Exponer PUT/GET `/users/{usuarioId}/strategy`, GET `/users/{usuarioId}/holdings` y GET `/users/{usuarioId}/holdings/history` con autorizacion reutilizando el contexto de usuario existente en `backend/app/src/main/java/com/dappunq/controller/ValuationControllerRest.java`

**Checkpoint**: La primera configuracion valida produce valuacion y el usuario puede consultar valores e historial sin depender del scheduler.

---

## Phase 4: User Story 2 - Actualizar la estrategia para la proxima valuacion (Priority: P1)

**Goal**: Registrar exactamente una configuracion pendiente, reemplazarla al modificarla y aplicarla solo en la siguiente ventana.

**Independent Test**: Con una estrategia actual, enviar dos configuraciones validas y verificar que solo existe la segunda como `PENDIENTE`, que el valor actual no cambia antes de la ventana y que una configuracion sin actual devuelve conflicto.

### Tests for User Story 2

- [ ] T024 [P] [US2] Probar reemplazo de pendiente, rechazo sin estrategia actual y conservacion de la configuracion actual ante pesos invalidos en `backend/app/src/test/java/com/dappunq/service/StrategyConfigurationServiceTest.java`
- [ ] T025 [P] [US2] Probar restricciones de una pendiente por usuario y fechas de vigencia con PostgreSQL/Testcontainers en `backend/app/src/test/java/com/dappunq/integration/PendingStrategyIntegrationTest.java`
- [ ] T026 [P] [US2] Probar respuestas 400/409 y estado `PENDIENTE` del endpoint de estrategia con MockMvc en `backend/app/src/test/java/com/dappunq/e2e/PendingStrategyE2ETest.java`

### Implementation for User Story 2

- [ ] T027 [US2] Extender `StrategyConfigurationService` para distinguir primera configuracion de cambios posteriores y exigir estrategia actual antes de crear pendiente en `backend/app/src/main/java/com/dappunq/service/StrategyConfigurationService.java`
- [ ] T028 [US2] Implementar reemplazo transaccional de la pendiente anterior y consulta conjunta de configuracion actual/pendiente en `backend/app/src/main/java/com/dappunq/persistence/StrategyConfigRepository.java`
- [ ] T029 [US2] Completar DTOs y mapeos para exponer `ACTUAL`, `PENDIENTE`, `SIN_ESTRATEGIA` y `vigenteDesde` en `backend/app/src/main/java/com/dappunq/dto/valuation/StrategyStateResponse.java` y `backend/app/src/main/java/com/dappunq/controller/ValuationDtoMapper.java`
- [ ] T030 [US2] Cubrir en el controlador el flujo autenticado de modificacion de estrategia sin recalcular antes de la ventana en `backend/app/src/main/java/com/dappunq/controller/ValuationControllerRest.java`

**Checkpoint**: Los cambios posteriores quedan aislados como pendiente y no alteran cotizaciones vigentes.

---

## Phase 5: User Story 3 - Ejecutar la valuacion semanal de todos los usuarios (Priority: P1)

**Goal**: Actualizar 50 jugadores, aplicar pendientes y valuar todas las tenencias en una ventana dominical idempotente, con recalculo manual publico.

**Independent Test**: Ejecutar una ventana con reloj controlado, verificar `scrape("50")` antes del calculo, aplicar pendientes, generar historial, no duplicar datos al reintentar y conservar la ultima valuacion ante scraping incompleto.

### Tests for User Story 3

- [ ] T031 [P] [US3] Probar orden scrape/calculo, ventana dominical, usuarios sin estrategia, idempotencia y rollback ante scrape fallido con mocks en `backend/app/src/test/java/com/dappunq/service/WeeklyValuationServiceTest.java`
- [ ] T032 [P] [US3] Probar claves unicas de ventana, aplicacion de pendiente y snapshots por usuario con PostgreSQL/Testcontainers en `backend/app/src/test/java/com/dappunq/integration/WeeklyValuationIntegrationTest.java`
- [ ] T033 [P] [US3] Probar ejecucion publica sin Authorization, respuesta 200/503 y contrato del resultado en `backend/app/src/test/java/com/dappunq/e2e/QuoteRecalculateE2ETest.java`
- [ ] T034 [P] [US3] Probar invocacion del scheduler con zona y cron configurables en `backend/app/src/test/java/com/dappunq/integration/ValuationSchedulerTest.java`

### Implementation for User Story 3

- [ ] T035 [US3] Implementar servicio transaccional que invoque `WhoScoredScrapingService.scrape("50")`, valide snapshot completo y no continue con datos parciales en `backend/app/src/main/java/com/dappunq/service/WeeklyValuationService.java`
- [ ] T036 [US3] Implementar aplicacion de configuraciones pendientes y valuacion de usuarios con estrategia actual, dejando explicitamente sin valuacion a usuarios sin estrategia en `backend/app/src/main/java/com/dappunq/service/WeeklyValuationService.java`
- [ ] T037 [US3] Implementar identificador de ventana, tipo de ejecucion, restricciones unicas e idempotencia de snapshots en `backend/app/src/main/java/com/dappunq/persistence/ValuationRepository.java` y `backend/app/src/main/java/com/dappunq/persistence/ValuationHistoryRepository.java`
- [ ] T038 [US3] Crear scheduler dominical a las 03:00 con zona `America/Argentina/Buenos_Aires` que delegue en el servicio comun en `backend/app/src/main/java/com/dappunq/config/ValuationScheduler.java`
- [ ] T039 [US3] Exponer `POST /quotes/recalculate` sin autenticacion y mapear resultado operativo o error 503 sin duplicar el flujo de calculo en `backend/app/src/main/java/com/dappunq/controller/QuoteControllerRest.java` y `backend/app/src/main/java/com/dappunq/config/SecurityConfig.java`
- [ ] T040 [US3] Verificar que la respuesta y el error del recalculo manual actualicen la especificacion OpenAPI publicada en `backend/app/src/main/resources/static/openapi.yaml`

**Checkpoint**: La ventana semanal y el endpoint manual comparten un flujo atomico, ordenado, observable e idempotente.

---

## Phase 6: User Story 4 - Administrar la cantidad y tenencia inicial de tokens (Priority: P2)

**Goal**: Crear y mantener exactamente 100 tokens agregados por jugador bajo la tenencia del Superusuario, sin alterarlos durante valuaciones.

**Independent Test**: Inicializar inventario para todos los jugadores, verificar 100 tokens por jugador bajo el Superusuario, unicidad de tenencia y cantidad constante tras una o varias valuaciones.

### Tests for User Story 4

- [ ] T041 [P] [US4] Probar inicializacion idempotente de 100 tokens por jugador y validacion de Superusuario unico en `backend/app/src/test/java/com/dappunq/service/InitialHoldingsServiceTest.java`
- [ ] T042 [P] [US4] Probar restricciones de tenencia, FK, unicidad usuario/jugador y valores total/unitario con PostgreSQL/Testcontainers en `backend/app/src/test/java/com/dappunq/integration/InitialHoldingsIntegrationTest.java`
- [ ] T043 [P] [US4] Probar que la valuacion no crea ni elimina tokens y mantiene `valorTotal = tokens * valorUnitario` en `backend/app/src/test/java/com/dappunq/e2e/HoldingsInvariantE2ETest.java`

### Implementation for User Story 4

- [ ] T044 [US4] Implementar servicio de inicializacion idempotente de tenencias del Superusuario con 100 tokens por jugador en `backend/app/src/main/java/com/dappunq/service/InitialHoldingsService.java`
- [ ] T045 [US4] Integrar la creacion de tenencia inicial al alta/actualizacion de jugadores sin duplicar filas existentes en `backend/app/src/main/java/com/dappunq/service/PlayerCatalogReadService.java`
- [ ] T046 [US4] Agregar validaciones de invariantes de cantidad y formula de valor en `backend/app/src/main/java/com/dappunq/model/valuation/Holding.java` y `backend/app/src/main/java/com/dappunq/persistence/HoldingEntity.java`
- [ ] T047 [US4] Configurar inicializacion de estrategia A con pesos 30/25/15/15/15 para el Superusuario en `backend/app/src/main/java/com/dappunq/service/SuperuserInitializationService.java`

**Checkpoint**: Todo jugador tiene inventario inicial exacto y las valuaciones solo modifican valores, nunca cantidades.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Validar la entrega completa y mantener documentación, calidad y compatibilidad.

- [ ] T048 [P] Actualizar la documentacion de endpoints, estados y errores en `backend/specs/012-token-valuation/contracts/openapi.yaml` y `backend/app/src/main/resources/static/openapi.yaml`
- [ ] T049 [P] Documentar los escenarios de configuracion, pendientes, rollback, idempotencia y recalculo manual en `backend/specs/012-token-valuation/quickstart.md`
- [ ] T050 Ejecutar `backend/gradlew.bat :app:test` y corregir fallos de compilacion, unitarios, Testcontainers y MockMvc sin modificar ni eliminar pruebas existentes
- [ ] T051 Ejecutar una validacion completa de la coleccion Postman y confirmar que todos los endpoints agregados estan documentados en `backend/postman/token-valuation.postman_collection.json`
- [ ] T052 Revisar separacion Model/Service/Controller/Persistence, mensajes en espanol, identificadores sin acentos y logs de errores operativos en los archivos modificados de `backend/app/src/main/java/com/dappunq`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No depende de otras fases; T001-T005 pueden ejecutarse en paralelo.
- **Foundational (Phase 2)**: Depende de Phase 1 y bloquea todas las historias; T006-T009, T011 y T013 pueden ejecutarse en paralelo, mientras T010 depende de T008-T009 y T012 depende de T011.
- **User Stories (Phase 3-6)**: Dependen de Phase 2. US1 es el MVP; US2 depende de la estructura de configuracion de US1, US3 depende de valuacion/tenencias de US1 y US2, y US4 puede implementarse en paralelo con US1 despues de Foundation.
- **Polish (Phase 7)**: Depende de las historias seleccionadas; T048-T049 pueden ejecutarse en paralelo y T050-T052 se ejecutan al final.

### User Story Dependencies

- **US1 (P1)**: Depende de Foundation; no depende de otra historia y constituye el MVP.
- **US2 (P1)**: Depende de US1 para reutilizar configuracion, consulta y valuacion inmediata; sus pruebas deben seguir siendo independientes con fixtures propios.
- **US3 (P1)**: Depende de US1 para recalcular tenencias y de US2 para aplicar pendientes; scheduler y recalculo manual comparten el mismo caso de uso.
- **US4 (P2)**: Depende de Foundation y puede ejecutarse en paralelo con US1; US1/US3 consumen sus tenencias iniciales.

### Within Each User Story

- Las pruebas se escriben antes de la implementacion y deben fallar por la funcionalidad ausente.
- Modelos e invariantes preceden a servicios; servicios preceden a controladores y scheduler.
- Persistencia debe mapear entidades a modelos sin trasladar reglas de dominio al Service.
- Cada checkpoint debe validarse antes de avanzar a la siguiente historia.

### Parallel Opportunities

- Phase 1: T001-T005 son independientes por archivo/configuracion.
- Phase 2: T006-T009, T011 y T013 son paralelizables; T010 y T012 esperan sus contratos.
- US1: T014-T017 y T018-T019 son paralelizables.
- US2: T024-T026 son paralelizables; T027-T030 requieren la base de US1.
- US3: T031-T034 son paralelizables; T035-T036 forman la orquestacion secuencial.
- US4: T041-T043 son paralelizables; T044-T047 requieren Holding/Player disponibles.
- US1 y US4 pueden trabajarse en paralelo tras Foundation si se coordinan los cambios compartidos de Player.

---

## Parallel Example: User Story 1

```text
Task: "T014 [US1] Probar invariantes de StrategyConfig"
Task: "T015 [US1] Probar normalizacion y redondeo"
Task: "T016 [US1] Probar persistencia de configuraciones y tenencias"
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

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Completar Setup y Foundation.
2. Implementar US1 con configuracion inicial, calculador, holdings, historial y endpoints autenticados.
3. Ejecutar T014-T017 y validar el checkpoint de US1.
4. Detenerse para demostrar que un usuario puede configurar y consultar una valuacion completa.

### Incremental Delivery

1. Agregar US2 para cambios pendientes sin alterar valores vigentes.
2. Agregar US3 para ventanas dominicales y recalculo manual publico idempotente.
3. Agregar US4 para inventario inicial del Superusuario y asegurar exactamente 100 tokens por jugador.
4. Ejecutar Polish y la suite completa antes de entregar.

### Parallel Team Strategy

1. Un equipo completa Setup y Foundation.
2. Luego un desarrollador implementa US1, otro prepara US4 y un tercero puede preparar pruebas/infraestructura de US3.
3. US2 se integra sobre US1 y US3 se integra cuando estan disponibles configuracion, tenencias y valuacion.

---

## Notes

- Todas las tareas usan checkbox, ID secuencial y rutas concretas; las tareas de historias incluyen la etiqueta `[USn]`.
- `[P]` indica archivos diferentes y ausencia de dependencia sobre una tarea incompleta.
- Los tests de cada historia verifican su criterio independiente y usan las capas exigidas por la constitucion.
- No se incluyen transferencias, compras ni ventas: permanecen fuera de alcance.
