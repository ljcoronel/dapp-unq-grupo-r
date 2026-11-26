# Tasks: Acceso publico al scraping de cotizaciones

**Input**: Documentos de diseño en `/backend/specs/017-refactor-scraping/`

**Prerequisites**: `plan.md` y `spec.md` requeridos; disponibles `research.md`, `data-model.md`, `contracts/scrape.md` y `quickstart.md`

**Organization**: Las tareas estan agrupadas por historia de usuario para permitir implementar y validar cada incremento de forma independiente.

## Formato: `[ID] [P?] [Story] Descripcion`

- **[P]**: Puede ejecutarse en paralelo porque modifica archivos diferentes y no depende de tareas incompletas.
- **[Story]**: Historia a la que pertenece la tarea (`US1`, `US2` o `US3`).
- Todas las tareas incluyen una ruta exacta.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirmar los puntos de extension existentes antes de modificar seguridad y contrato.

- [ ] T001 Revisar la configuracion actual de Spring Security y del filtro API key en `backend/app/src/main/java/com/dappunq/security/SecurityConfig.java` y `backend/app/src/main/java/com/dappunq/security/ApiKeyAuthenticationFilter.java`
- [ ] T002 [P] Revisar las pruebas E2E y unitarias existentes en `backend/app/src/test/java/com/dappunq/e2e/ScrapingApiKeyE2ETest.java`, `backend/app/src/test/java/com/dappunq/e2e/QuoteRecalculateE2ETest.java` y `backend/app/src/test/java/com/dappunq/security/ApiKeyAuthenticationFilterTest.java`
- [ ] T003 [P] Revisar el contrato publicado y el controlador existentes en `backend/app/src/main/resources/static/openapi.yaml` y `backend/app/src/main/java/com/dappunq/controller/QuoteControllerRest.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Dejar identificadas las reglas compartidas que habilitan las historias sin modificar logica de negocio.

**CRITICAL**: No iniciar ninguna historia hasta completar esta fase.

- [ ] T004 Definir el matcher exacto `POST /quotes/scrape` y conservar separado el matcher protegido `POST /quotes/recalculate` en `backend/app/src/main/java/com/dappunq/security/ApiKeyAuthenticationFilter.java` y `backend/app/src/main/java/com/dappunq/security/SecurityConfig.java`
- [ ] T005 [P] Confirmar que `numberOfPlayers` conserva `defaultValue = "50"` y la firma actual del controlador en `backend/app/src/main/java/com/dappunq/controller/QuoteControllerRest.java`
- [ ] T006 [P] Confirmar la estructura de pruebas MockMvc y Testcontainers necesaria en `backend/app/src/test/java/com/dappunq/e2e/ScrapingApiKeyE2ETest.java`

**Checkpoint**: Fundacion lista; las historias pueden empezar sin cambios compartidos adicionales.

---

## Phase 3: User Story 1 - Ejecutar scraping sin credenciales (Priority: P1) MVP

**Goal**: Permitir que `POST /quotes/scrape` sea accesible sin requerir ni validar `X-API-Key`, conservando el scraping normal.

**Independent Test**: Ejecutar `POST /quotes/scrape?numberOfPlayers=1` sin encabezado y con claves validas, invalidas, arbitrarias o vacias; todas deben devolver acceso exitoso y ejecutar el scraping una vez.

### Tests for User Story 1

- [ ] T007 [P] [US1] Actualizar la prueba E2E para aceptar solicitudes validas sin API key y con API key valida, invalida, arbitraria o vacia en `backend/app/src/test/java/com/dappunq/e2e/ScrapingApiKeyE2ETest.java`
- [ ] T008 [P] [US1] Añadir pruebas unitarias del filtro que prueben el bypass exclusivo para `POST /quotes/scrape` y el rechazo de metodos o rutas distintas en `backend/app/src/test/java/com/dappunq/security/ApiKeyAuthenticationFilterTest.java`

### Implementation for User Story 1

- [ ] T009 [US1] Ajustar `shouldNotFilter` en `backend/app/src/main/java/com/dappunq/security/ApiKeyAuthenticationFilter.java` para omitir el filtro unicamente en `POST /quotes/scrape` y no consultar `X-API-Key`
- [ ] T010 [US1] Cambiar la regla de autorizacion en `backend/app/src/main/java/com/dappunq/security/SecurityConfig.java` para usar `.permitAll()` exclusivamente en `POST /quotes/scrape`
- [ ] T011 [US1] Declarar `security: []` en `POST /quotes/scrape` sin alterar el esquema ni las operaciones protegidas en `backend/app/src/main/resources/static/openapi.yaml`
- [ ] T012 [P] [US1] Crear la prueba de contrato OpenAPI para verificar seguridad vacia en `/quotes/scrape` y conservar `ApiKeyAuth` en operaciones protegidas en `backend/app/src/test/java/com/dappunq/contract/ScrapingOpenApiContractTest.java`

**Checkpoint**: US1 esta completa y puede demostrarse mediante scraping publico valido sin credenciales.

---

## Phase 4: User Story 2 - Conservar la validacion de numberOfPlayers (Priority: P1)

**Goal**: Mantener el default 50 y la respuesta `400` para valores no numericos o no enteros positivos sin modificar el controlador ni el servicio.

**Independent Test**: Solicitar el endpoint sin el parametro y verificar 50; enviar texto, cero o negativo y verificar `400`; enviar un entero positivo y verificar procesamiento normal.

### Tests for User Story 2

- [ ] T013 [P] [US2] Añadir casos E2E sin `numberOfPlayers` que verifiquen la invocacion con `"50"` en `backend/app/src/test/java/com/dappunq/e2e/ScrapingApiKeyE2ETest.java`
- [ ] T014 [P] [US2] Añadir casos E2E para texto, cero y negativo que verifiquen `400` y ausencia de invocacion del scraping en `backend/app/src/test/java/com/dappunq/e2e/ScrapingApiKeyE2ETest.java`
- [ ] T015 [P] [US2] Añadir un caso E2E con entero positivo que verifique la invocacion normal del scraping en `backend/app/src/test/java/com/dappunq/e2e/ScrapingApiKeyE2ETest.java`

### Implementation for User Story 2

- [ ] T016 [US2] Verificar y, solo si fuera necesario, conservar `@RequestParam(defaultValue = "50") int numberOfPlayers` sin cambiar la firma ni la logica de `backend/app/src/main/java/com/dappunq/controller/QuoteControllerRest.java`
- [ ] T017 [US2] Mantener intactos el binding y la validacion existentes de Spring MVC para producir `400` en conversion invalida o valor no positivo, sin agregar limite superior
- [ ] T018 [P] [US2] Actualizar el contrato OpenAPI de `NumberOfPlayers` y las respuestas `200`/`400` en `backend/app/src/main/resources/static/openapi.yaml` sin cambiar el formato funcional ni la persistencia

**Checkpoint**: US2 conserva por separado el default, los valores validos y los errores existentes del parametro.

---

## Phase 5: User Story 3 - Mantener la proteccion del resto de la API (Priority: P1)

**Goal**: Limitar la excepcion de autenticacion exclusivamente a `POST /quotes/scrape` y conservar las reglas actuales de los demas endpoints.

**Independent Test**: Invocar `POST /quotes/recalculate` sin API key y verificar `401`; invocar metodos o rutas distintos de scraping con credenciales invalidas y verificar que siguen protegidos.

### Tests for User Story 3

- [ ] T019 [P] [US3] Mantener o ampliar la regresion E2E de `POST /quotes/recalculate` sin API key para verificar `401` y ausencia de scraping en `backend/app/src/test/java/com/dappunq/e2e/QuoteRecalculateE2ETest.java`
- [ ] T020 [P] [US3] Añadir pruebas unitarias para confirmar que `GET /quotes/scrape`, `POST /quotes/scrape/other` y otras rutas protegidas no quedan exentas en `backend/app/src/test/java/com/dappunq/security/ApiKeyAuthenticationFilterTest.java`
- [ ] T021 [P] [US3] Ampliar la prueba de contrato para confirmar que `POST /quotes/recalculate` conserva `ApiKeyAuth` y la clave no se elimina del esquema en `backend/app/src/test/java/com/dappunq/contract/ScrapingOpenApiContractTest.java`

### Implementation for User Story 3

- [ ] T022 [US3] Revisar y preservar las reglas de `POST /quotes/recalculate`, otros metodos POST y `anyRequest()` en `backend/app/src/main/java/com/dappunq/security/SecurityConfig.java`
- [ ] T023 [US3] Revisar el flujo especial de recálculo y conservar su validacion 401/403/500 en `backend/app/src/main/java/com/dappunq/security/ApiKeyAuthenticationFilter.java`

**Checkpoint**: Todas las historias funcionan; la excepcion no se extiende a otros metodos ni rutas.

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Validar integridad, documentacion y formato antes de entrega.

- [ ] T024 [P] Ejecutar las pruebas focalizadas indicadas en `backend/specs/017-refactor-scraping/quickstart.md`: filtro, scraping E2E, recálculo E2E y contrato OpenAPI
- [ ] T025 [P] Revisar el diff para confirmar que no hubo cambios en scraping, persistencia, firma del controlador ni modelos
- [ ] T026 Actualizar o verificar la coleccion Postman relacionada con `POST /quotes/scrape` segun la entrega requerida por la constitucion
- [ ] T027 Ejecutar compilacion local y pruebas relacionadas desde `backend` y registrar cualquier fallo en el resumen de validacion

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sin dependencias; puede comenzar inmediatamente.
- **Foundational (Phase 2)**: Depende de Setup; bloquea todas las historias.
- **US1 (Phase 3)**: Depende de Foundational y es el MVP.
- **US2 (Phase 4)**: Depende de Foundational; puede ejecutarse en paralelo con US1.
- **US3 (Phase 5)**: Depende de Foundational; puede ejecutarse en paralelo con US1 y US2.
- **Polish (Phase N)**: Depende de completar todas las historias deseadas.

### User Story Dependencies

- **US1**: Sin dependencias de otras historias; modifica el bypass publico.
- **US2**: Independiente de US1; conserva el contrato existente del parametro.
- **US3**: Independiente de las otras historias; valida que ninguna proteccion se debilita.

### Within Each User Story

- Escribir las pruebas antes de implementar el cambio correspondiente.
- Ejecutar las pruebas focalizadas y confirmar que fallan por el comportamiento antiguo.
- Implementar el cambio minimo en cada archivo.
- Ejecutar nuevamente las pruebas antes de avanzar al checkpoint.

---

## Parallel Opportunities

- T002 y T003 pueden ejecutarse en paralelo dentro de Setup.
- T005 y T006 pueden ejecutarse en paralelo dentro de Foundational.
- Las pruebas T007-T008 pueden escribirse en paralelo en US1.
- T012 puede desarrollarse en paralelo con las implementaciones de seguridad de US1.
- Las pruebas T013-T015 pueden ejecutarse en paralelo como casos separados de US2.
- T018 puede modificarse en paralelo con T016-T017.
- Las pruebas T019-T021 pueden ejecutarse en paralelo en US3.
- T024 y T025 pueden ejecutarse en paralelo durante Polish.

---

## Parallel Example: User Story 1

```text
T007 - Ampliar ScrapingApiKeyE2ETest
T008 - Ampliar ApiKeyAuthenticationFilterTest
T012 - Crear ScrapingOpenApiContractTest

T009 - Modificar ApiKeyAuthenticationFilter
T010 - Modificar SecurityConfig
T011 - Modificar openapi.yaml
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Completar Phase 1: Setup.
2. Completar Phase 2: Foundational.
3. Completar Phase 3: US1.
4. Ejecutar las pruebas de US1 y validar el bypass publico sin credenciales.
5. Detenerse y demostrar o desplegar el MVP si esta listo.

### Incremental Delivery

1. Completar Setup y Foundational.
2. Entregar US1 como MVP.
3. Añadir US2 y validar el contrato del parametro.
4. Añadir US3 y verificar que no se amplía la excepcion.
5. Ejecutar Polish y confirmar la entrega completa.

### Parallel Team Strategy

1. El equipo revisa conjuntamente Setup y Foundational.
2. Tras Foundational:
   - Desarrollador A implementa US1.
   - Desarrollador B implementa US2.
   - Desarrollador C implementa US3.
3. Integrar y ejecutar conjuntamente las pruebas de Polish.

---

## Notes

- `[P]` significa archivos distintos y sin dependencias sobre tareas incompletas.
- La etiqueta `[USn]` aparece solo en fases de historias de usuario.
- No se requiere crear entidades, migraciones, servicios, endpoints ni mecanismos nuevos de autenticacion.
