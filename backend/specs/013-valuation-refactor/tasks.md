---
description: "Tareas de implementación para el refactor de valuación: fórmula compuesta y superusuario"
---

# Tasks: Refactor de valuación — fórmula compuesta y superusuario

**Input**: Documentos de diseño en `backend/specs/013-valuation-refactor/` (`spec.md`, `plan.md`, `research.md`, `data-model.md`, `contracts/valuation-api.md`, `quickstart.md`).

**Prerequisites**: `plan.md` (arquitectura y stack), `spec.md` (historias de usuario y criterios de aceptación), `research.md` (decisiones técnicas), `data-model.md` (invariantes y entidades), `contracts/` (contrato de API).

**Tests**: Mandatorios según la constitución del proyecto y la especificación (unitarios JUnit 5, integración PostgreSQL con Testcontainers y end-to-end con MockMvc).

**Organization**: Tareas agrupadas por historia de usuario para permitir implementación y verificación independiente de cada incremento.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Tarea paralelizable (archivos distintos, sin dependencias con tareas pendientes).
- **[Story]**: Historia de usuario a la que pertenece ([US1], [US2], [US3]). Solo en fases de historias de usuario.
- Rutas de archivo exactas en cada descripción.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar fixtures, configuración de pruebas y contratos compartidos para el refactor.

- [X] T001 [P] Actualizar la colección de Postman con requests y aserciones para valuación acumulativa y estado de tenencias del superusuario en `backend/postman/token-valuation.postman_collection.json`
- [X] T002 [P] Preparar fixtures y helpers de prueba para soportar tenencias con valor acumulado y datos de prueba de superusuario en `backend/app/src/test/java/com/dappunq/config/ValuationTestDataConfig.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Actualizar firmas base del dominio y adaptar llamadores existentes antes de implementar las historias de usuario.

**⚠️ CRITICAL**: El trabajo en historias de usuario no puede comenzar hasta completar esta fase para evitar inconsistencias de firma y compilación.

- [X] T003 [P] Declarar la constante estática `FACTOR_ESCALA = BigDecimal.valueOf(2)` y extender la firma de cálculo para recibir `BigDecimal valorBase` en `backend/app/src/main/java/com/dappunq/model/valuation/PlayerValuationCalculator.java`
- [X] T004 Adaptar la invocación de `calculate` en los servicios de valuación pasando `valorBase` obtenido de `HoldingEntity.getValorUnitario()` en `backend/app/src/main/java/com/dappunq/service/HoldingsValuationService.java` y `backend/app/src/main/java/com/dappunq/service/WeeklyValuationService.java`
- [X] T005 [P] Adaptar tests unitarios existentes que invocan `calculate` para contemplar el nuevo parámetro `valorBase` y asegurar compilación limpia en `backend/app/src/test/java/com/dappunq/unit/PlayerValuationCalculatorTest.java` y `backend/app/src/test/java/com/dappunq/service/WeeklyValuationServiceTest.java`

**Checkpoint**: Base de compilación lista — la implementación de historias de usuario puede comenzar.

---

## Phase 3: User Story 1 - Valor de token acumulativo al revaluar (Priority: P1) 🎯 MVP

**Goal**: Permitir que cada nueva ejecución de valuación (dominical, manual vía `POST /quotes/recalculate` o al configurar primera estrategia) tome como punto de partida el `valorBase` almacenado en el token y acumule el nuevo rendimiento calculado, preservando el historial entre valuaciones.

**Independent Test**: Dado un token con valor previo conocido (0.00 o > 0.00), ejecutar una valuación y verificar que el nuevo valor unitario almacenado sea igual a `valorBase + (result * 2)` y no reemplace destructivamente el valor acumulado con solo `result`.

### Tests for User Story 1

> **NOTE: Escribir estos tests primero y verificar que fallen antes de completar la implementación.**

- [X] T006 [P] [US1] Crear tests unitarios verificando fórmula acumulativa con `valorBase = 0` y `valorBase > 0` en `backend/app/src/test/java/com/dappunq/unit/PlayerValuationCalculatorTest.java`
- [X] T007 [P] [US1] Crear test de servicio verificando que `HoldingsValuationService.revalue()` lee el `valorBase` persistido previo y actualiza tenencias e historial con el acumulado en `backend/app/src/test/java/com/dappunq/service/HoldingsValuationServiceTest.java`
- [X] T008 [P] [US1] Crear test de servicio verificando que `WeeklyValuationService.execute()` lee `valorBase` persistido para cada tenencia y persiste el valor acumulativo en `backend/app/src/test/java/com/dappunq/service/WeeklyValuationServiceTest.java`
- [X] T009 [P] [US1] Crear test de integración con Testcontainers verificando la acumulación de valor en PostgreSQL tras múltiples valuaciones consecutivas en `backend/app/src/test/java/com/dappunq/integration/ValuationPersistenceIntegrationTest.java`
- [X] T010 [P] [US1] Crear test de integración verificando la ejecución semanal y manual consecutiva acumulando sobre el valor previo en `backend/app/src/test/java/com/dappunq/integration/WeeklyValuationIntegrationTest.java`
- [X] T011 [P] [US1] Crear test end-to-end con MockMvc verificando que `POST /quotes/recalculate` y `GET /users/{id}/holdings` reflejan los valores acumulados en `backend/app/src/test/java/com/dappunq/e2e/ValuationConfigurationE2ETest.java`

### Implementation for User Story 1

- [X] T012 [US1] Implementar en el modelo de cálculo la suma `valorBase + (result * FACTOR_ESCALA)` garantizando que `valorBase` nulo sea tratado como 0 en `backend/app/src/main/java/com/dappunq/model/valuation/PlayerValuationCalculator.java`
- [X] T013 [US1] Asegurar que `HoldingsValuationService.revalue()` extraiga `valorBase` de `h.getValorUnitario()` antes del cálculo y actualice `HoldingEntity` y `ValuationHistoryEntity` con el valor acumulado en `backend/app/src/main/java/com/dappunq/service/HoldingsValuationService.java`
- [X] T014 [US1] Asegurar que `WeeklyValuationService.execute()` extraiga `valorBase` de `holding.getValorUnitario()` antes del cálculo y registre `ValuationEntity` y `ValuationHistoryEntity` con el valor acumulado en `backend/app/src/main/java/com/dappunq/service/WeeklyValuationService.java`
- [X] T015 [US1] Asegurar que al configurar la primera estrategia de un usuario en `StrategyConfigurationService`, las tenencias iniciales se valúen con `valorBase = 0` acumulando determinísticamente en `backend/app/src/main/java/com/dappunq/service/StrategyConfigurationService.java`

**Checkpoint**: User Story 1 completa y verificable independientemente como MVP.

---

## Phase 4: User Story 2 - factorEscala fijo en la fórmula de valuación (Priority: P1)

**Goal**: Garantizar que el sistema aplique uniformemente la constante determinista e inmutable `factorEscala = 2` al multiplicar `result` antes de sumarlo a `valorBase`, aplicando el redondeo `HALF_UP` a 2 decimales exclusivamente al resultado compuesto final.

**Independent Test**: Ejecutar cálculos unitarios directos y verificar matemáticamente que cuando `result = 0.50` y `valorBase = 3.00`, el valor resultante es exactamente `4.00`, y cuando `result = 0.00` el valor permanece idéntico a `valorBase`.

### Tests for User Story 2

- [X] T016 [P] [US2] Crear tests unitarios verificando que `factorEscala` multiplica exactamente por 2 y que con `result = 0.00` el valor final no varía respecto a `valorBase` en `backend/app/src/test/java/com/dappunq/unit/PlayerValuationCalculatorTest.java`
- [X] T017 [P] [US2] Crear tests unitarios para verificar la regla de redondeo `HALF_UP` a 2 decimales aplicada al valor compuesto final (sin redondeos truncados intermedios) en `backend/app/src/test/java/com/dappunq/unit/PlayerValuationCalculatorTest.java`
- [X] T018 [P] [US2] Crear tests unitarios para casos de borde de la fórmula (métricas faltantes tratadas como cero, bonus de Defender en Estrategia B y jugador en snapshot sin datos) en `backend/app/src/test/java/com/dappunq/unit/PlayerValuationCalculatorTest.java`

### Implementation for User Story 2

- [X] T019 [US2] Refinar el método `calculate` para aplicar `valorBase.add(result.multiply(FACTOR_ESCALA)).setScale(2, RoundingMode.HALF_UP)` como expresión final única en `backend/app/src/main/java/com/dappunq/model/valuation/PlayerValuationCalculator.java`
- [X] T020 [US2] Validar invariantes de dominio: lanzar `ValuationDomainException` o tratar defensivamente `valorBase` negativo antes del cálculo en `backend/app/src/main/java/com/dappunq/model/valuation/PlayerValuationCalculator.java`

**Checkpoint**: User Story 2 completa. La fórmula matemática es determinista, uniforme y cumple con las precisiones requeridas.

---

## Phase 5: User Story 3 - Superusuario inicializado correctamente al arrancar el sistema (Priority: P1)

**Goal**: Automatizar la inicialización del Superusuario en el arranque de la aplicación siguiendo la secuencia: 1) crear tenencias de 100 tokens por jugador con valor unitario 0; 2) configurar Estrategia A como ACTUAL (pesos 30/25/15/15/15) si no existe ninguna ACTUAL; 3) valuar sus tokens como consecuencia directa del paso 2; garantizando idempotencia en reinicios posteriores.

**Independent Test**: Arrancar el backend desde cero, verificar mediante `GET /users/{idSuperUser}/strategy` que existe una estrategia ACTUAL configurada y mediante `GET /users/{idSuperUser}/holdings` que los tokens tienen tenencia y valor unitario mayor a 0 (habiendo jugadores con métricas).

### Tests for User Story 3

- [X] T021 [P] [US3] Crear tests de servicio para `SuperuserInitializationService` verificando la ejecución secuencial 1→2→3 y que la valuación ocurre si la estrategia fue creada en `backend/app/src/test/java/com/dappunq/service/SuperuserInitializationServiceTest.java`
- [X] T022 [P] [US3] Crear tests de servicio verificando la idempotencia: si el superusuario ya cuenta con estrategia ACTUAL, no se duplica ni se revalúan sus tokens redundantemente en `backend/app/src/test/java/com/dappunq/service/SuperuserInitializationServiceTest.java`
- [X] T023 [P] [US3] Crear test de integración con Testcontainers verificando la inicialización completa del superusuario, sus tenencias y su valuación inicial persistida en `backend/app/src/test/java/com/dappunq/integration/InitialHoldingsIntegrationTest.java`
- [X] T024 [P] [US3] Crear test de integración verificando la idempotencia en reinicios sucesivos del contexto de Spring Boot en `backend/app/src/test/java/com/dappunq/integration/SuperuserStartupIntegrationTest.java`
- [X] T025 [P] [US3] Crear tests end-to-end con MockMvc verificando que `GET /users/{idSuperUser}/strategy` y `GET /users/{idSuperUser}/holdings` responden con datos activos y valor positivo tras el startup en `backend/app/src/test/java/com/dappunq/e2e/SuperuserStartupE2ETest.java`

### Implementation for User Story 3

- [X] T026 [US3] Inyectar `InitialHoldingsService` y refactorizar `SuperuserInitializationService` implementando el método orquestador `initializeSuperuser()` que ejecuta el flujo 1→2→3 de forma transaccional e idempotente en `backend/app/src/main/java/com/dappunq/service/SuperuserInitializationService.java`
- [X] T027 [US3] Implementar o actualizar el runner de inicialización (`ApplicationRunner` con orden adecuado) para disparar `initializeSuperuser()` al arranque del sistema en `backend/app/src/main/java/com/dappunq/config/AppCommandConfig.java`
- [X] T028 [US3] Garantizar tolerancia a catálogo vacío: si no hay jugadores registrados al arrancar, el flujo de tenencias e inicialización de estrategia no debe fallar en `backend/app/src/main/java/com/dappunq/service/SuperuserInitializationService.java`

**Checkpoint**: User Story 3 completa. El Superusuario cuenta con estrategia activa y catálogo valorizado desde el primer arranque.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Verificación integral, documentación y validación de escenarios de arranque y revaluación.

- [X] T029 [P] Documentar el comportamiento de valuación acumulativa y el estado inicial del superusuario en `backend/app/src/main/resources/static/openapi.yaml`
- [X] T030 Ejecutar la suite completa de pruebas (`./gradlew test`) asegurando paso limpio de tests unitarios, de integración y E2E en `backend/app`
- [X] T031 Ejecutar las validaciones funcionales y escenarios de idempotencia descritos en `backend/specs/013-valuation-refactor/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No tiene dependencias — puede ejecutarse de inmediato.
- **Foundational (Phase 2)**: Depende de Setup — **BLOQUEA** todas las historias de usuario para que el compilador y los tests compartan las firmas base.
- **User Stories (Phases 3, 4, 5)**: Dependen de la finalización de Foundational (Phase 2).
  - **US1 (P1)**: Núcleo del refactor acumulativo (MVP).
  - **US2 (P1)**: Precisión del factor de escala y redondeo compuesto (puede ejecutarse en paralelo o inmediatamente después de US1).
  - **US3 (P1)**: Orquestación del superusuario en el arranque (depende de la fórmula de valuación de US1/US2).
- **Polish (Phase 6)**: Depende de completar todas las historias de usuario deseadas.

### User Story Dependencies

- **User Story 1 (P1)**: Depende de Phase 2 (Foundational). Implementa la base acumulativa en `PlayerValuationCalculator`, `HoldingsValuationService` y `WeeklyValuationService`.
- **User Story 2 (P1)**: Depende de Phase 2. Refina el cálculo y las reglas de redondeo/invariantes con `FACTOR_ESCALA = 2`.
- **User Story 3 (P1)**: Depende de US1 y US2 para que la valuación ejecutada en el paso 3 del flujo del superusuario use la fórmula acumulativa correcta.

### Within Each User Story

- Los tests deben crearse primero y verificar su fallo antes de completar la implementación.
- Los modelos y firmas se implementan antes que los servicios y orquestadores.
- Los servicios se implementan antes que los endpoints y runners de integración.
- Cada historia debe completarse y validarse antes de considerarla entregada.

### Parallel Opportunities

- T001 y T002 en Setup son paralelizables entre sí.
- T003 y T005 en Foundational pueden prepararse en paralelo.
- En US1: T006, T007, T008, T009, T010, T011 son tests en archivos separados y pueden desarrollarse en paralelo.
- En US2: T016, T017, T018 pueden ejecutarse en paralelo.
- En US3: T021, T022, T023, T024, T025 pueden desarrollarse en paralelo.
- T029 puede ejecutarse en paralelo con tareas de implementación o prueba.

---

## Parallel Example: User Story 1

```bash
# Lanzar creación de pruebas para User Story 1 en paralelo:
Task: "Crear tests unitarios verificando fórmula acumulativa con valorBase = 0 y valorBase > 0 en backend/app/src/test/java/com/dappunq/unit/PlayerValuationCalculatorTest.java"
Task: "Crear test de servicio verificando que HoldingsValuationService.revalue() lee el valorBase persistido previo en backend/app/src/test/java/com/dappunq/service/HoldingsValuationServiceTest.java"
Task: "Crear test de integración con Testcontainers verificando la acumulación de valor en PostgreSQL en backend/app/src/test/java/com/dappunq/integration/ValuationPersistenceIntegrationTest.java"
Task: "Crear test end-to-end con MockMvc verificando que POST /quotes/recalculate y GET /users/{id}/holdings reflejan valores acumulados en backend/app/src/test/java/com/dappunq/e2e/ValuationConfigurationE2ETest.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Completar Phase 1: Setup (T001 - T002).
2. Completar Phase 2: Foundational (T003 - T005) — crítico para desbloquear compilación.
3. Completar Phase 3: User Story 1 (T006 - T015).
4. **STOP and VALIDATE**: Ejecutar tests de US1 (`PlayerValuationCalculatorTest`, `HoldingsValuationServiceTest`, `ValuationPersistenceIntegrationTest`) y verificar acumulación funcional (MVP).

### Incremental Delivery

1. Setup + Foundational → Base técnica lista y compilable.
2. US1 → Fórmula acumulativa activa en servicios y persistencia (MVP desplegable).
3. US2 → Verificación estricta de `factorEscala = 2`, redondeo compuesto e invariantes matemáticas.
4. US3 → Inicialización automática del superusuario (tenencias + estrategia + valuación inmediata).
5. Polish → Documentación OpenAPI, suite completa `./gradlew test` y validación de `quickstart.md`.

---

## Notes

- Todas las tareas respetan estrictamente la convención `- [ ] [TaskID] [P?] [Story?] Descripción con ruta de archivo`.
- Las tareas de setup, foundational y polish no tienen etiqueta de historia `[Story]`.
- Las tareas de historias de usuario tienen obligatoriamente la etiqueta `[US1]`, `[US2]` o `[US3]`.
- Cada tarea contiene la ruta exacta de archivo relativa a la raíz del repositorio.
- No se rompen principios constitucionales: la lógica reside en el Rich Model, las validaciones respetan las capas y no se añaden anotaciones JPA en los modelos.
