---

description: "Tareas ejecutables para migrar el catálogo de jugadores a WhoScored"
---

# Tasks: Migración del catálogo a WhoScored

**Input**: Documentos de diseño en `backend/specs/009-whoscored-team-player-migration/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/scrape.md`, `quickstart.md`

**Tests**: Incluidos porque la especificación y el plan exigen pruebas unitarias, de integración, E2E y una prueba real acotada.

**Organización**: Las tareas están agrupadas por historia de usuario para permitir implementación y validación incremental.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar la estructura de persistencia, dependencias y configuración que soportará la migración.

- [x] T001 Revisar `backend/app/build.gradle` y `backend/app/src/main/resources/application.properties` para identificar dependencias, propiedades y configuración heredada de FootballData que deberán ajustarse
- [x] T002 [P] Inventariar las referencias activas a FootballData en `backend/app/src/main/java`, `backend/app/src/test/java` y recursos de `backend/app/src/main/resources` para usar el inventario como criterio de eliminación
- [x] T003 [P] Documentar en `backend/specs/009-whoscored-team-player-migration/quickstart.md` los comandos de prueba deterministas, Testcontainers, Playwright y el caso real con `numberOfPlayers=1`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establecer el modelo JPA, repositorios y reglas comunes que bloquean la implementación de las historias.

**⚠️ CRITICAL**: No iniciar la implementación de las historias hasta completar esta fase.

- [x] T004 [P] Ajustar `backend/app/src/main/java/com/dappunq/persistence/NewLeagueEntity.java` para mantener la entidad activa de WhoScored, hacer `externalId` obligatorio y único y conservar nombre y región sin métricas heredadas
- [x] T005 [P] Ajustar `backend/app/src/main/java/com/dappunq/persistence/TeamEntity.java` con tabla `teams`, `externalId` obligatorio y único, nombre obligatorio y ningún estado adicional innecesario
- [x] T006 [P] Ajustar `backend/app/src/main/java/com/dappunq/persistence/WhoScoredEntity.java` para conservar sus campos actuales, hacer único y obligatorio `externalId`, aceptar `null` en estadísticas opcionales y agregar relaciones `ManyToOne` obligatorias a liga y equipo
- [x] T007 [P] Crear el repositorio Spring Data de `LeagueEntity` en `backend/app/src/main/java/com/dappunq/persistence/NewLeagueRepository.java` con búsqueda por `externalId`
- [x] T008 [P] Crear el repositorio Spring Data de `TeamEntity` en `backend/app/src/main/java/com/dappunq/persistence/TeamRepository.java` con búsqueda por `externalId`
- [x] T009 [P] Crear o adaptar el repositorio de `PlayerEntity` en `backend/app/src/main/java/com/dappunq/persistence/WhoScoredRepository.java` con búsqueda por `externalId` y operaciones de consulta del catálogo
- [x] T010 Adaptar `backend/app/src/main/java/com/dappunq/service/PlayerCatalogReadService.java` y `backend/app/src/main/java/com/dappunq/controller/PlayerControllerRest.java` para leer las entidades activas WhoScored y exponer sus relaciones sin depender de `LeagueEntity` o `PlayerEntity`
- [x] T011 Definir en `backend/app/src/main/java/com/dappunq/exception/CatalogUnavailableException.java` y `backend/app/src/main/java/com/dappunq/exception/GlobalExceptionHandler.java` la propagación consistente de lotes vacíos, respuestas inválidas y fallos externos sin borrar el snapshot válido

**Checkpoint**: El modelo activo y las interfaces de persistencia están listos; las historias pueden implementarse sobre estas reglas.

---

## Phase 3: User Story 1 - Obtener el catálogo desde WhoScored (Priority: P1) 🎯 MVP

**Goal**: Normalizar datos reales o simulados de WhoScored, persistirlos idempotentemente y conservar el catálogo anterior cuando la actualización no es válida.

**Independent Test**: Ejecutar `WhoScoredScrapingService` con fixtures válidos, duplicados, campos opcionales, cero numérico, registros inválidos y fallos; verificar que no se invoque FootballData, que el lote válido se persista una sola vez y que un fallo conserve los datos existentes.

### Tests for User Story 1

- [x] T012 [P] [US1] Crear pruebas unitarias de normalización en `backend/app/src/test/java/com/dappunq/unit/WhoScoredScrapingServiceTest.java` para nombres, posiciones, identificadores, estadísticas adicionales, campos ausentes y cero válido
- [x] T013 [P] [US1] Crear pruebas unitarias de validación en `backend/app/src/test/java/com/dappunq/unit/WhoScoredScrapingValidationTest.java` para excluir jugadores sin `playerId`, sin `teamId`, sin nombre de equipo o sin liga confiable, y deduplicar por identificador externo
- [x] T014 [P] [US1] Crear pruebas de persistencia con Testcontainers en `backend/app/src/test/java/com/dappunq/integration/WhoScoredCatalogPersistenceIntegrationTest.java` para restricciones únicas, upsert repetido, orden de claves foráneas y ausencia de límite artificial de diez jugadores
- [x] T015 [P] [US1] Actualizar o reemplazar `backend/app/src/test/java/com/dappunq/integration/PlayerCatalogRefreshIntegrationTest.java` para probar snapshot seguro ante lote vacío, JSON inválido, error externo y fallo aislado de una competición

### Implementation for User Story 1

- [x] T016 [US1] Implementar la normalización de la respuesta WhoScored y sus estadísticas en `backend/app/src/main/java/com/dappunq/service/WhoScoredScrapingService.java`, preservando `null`, cero y todos los atributos compatibles recibidos
- [x] T017 [US1] Implementar en `backend/app/src/main/java/com/dappunq/service/WhoScoredScrapingService.java` la validación de identificadores, deduplicación por `playerId`, exclusión de relaciones ficticias y procesamiento independiente de las competiciones configuradas
- [x] T018 [US1] Implementar el upsert transaccional en `backend/app/src/main/java/com/dappunq/service/WhoScoredScrapingService.java`, resolviendo liga y equipo por `externalId`, guardándolos antes del jugador y actualizando entidades existentes sin duplicados
- [x] T019 [US1] Adaptar `backend/app/src/main/java/com/dappunq/dto/whoscored/PlayerStatDTO.java` y `backend/app/src/main/java/com/dappunq/dto/whoscored/PlayerStatisticsResponse.java` al formato normalizado que consume el servicio, sin reintroducir DTOs de FootballData
- [x] T020 [US1] Adaptar `backend/app/src/main/java/com/dappunq/controller/ScrapingControllerRest.java` para mantener `POST /scrape`, convertir `numberOfPlayers` positivo a `String`, aplicar default `1000` y devolver errores de formato o ejecución según `backend/specs/009-whoscored-team-player-migration/contracts/scrape.md`
- [x] T021 [US1] Eliminar el arranque automático o flujo de refresco heredado que compita con WhoScored en `backend/app/src/main/java/com/dappunq/service/PlayerCatalogStartupRunner.java` y `backend/app/src/main/java/com/dappunq/service/PlayerCatalogRefreshService.java`, dejando un único flujo activo
- [x] T022 [US1] Actualizar `backend/app/src/test/java/com/dappunq/integration/WhoScoredRealScrapingIntegrationTest.java` para invocar la URL real con `numberOfPlayers="1"`, parsear al menos un registro y comprobar que puede persistirse sin FootballData

**Checkpoint**: El catálogo se actualiza desde WhoScored, es idempotente y mantiene el snapshot válido ante entradas fallidas.

---

## Phase 4: User Story 2 - Consultar jugadores y equipos enriquecidos (Priority: P1)

**Goal**: Publicar jugadores con equipo, liga y atributos extendidos conservando la navegación, identificación y agrupación existentes.

**Independent Test**: Persistir un fixture con una liga, un equipo y un jugador enriquecido; consultar listado, agrupación y detalle, verificando la relación, los campos presentes, los ausentes y el cero válido.

### Tests for User Story 2

- [x] T023 [P] [US2] Crear pruebas unitarias de entidades y mapeo en `backend/app/src/test/java/com/dappunq/unit/WhoScoredEntityTest.java` para `TeamEntity`, relaciones obligatorias, unicidad de `externalId` y atributos extendidos con `null` y cero
- [x] T024 [P] [US2] Actualizar `backend/app/src/test/java/com/dappunq/integration/PlayerCatalogPersistenceIntegrationTest.java` para verificar la relación unidireccional jugador-equipo-liga y el upsert coherente de un mismo equipo para varios jugadores
- [x] T025 [P] [US2] Actualizar `backend/app/src/test/java/com/dappunq/e2e/PlayerCatalogE2ETest.java`, `backend/app/src/test/java/com/dappunq/e2e/PlayerByIdE2ETest.java` y `backend/app/src/test/java/com/dappunq/e2e/PlayerCatalogGroupingE2ETest.java` para validar listado, detalle, agrupación y atributos WhoScored
- [x] T026 [US2] Actualizar `backend/app/src/test/java/com/dappunq/e2e/PlayerIncompleteDataE2ETest.java` para comprobar que datos opcionales ausentes no se inventan y que un equipo no identificable no crea una relación ficticia

### Implementation for User Story 2

- [x] T027 [US2] Actualizar `backend/app/src/main/java/com/dappunq/dto/PlayerResponse.java` y `backend/app/src/main/java/com/dappunq/service/PlayerMapper.java` para incluir equipo, liga y atributos extendidos sin convertir ausencias en valores inventados
- [x] T028 [US2] Adaptar `backend/app/src/main/java/com/dappunq/service/PlayerCatalogReadService.java` para consultas de listado, detalle y agrupación basadas en `PlayerRepository`, preservando las reglas públicas de identificación
- [x] T029 [US2] Adaptar `backend/app/src/main/java/com/dappunq/controller/PlayerControllerRest.java` para exponer la relación equipo-jugador y los nuevos campos sin cambiar autenticación ni contratos no relacionados
- [x] T030 [US2] Verificar y ajustar `backend/app/src/main/java/com/dappunq/persistence/WhoScoredEntity.java`, `backend/app/src/main/java/com/dappunq/persistence/TeamEntity.java` y sus repositorios para reutilizar una única identidad de equipo por `externalId` dentro y fuera de cada lote

**Checkpoint**: Las consultas de catálogo y detalle exponen información enriquecida sin romper el comportamiento existente.

---

## Phase 5: User Story 3 - Eliminar FootballData de forma segura (Priority: P2)

**Goal**: Retirar completamente clases, DTOs, configuración, dependencias y pruebas de FootballData sin dejar referencias activas.

**Independent Test**: Buscar `FootballData`, `footballdata`, `LeagueEntity`, `PlayerEntity` y sus servicios en el árbol backend; confirmar que no quedan archivos ni usos activos y que el arranque y la suite siguen compilando con WhoScored.

### Tests for User Story 3

- [x] T031 [P] [US3] Eliminar o migrar las pruebas que importen FootballData, incluyendo `backend/app/src/test/java/com/dappunq/integration/PlayerCatalogRefreshIntegrationTest.java` y cualquier fixture o mock específico detectado por el inventario
- [x] T032 [P] [US3] Añadir una verificación de ausencia de referencias en `backend/app/src/test/java/com/dappunq/integration/FootballDataRemovalTest.java` o en el mecanismo de validación existente, cubriendo clases, configuración, dependencias y recursos heredados

### Implementation for User Story 3

- [x] T033 [P] [US3] Eliminar `backend/app/src/main/java/com/dappunq/persistence/LeagueEntity.java`, `backend/app/src/main/java/com/dappunq/persistence/PlayerEntity.java`, `backend/app/src/main/java/com/dappunq/model/League.java` y sus repositorios o mappers exclusivos
- [x] T034 [P] [US3] Eliminar `backend/app/src/main/java/com/dappunq/service/FootballDataClient.java`, `backend/app/src/main/java/com/dappunq/service/PlayerCatalogStartupRunner.java` y `backend/app/src/main/java/com/dappunq/service/PlayerCatalogRefreshService.java` cuando no queden referencias activas
- [x] T035 [P] [US3] Eliminar el directorio `backend/app/src/main/java/com/dappunq/dto/footballdata/` y cualquier DTO, fixture o recurso exclusivo de FootballData
- [x] T036 [US3] Limpiar `backend/app/build.gradle`, `backend/app/src/main/resources/application.properties` y demás configuración del backend para retirar credenciales, URLs, propiedades, dependencias e imports específicos de FootballData
- [x] T037 [US3] Actualizar los tests y referencias restantes en `backend/app/src/test/java` y documentación de `backend/README.md` para describir únicamente WhoScored y eliminar usos, mocks o instrucciones del proveedor anterior
- [x] T038 [US3] Ejecutar una búsqueda final en `backend/` y corregir todas las referencias activas a FootballData, `LeagueEntity.java` y `PlayerEntity.java` sin alterar funcionalidades no relacionadas con el catálogo

**Checkpoint**: FootballData fue eliminado del código ejecutable, configuración, pruebas y documentación, y WhoScored es la única fuente activa.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Validar la solución completa, su compatibilidad y los criterios de éxito de la feature.

- [x] T039 [P] Revisar anotaciones, nombres de tablas y restricciones `UNIQUE` de `externalId` en `backend/app/src/main/java/com/dappunq/persistence/NewLeagueEntity.java`, `TeamEntity.java` y `WhoScoredEntity.java`
- [x] T040 [P] Revisar que no se hayan agregado variables de instancia prohibidas a `LeagueEntity`, `TeamEntity` o `PlayerEntity` y que las relaciones permanezcan unidireccionales
- [x] T041 Ejecutar `backend/gradlew :app:test` y corregir fallos de compilación, unitarios, integración, E2E o Testcontainers introducidos por la migración
- [x] T042 Ejecutar el caso de validación de `backend/specs/009-whoscored-team-player-migration/quickstart.md`, incluyendo `POST /scrape?numberOfPlayers=1`, repetición idempotente y conservación del catálogo ante respuesta inválida
- [x] T043 Actualizar `backend/specs/009-whoscored-team-player-migration/quickstart.md` y `backend/specs/009-whoscored-team-player-migration/plan.md` si los comandos, nombres de clases o decisiones finales difieren de la implementación

## Phase 7: Convergence

- [x] T044 Eliminar el código heredado de FootballData del backend para dejar solo la fuente WhoScored y cumplir RF-010, RF-011 y la arquitectura de capas y pruebas de la Constitución II/V per FR-010 + Constitution II/V (contradicts)
- [x] T045 Eliminar el modelo de persistencia heredado `LeagueEntity` y `PlayerEntity` y actualizar los servicios y repositorios restantes para reutilizar `LeagueEntity`, `TeamEntity` y `PlayerEntity` sin dependencias activas de los modelos antiguos per RF-010 / plan: storage decision (partial)
- [x] T046 Reemplazar pruebas y documentación obsoletas que aún importan `footballdata` o el flujo de `PlayerCatalogRefreshService`, y ejecutar `backend/gradlew :app:test` para validar la suite WhoScored-only per RF-012 / CS-007 / plan: testing decision (partial)

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No depende de otras fases; T001-T003 pueden comenzar en paralelo.
- **Foundational (Phase 2)**: Depende de Phase 1; T004-T009 pueden desarrollarse en paralelo por archivo, T010-T011 dependen del modelo y repositorios.
- **User Story 1 (Phase 3)**: Depende de Phase 2; T012-T015 pueden escribirse en paralelo y T016-T022 dependen de las interfaces activas.
- **User Story 2 (Phase 4)**: Depende de Phase 2 y de los modelos/lectura estabilizados en US1; sus pruebas pueden prepararse en paralelo con el trabajo final de US1.
- **User Story 3 (Phase 5)**: Depende de que US1 y US2 ya no necesiten los componentes heredados; las eliminaciones por archivos pueden ejecutarse en paralelo tras T038.
- **Polish (Phase 6)**: Depende de las tres historias y de que la eliminación de FootballData haya terminado.

### User Story Dependencies

- **US1 (P1)**: Depende de la infraestructura fundacional; es el MVP y habilita el flujo de actualización.
- **US2 (P1)**: Depende del modelo fundacional y consume el catálogo normalizado de US1, pero se valida de forma independiente con fixtures persistidos.
- **US3 (P2)**: Depende de US1 y US2 para garantizar que la eliminación no quite componentes todavía activos.

### Parallel Opportunities

- Phase 1: T001, T002 y T003.
- Phase 2: T004-T009 por archivos diferentes; T007-T009 también pueden implementarse en paralelo.
- US1: T012-T015 y luego T016/T017/T019 en archivos o responsabilidades separables.
- US2: T023-T026 y, tras fijar el contrato de respuesta, T027-T029.
- US3: T031-T035 por archivos separados; T036-T037 después de retirar las clases.
- Diferentes historias pueden trabajarse en paralelo una vez estabilizada Phase 2, con coordinación sobre `WhoScoredScrapingService.java` y los tests de catálogo.

## Parallel Example: User Story 1

```text
# En paralelo, antes de modificar la implementación:
Task: "Crear pruebas unitarias de normalización en backend/app/src/test/java/com/dappunq/unit/WhoScoredScrapingServiceTest.java"
Task: "Crear pruebas unitarias de validación en backend/app/src/test/java/com/dappunq/unit/WhoScoredScrapingValidationTest.java"
Task: "Crear pruebas de persistencia en backend/app/src/test/java/com/dappunq/integration/WhoScoredCatalogPersistenceIntegrationTest.java"
Task: "Actualizar pruebas de snapshot en backend/app/src/test/java/com/dappunq/integration/PlayerCatalogRefreshIntegrationTest.java"

# Después de estabilizar las entidades y repositorios:
Task: "Implementar normalización en backend/app/src/main/java/com/dappunq/service/WhoScoredScrapingService.java"
Task: "Adaptar DTOs en backend/app/src/main/java/com/dappunq/dto/whoscored/"
Task: "Adaptar POST /scrape en backend/app/src/main/java/com/dappunq/controller/ScrapingControllerRest.java"
```

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Completar Phase 1 y Phase 2.
2. Completar US1 con normalización, upsert, snapshot seguro, endpoint y prueba real acotada.
3. Ejecutar las pruebas de US1 y validar `POST /scrape?numberOfPlayers=1`.
4. Detenerse para demostrar que el catálogo ya migra a WhoScored sin FootballData.

### Incremental Delivery

1. Agregar US2 para publicar equipo y atributos enriquecidos en listado, detalle y agrupación.
2. Ejecutar las pruebas de dominio, persistencia y E2E de US2.
3. Completar US3 retirando clases, configuración, dependencias y pruebas heredadas.
4. Ejecutar Phase 6 y confirmar la suite completa y el quickstart.

### Notes

- Cada tarea usa `- [ ]`, un identificador secuencial y rutas concretas.
- Las tareas de historias incluyen obligatoriamente `[US1]`, `[US2]` o `[US3]`; Setup, Foundational y Polish no llevan etiqueta de historia salvo las tareas fundacionales que preparan explícitamente una historia.
- `[P]` solo se usa cuando la tarea puede ejecutarse en paralelo sin depender de archivos incompletos de otra tarea.
