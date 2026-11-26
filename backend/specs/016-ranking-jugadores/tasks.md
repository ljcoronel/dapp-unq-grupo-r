# Tasks: Ranking global de jugadores

**Input**: Documentos de diseño desde `backend/specs/016-ranking-jugadores/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `quickstart.md`, `docs/openapi.yaml`

**Tests**: La constitución exige pruebas felices, bordes, integración con PostgreSQL/Testcontainers y E2E con MockMvc.

**Organization**: Las tareas se agrupan por historia de usuario para habilitar implementación y validación independiente.

## Formato: `[ID] [P?] [Story] Descripcion`

- **[P]**: Puede ejecutarse en paralelo (archivos distintos y sin dependencia de tareas incompletas).
- **[Story]**: Historia a la que pertenece la tarea (US1, US2 o US3).
- Las tareas incluyen rutas exactas dentro del repositorio.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirmar la línea base existente antes de incorporar el ranking.

- [ ] T001 Ejecutar `backend\gradlew.bat test` y documentar el estado inicial de compilación y pruebas en `backend/specs/016-ranking-jugadores/tasks.md`.
- [ ] T002 Revisar y conservar los patrones existentes de capas en `backend/app/src/main/java/com/dappunq/controller/`, `service/`, `model/`, `persistence/` y `dto/`.
- [ ] T003 [P] Revisar `GET /players/ranking` y conservar el contrato existente de `POST /quotes/scrape` en `docs/openapi.yaml`, sin crear un OpenAPI duplicado de feature.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Crear los datos y componentes compartidos que bloquean todas las historias de usuario.

**CRITICAL**: Ninguna historia puede comenzar hasta completar esta fase.

- [ ] T004 Confirmar que el ranking lee exclusivamente jugadores ya persistidos y no realiza altas ni escrituras de entidades de jugador en `backend/app/src/main/java/com/dappunq/adapter/RankingRepositoryAdapter.java`.
- [ ] T005 Crear o actualizar solo los índices de ranking requeridos en `backend/app/src/main/resources/schema.sql`; no agregar columnas de estado no soportadas por el modelo actual.
- [ ] T006 [P] Implementar el modelo de dominio `RankingFiltros` con validación de filtros, página y límite en `backend/app/src/main/java/com/dappunq/model/ranking/RankingFiltros.java`.
- [ ] T007 [P] Implementar el modelo de dominio `RankingJugador` con posición, datos del jugador, valor, origen y reglas de invariante en `backend/app/src/main/java/com/dappunq/model/ranking/RankingJugador.java`.
- [ ] T008 [P] Implementar el modelo de dominio `RankingPagina` con items, total, página y límite en `backend/app/src/main/java/com/dappunq/model/ranking/RankingPagina.java`.
- [ ] T009 Crear el contrato interno `RankingRepository` en `backend/app/src/main/java/com/dappunq/repository/RankingRepository.java`.
- [ ] T010 Implementar `RankingRepositoryAdapter` con JdbcTemplate y CTE PostgreSQL en `backend/app/src/main/java/com/dappunq/adapter/RankingRepositoryAdapter.java`.
- [ ] T011 [P] Crear los DTO de entrada/salida del ranking en `backend/app/src/main/java/com/dappunq/dto/ranking/RankingPlayerResponse.java` y `backend/app/src/main/java/com/dappunq/dto/ranking/RankingPageResponse.java`.
- [ ] T012 Crear el servicio de ranking en `backend/app/src/main/java/com/dappunq/service/RankingService.java` y `backend/app/src/main/java/com/dappunq/service/RankingServiceImpl.java`.
- [ ] T013 Registrar la configuración necesaria para que Spring resuelva JdbcTemplate, repositorio y servicio del ranking en `backend/app/src/main/java/com/dappunq/`.

**Checkpoint**: La infraestructura y los modelos compartidos están listos; el ranking puede implementarse por historias.

---

## Phase 3: User Story 1 - Consultar el ranking global (Priority: P1) 🎯 MVP

**Goal**: Devolver una fila por jugador activo con el máximo vigente, origen correcto, fallback y los desempates definidos.

**Independent Test**: Consultar el ranking completo con varias cotizaciones vigentes, jugadores sin cotización personalizada y valores empatados; recorrer todas las páginas y verificar unicidad, cobertura, máximo, origen y orden.

### Tests for User Story 1

- [ ] T014 [P] [US1] Agregar unitarias de invariantes de `RankingFiltros`, `RankingJugador` y `RankingPagina` en `backend/app/src/test/java/com/dappunq/unit/RankingDomainTest.java`.
- [ ] T015 [P] [US1] Agregar pruebas unitarias del servicio y los desempates sin persistencia en `backend/app/src/test/java/com/dappunq/service/RankingServiceImplTest.java`.
- [ ] T016 [P] [US1] Agregar integración Testcontainers del Adapter JDBC con datos vigentes, históricas y tenencias en `backend/app/src/test/java/com/dappunq/integration/RankingRepositoryAdapterIntegrationTest.java`.
- [ ] T017 [P] [US1] Agregar contrato y E2E MockMvc de `GET /players/ranking` para 200, 400 y 500; el E2E debe llamar primero `POST /quotes/scrape?numberOfPlayers=N` y usar un doble determinista de `ScrapingService` para persistir fixtures sin red en `backend/app/src/test/java/com/dappunq/e2e/RankingControllerE2ETest.java`, y validar `docs/openapi.yaml`.

### Implementation for User Story 1

- [ ] T018 [P] [US1] Limitar el contrato de lectura del ranking a consultas sobre registros persistidos y asegurar que no incluya operaciones de alta/modificación de jugadores en `backend/app/src/main/java/com/dappunq/repository/RankingRepository.java`.
- [ ] T019 [P] [US1] Verificar en E2E que invocar `GET /players/ranking` no inserta ni modifica jugadores fuera del endpoint de scraping en `backend/app/src/test/java/com/dappunq/e2e/RankingControllerE2ETest.java`.
- [ ] T020 [US1] Completar la consulta consolidada del catálogo persistido de las cinco ligas y cotizaciones vigentes en `backend/app/src/main/java/com/dappunq/adapter/RankingRepositoryAdapter.java`.
- [ ] T021 [US1] Implementar la selección del máximo vigente y el origen determinista Estrategia A/B o Cotización base en `backend/app/src/main/java/com/dappunq/adapter/RankingRepositoryAdapter.java`.
- [ ] T022 [US1] Implementar el fallback de cotización base del superusuario o 1 crédito en `backend/app/src/main/java/com/dappunq/adapter/RankingRepositoryAdapter.java`.
- [ ] T023 [US1] Implementar el orden por valor, base, tenencias actuales de inversores, nombre e identificador estable en `backend/app/src/main/java/com/dappunq/adapter/RankingRepositoryAdapter.java`.
- [ ] T024 [US1] Exponer `GET /players/ranking` y mapear modelos a DTO sin mezclar persistencia en `backend/app/src/main/java/com/dappunq/controller/PlayerControllerRest.java` y `backend/app/src/main/java/com/dappunq/controller/mapper/RankingDtoMapper.java`.

**Checkpoint**: User Story 1 está completa y puede demostrarse de forma independiente como MVP.

---

## Phase 4: User Story 2 - Explorar por filtros y páginas (Priority: P2)

**Goal**: Permitir filtros individuales o combinados, paginación estable y posiciones continuas dentro del conjunto filtrado.

**Independent Test**: Aplicar liga, equipo y posición por separado y combinados, recorrer todas las páginas y verificar cobertura exacta, orden continuo, ausencia de duplicados y total cero cuando no hay coincidencias.

### Tests for User Story 2

- [ ] T025 [P] [US2] Agregar unitarias de validación y normalización de filtros de ranking en `backend/app/src/test/java/com/dappunq/unit/RankingDomainTest.java`.
- [ ] T026 [P] [US2] Agregar integración Testcontainers para filtros combinados, páginas vacías y continuidad de posición en `backend/app/src/test/java/com/dappunq/integration/RankingRepositoryAdapterIntegrationTest.java`.
- [ ] T027 [P] [US2] Agregar E2E MockMvc para filtros válidos, parámetros inválidos y resultado vacío en `backend/app/src/test/java/com/dappunq/e2e/RankingControllerE2ETest.java`.

### Implementation for User Story 2

- [ ] T028 [US2] Aplicar filtros antes del ranking y paginar mediante parámetros seguros en `backend/app/src/main/java/com/dappunq/adapter/RankingRepositoryAdapter.java`.
- [ ] T029 [US2] Agregar recuento total del conjunto filtrado y metadatos `total`, `page` y `limit` en `backend/app/src/main/java/com/dappunq/adapter/RankingRepositoryAdapter.java`.
- [ ] T030 [US2] Numerar posiciones dentro del conjunto filtrado y conservarlas entre páginas en `backend/app/src/main/java/com/dappunq/adapter/RankingRepositoryAdapter.java`.
- [ ] T031 [US2] Validar `page >= 1`, `limit 1-100` y filtros trim, 1-100 caracteres y sin control en `backend/app/src/main/java/com/dappunq/controller/PlayerControllerRest.java`.
- [ ] T032 [US2] Devolver `RankingPageResponse` con array vacío y total cero sin jugadores fuera del filtro en `backend/app/src/main/java/com/dappunq/controller/mapper/RankingDtoMapper.java`.

**Checkpoint**: El ranking global y sus filtros/páginas funcionan de forma independiente y no omiten ni duplican coincidencias.

---

## Phase 5: User Story 3 - Ver cambios vigentes en el ranking (Priority: P2)

**Goal**: Reflejar en la siguiente consulta completada los cambios vigentes de estrategias, pesos y recálculos sin usar cotizaciones históricas.

**Independent Test**: Cambiar una configuración o cotización vigente para que un jugador supere al líder y consultar nuevamente; verificar que el nuevo líder/origen aparece y que las cotizaciones históricas no intervienen.

### Tests for User Story 3

- [ ] T033 [P] [US3] Agregar integración Testcontainers para cambios vigentes de configuración y cotización en `backend/app/src/test/java/com/dappunq/integration/RankingRepositoryAdapterIntegrationTest.java`.
- [ ] T034 [P] [US3] Agregar integración Testcontainers que excluya cotizaciones reemplazadas y estrategias no ACTUAL en `backend/app/src/test/java/com/dappunq/integration/RankingRepositoryAdapterIntegrationTest.java`.
- [ ] T035 [P] [US3] Agregar E2E MockMvc que invoque scraping nuevamente con cambios en el fixture y compruebe en una segunda consulta que catálogo/cotizaciones se reflejan sin duplicados en `backend/app/src/test/java/com/dappunq/e2e/RankingControllerE2ETest.java`.

### Implementation for User Story 3

- [ ] T036 [US3] Filtrar cotizaciones por `is_current=true` y configuración ACTUAL del mismo usuario/estrategia, sin referenciar una columna de usuario activo inexistente en `backend/app/src/main/java/com/dappunq/adapter/RankingRepositoryAdapter.java`.
- [ ] T037 [US3] Evitar cachear valores consolidados entre consultas para que cada consulta completada use la información vigente en `backend/app/src/main/java/com/dappunq/service/RankingServiceImpl.java`.
- [ ] T038 [US3] Verificar que las tenencias usadas para desempatar correspondan a tokens actuales en manos de inversores, excluyendo al superusuario y sin usar compras históricas, en `backend/app/src/main/java/com/dappunq/adapter/RankingRepositoryAdapter.java`.

**Checkpoint**: Las consultas reflejan la vigencia actual y excluyen datos históricos o configuraciones inactivas.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Completar documentación, errores uniformes y validación integral.

- [ ] T039 [P] Agregar `GET /players/ranking` al contrato OpenAPI central `docs/openapi.yaml`, preservando el contrato de `POST /quotes/scrape` sin duplicar la especificación.
- [ ] T040 [P] Agregar al Postman de backend el endpoint `GET /players/ranking`, parámetros, ejemplos y respuestas en `backend/postman/token-valuation.postman_collection.json` y `backend/postman/token-market.postman_collection.json`.
- [ ] T041 [P] Actualizar `backend/specs/016-ranking-jugadores/quickstart.md` para invocar `POST /quotes/scrape` antes del ranking y documentar filtros, paginación, vigencia y sincronización.
- [ ] T042 Uniformar respuestas 400 y 500 del ranking mediante `backend/app/src/main/java/com/dappunq/exception/GlobalExceptionHandler.java` y `backend/app/src/main/java/com/dappunq/dto/ErrorResponse.java`.
- [ ] T043 Revisar nombres, excepciones y mensajes en español sin acentos ni letra `ñ` en identificadores Java dentro de `backend/app/src/main/java/com/dappunq/model/ranking/`, `service/`, `adapter/`, `repository/` y `controller/`.
- [ ] T044 Ejecutar toda la suite `backend\gradlew.bat test` y resolver fallos sin modificar ni eliminar pruebas existentes.
- [ ] T045 Ejecutar `backend\gradlew.bat bootRun` y validar arranque local, Swagger y `GET /players/ranking`.
- [ ] T046 Ejecutar las consultas documentadas en `backend/specs/016-ranking-jugadores/quickstart.md` y confirmar todos los criterios medibles aplicables.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sin dependencias; puede comenzar inmediatamente.
- **Foundational (Phase 2)**: Depende de Setup y bloquea todas las historias de usuario.
- **User Story 1 (Phase 3)**: Depende de Foundational y entrega el MVP.
- **User Story 2 (Phase 4)**: Depende de Foundational; puede comenzar en paralelo con US1 después de la infraestructura.
- **User Story 3 (Phase 5)**: Depende de Foundational y reutiliza el flujo de consulta consolidada de US1.
- **Polish (Phase 6)**: Depende de las historias deseadas completadas.

### User Story Dependencies

- **US1 (P1)**: Sin dependencia de US2 o US3; es el MVP.
- **US2 (P2)**: Puede integrarse sobre el ranking de US1, pero conserva criterios independientes.
- **US3 (P2)**: Puede integrarse sobre el cálculo de US1 y debe verificar vigencia sin modificar reglas de filtro.

### Within Each User Story

- Las pruebas deben escribirse antes de implementación y deben fallar primero.
- Modelos antes que servicios.
- Servicio antes que controller.
- Adapter/repository antes que integración final.
- Cada historia completa su checkpoint antes de avanzar.

---

## Parallel Opportunities

- T003, T005, T006, T007, T008 y T011 pueden ejecutarse en paralelo una vez revisado el setup.
- Dentro de US1, las tareas T014-T017 son independientes y pueden ejecutarse en paralelo antes de implementar.
- Las entidades T018 y T019 pueden modificarse en paralelo.
- Dentro de US2, las tareas T025-T027 pueden ejecutarse en paralelo.
- Dentro de US3, las tareas T033-T035 pueden ejecutarse en paralelo.
- T039-T041 pueden ejecutarse en paralelo durante Polish.
- Después de Foundational, US1, US2 y US3 pueden asignarse a desarrolladores distintos si sus interfaces compartidas quedan estabilizadas.

---

## Parallel Example: User Story 1

```text
T014: unitarias del modelo de ranking
T015: unitarias del servicio RankingServiceImpl
T016: integración Testcontainers del Adapter JDBC
T017: contrato OpenAPI y E2E MockMvc
T018: contrato de repositorio read-only para jugadores
T019: prueba E2E de no escritura desde el ranking
```

---

## Parallel Example: User Story 2

```text
T025: unitarias de filtros
T026: integración de filtros/paginación
T027: E2E de filtros y páginas
```

---

## Parallel Example: User Story 3

```text
T033: integración de cambios vigentes
T034: integración de exclusión histórica/inactiva
T035: E2E de consulta posterior al cambio
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Completar Phase 1: Setup.
2. Completar Phase 2: Foundational.
3. Completar Phase 3: User Story 1.
4. **Detenerse y validar**: unicidad, cobertura, máximo, origen, fallback y desempates.
5. Demostrar el MVP antes de incorporar filtros o vigencia adicional.

### Incremental Delivery

1. Completar Setup + Foundational.
2. Entregar US1 y validar el ranking completo.
3. Entregar US2 y validar filtros/paginación.
4. Entregar US3 y validar consultas posteriores a cambios vigentes.
5. Completar Polish y validar constitución, Quickstart y Postman.

### Parallel Team Strategy

1. El equipo completa Setup y Foundational.
2. Después de Foundational:
   - Developer A: US1.
   - Developer B: US2.
   - Developer C: US3.
3. Todos integran por el mismo Adapter, servicio y contrato REST.
