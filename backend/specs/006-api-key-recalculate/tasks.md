# Tasks: Proteccion del recálculo con API Key

**Input**: Documentacion de diseño de `backend/specs/006-api-key-recalculate/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/recalculate.md, quickstart.md

**Tests**: Los escenarios de autenticacion y end-to-end están definidos en spec.md y quickstart.md.

**Organization**: Las tareas se organizan por historia de usuario para permitir implementación y validación independientes.

## Formato: `[ID] [P?] [Story] Descripcion`

- **[P]**: Puede ejecutarse en paralelo (archivos distintos, sin dependencias pendientes)
- **[Story]**: Historia de usuario a la que pertenece (US1, US2 o US3)
- Todas las descripciones incluyen rutas exactas

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar la línea base y la estructura existente de la feature

- [ ] T001 Revisar los artefactos y el contrato HTTP de `backend/specs/006-api-key-recalculate/`
- [ ] T002 Confirmar los puntos de integración existentes en `backend/app/src/main/java/com/dappunq/security/ApiKeyAuthenticationFilter.java`, `backend/app/src/main/java/com/dappunq/security/SecurityConfig.java` y `backend/app/src/main/java/com/dappunq/service/ApiKeyService.java`
- [ ] T003 Confirmar las fixtures y herramientas de prueba existentes en `backend/app/src/test/java/com/dappunq/security/ApiKeyAuthenticationFilterTest.java`, `backend/app/src/test/java/com/dappunq/e2e/QuoteRecalculateE2ETest.java` y `backend/app/src/test/java/com/dappunq/e2e/ScrapingApiKeyE2ETest.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Implementar las decisiones compartidas que bloquean todas las historias

**CRITICAL**: Ninguna historia de usuario puede validarse hasta completar esta fase

- [ ] T004 Agregar a `backend/app/src/main/java/com/dappunq/service/ApiKeyService.java` un resultado tipado que clasifique credencial administrativa ausente, encabezado ausente/vacio, clave inválida y clave válida sin exponer el secreto
- [ ] T005 Extender `backend/app/src/main/java/com/dappunq/service/ApiKeyService.java` para consultar la fila activa con permiso `scraping` y comparar el valor recibido exacta y sin normalización
- [ ] T006 Mantener en `backend/app/src/main/java/com/dappunq/service/ApiKeyService.java` el comportamiento preexistente de `isValidScrapingApiKey` para `/quotes/scrape`
- [ ] T007 Actualizar `backend/app/src/main/java/com/dappunq/security/ApiKeyAuthenticationFilter.java` para aplicar el filtro a los POST de `/quotes/scrape` y `/quotes/recalculate`
- [ ] T008 Agregar en `backend/app/src/main/java/com/dappunq/security/ApiKeyAuthenticationFilter.java` un Correlation ID entrante o generado y su limpieza posterior al terminar el intento

**Checkpoint**: Base de autenticación compartida lista para historias de usuario

---

## Phase 3: User Story 1 - Autorizar el recálculo administrativo (Priority: P1) 🎯 MVP

**Goal**: Permitir que una API Key administrativa válida autorice el recálculo con o sin JWT y mantenga el resultado 200 existente

**Independent Test**: Ejecutar `POST /quotes/recalculate` con una clave activa válida, una vez sin JWT y otra con JWT, y verificar 200 sin cambios en la lógica de recálculo

### Tests for User Story 1

- [ ] T009 [P] [US1] Ampliar `backend/app/src/test/java/com/dappunq/security/ApiKeyAuthenticationFilterTest.java` con pruebas unitarias de clave válida para `/quotes/recalculate`, sin JWT y con JWT presente
- [ ] T010 [P] [US1] Actualizar `backend/app/src/test/java/com/dappunq/e2e/QuoteRecalculateE2ETest.java` para provisionar una API Key válida y enviarla en los casos exitosos y de 503
- [ ] T011 [P] [US1] Actualizar `backend/app/src/test/java/com/dappunq/e2e/ValuationConfigurationE2ETest.java` para que las solicitudes existentes a `/quotes/recalculate` incluyan la API Key válida
- [ ] T012 [P] [US1] Actualizar `backend/app/src/test/java/com/dappunq/e2e/HoldingsInvariantE2ETest.java` para que las solicitudes existentes a `/quotes/recalculate` incluyan la API Key válida

### Implementation for User Story 1

- [ ] T013 [US1] Hacer que `backend/app/src/main/java/com/dappunq/security/ApiKeyAuthenticationFilter.java` cree autoridad `scraping` solo cuando el resultado tipado sea clave válida
- [ ] T014 [US1] Cambiar `backend/app/src/main/java/com/dappunq/security/SecurityConfig.java` la regla de `/quotes/recalculate` de permitida públicamente a `hasAuthority("scraping")`
- [ ] T015 [US1] Conservar en `backend/app/src/main/java/com/dappunq/security/ApiKeyAuthenticationFilter.java` el flujo funcional 200 y la lógica existente de recálculo sin modificar el controlador
- [ ] T016 [US1] Mantener en `backend/app/src/test/java/com/dappunq/e2e/ScrapingApiKeyE2ETest.java` la autorización y el rechazo 401 existentes de `/quotes/scrape`

**Checkpoint**: El recálculo es accesible únicamente con una API Key válida y conserva el comportamiento 200/503

---

## Phase 4: User Story 2 - Rechazar solicitudes no autorizadas o sin configuración (Priority: P1)

**Goal**: Detener antes de la lógica de negocio toda solicitud ausente, inválida o ejecutada sin credencial administrativa activa

**Independent Test**: Ejecutar casos ausente/vacio, JWT sin clave, clave incorrecta y configuración ausente; verificar 401/403/500 y ausencia de interacciones con recálculo

### Tests for User Story 2

- [ ] T017 [P] [US2] Añadir a `backend/app/src/test/java/com/dappunq/security/ApiKeyAuthenticationFilterTest.java` pruebas unitarias para 401 ausente/vacio, 403 incorrecta/malformada y 500 sin configuración
- [ ] T018 [P] [US2] Añadir a `backend/app/src/test/java/com/dappunq/e2e/QuoteRecalculateE2ETest.java` pruebas MockMvc para API Key ausente y solo Bearer JWT
- [ ] T019 [P] [US2] Añadir a `backend/app/src/test/java/com/dappunq/e2e/QuoteRecalculateE2ETest.java` pruebas MockMvc para clave incorrecta, con diferencia de un carácter y con mayúsculas/espacios
- [ ] T020 [P] [US2] Añadir a `backend/app/src/test/java/com/dappunq/e2e/QuoteRecalculateE2ETest.java` una prueba aislada MockMvc con la credencial activa retirada que espera 500
- [ ] T021 [P] [US2] Verificar en `backend/app/src/test/java/com/dappunq/e2e/QuoteRecalculateE2ETest.java` que los rechazos no invocan scraping ni recálculo

### Implementation for User Story 2

- [ ] T022 [US2] Implementar en `backend/app/src/main/java/com/dappunq/security/ApiKeyAuthenticationFilter.java` la respuesta 401 genérica para recálculo con encabezado ausente o vacio
- [ ] T023 [US2] Implementar en `backend/app/src/main/java/com/dappunq/security/ApiKeyAuthenticationFilter.java` la respuesta 403 genérica para clave distinta o malformada
- [ ] T024 [US2] Implementar en `backend/app/src/main/java/com/dappunq/security/ApiKeyAuthenticationFilter.java` la respuesta 500 genérica cuando no exista credencial activa
- [ ] T025 [US2] Registrar en `backend/app/src/main/java/com/dappunq/security/ApiKeyAuthenticationFilter.java` timestamp, ruta, resultado y Correlation ID para cada intento sin API Key, JWT ni cuerpo
- [ ] T026 [US2] Garantizar que `backend/app/src/main/java/com/dappunq/security/ApiKeyAuthenticationFilter.java` responda antes de invocar el recálculo o realizar consultas de cotizaciones

**Checkpoint**: Todos los rechazos tienen el código requerido, terminan antes de lógica de negocio y producen trazabilidad segura

---

## Phase 5: User Story 3 - Comprender el acceso y los resultados de autenticacion (Priority: P2)

**Goal**: Documentar claramente la API Key administrativa y distinguirla del JWT

**Independent Test**: Revisar OpenAPI y Postman y confirmar `ApiKeyAuth`, `X-API-Key`, 401/403/500 y la ausencia de acceso público

### Implementation for User Story 3

- [ ] T027 [P] [US3] Cambiar en `backend/app/src/main/resources/static/openapi.yaml` `/quotes/recalculate` de `security: []` a `ApiKeyAuth` con encabezado `X-API-Key`
- [ ] T028 [P] [US3] Agregar a `/quotes/recalculate` en `backend/app/src/main/resources/static/openapi.yaml` las respuestas documentadas 401, 403 y 500 sin secretos ni JWT como alternativa
- [ ] T029 [P] [US3] Declarar en `backend/app/src/main/resources/static/openapi.yaml` el esquema `ApiKeyAuth` como `apiKey` con nombre de encabezado `X-API-Key`
- [ ] T030 [P] [US3] Actualizar `backend/postman/token-valuation.postman_collection.json` para agregar `X-API-Key: {{apiKey}}` al request de recálculo y reemplazar la descripción de endpoint público
- [ ] T031 [US3] Actualizar `backend/specs/006-api-key-recalculate/quickstart.md` si el flujo manual o los resultados documentados difieren de los cambios finales

**Checkpoint**: OpenAPI y Postman identifican la credencial requerida y todos los rechazos posibles

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Validación final, seguridad y consistencia transversal

- [ ] T032 [P] Revisar que `backend/app/src/main/java/com/dappunq/security/ApiKeyAuthenticationFilter.java` y `backend/app/src/main/java/com/dappunq/service/ApiKeyService.java` no registren ni serialicen API Key o JWT
- [ ] T033 [P] Revisar que los mensajes JSON de error de recálculo sean genéricos y estén escritos en español
- [ ] T034 Ejecutar las pruebas de unidad y E2E de `backend/app/src/test/java/com/dappunq/security/ApiKeyAuthenticationFilterTest.java` y `backend/app/src/test/java/com/dappunq/e2e/QuoteRecalculateE2ETest.java`
- [ ] T035 Ejecutar las pruebas E2E completas indicadas por `backend/specs/006-api-key-recalculate/quickstart.md`
- [ ] T036 Ejecutar la compilación y el conjunto de pruebas de `backend/app` desde `backend`
- [ ] T037 Validar el contrato final contra `backend/app/src/main/resources/static/openapi.yaml`, `backend/postman/token-valuation.postman_collection.json` y `backend/specs/006-api-key-recalculate/contracts/recalculate.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- Phase 1 Setup: sin dependencias
- Phase 2 Foundational: depende de Phase 1 y bloquea todas las historias
- US1 (Phase 3), US2 (Phase 4) y US3 (Phase 5): dependen de Phase 2
- Polish (Final Phase): depende de completar las historias deseadas

### User Story Dependencies

- US1 (P1): Independiente; entrega el 200 autorizado con API Key
- US2 (P1): Puede integrar US1, pero permanece verificable con fixtures aislados
- US3 (P2): Independiente del código funcional una vez definido el contrato final

### Within Each User Story

- US1 y US2: escribir y ejecutar primero las pruebas correspondientes
- US1: autenticación y reglas de seguridad antes de validaciones finales
- US2: clasificación y respuestas antes de trazabilidad/logging
- US3: OpenAPI y Postman pueden ejecutarse en paralelo

## Parallel Opportunities

- T009-T012 son pruebas independientes de archivos distintos
- T017-T021 son casos E2E o unitarios independientes
- T027-T030 pueden actualizarse en paralelo en OpenAPI y Postman
- T032-T033 son revisiones paralelas de seguridad y mensajes
- Distintos desarrolladores pueden cubrir US1, US2 y US3 después de Phase 2

## Parallel Example: User Story 1

```text
T009 ApiKeyAuthenticationFilterTest
T010 QuoteRecalculateE2ETest
T011 ValuationConfigurationE2ETest
T012 HoldingsInvariantE2ETest
```

## Parallel Example: User Story 2

```text
T017 ApiKeyAuthenticationFilterTest
T018-T021 QuoteRecalculateE2ETest
```

## Parallel Example: User Story 3

```text
T027-T029 openapi.yaml
T030 Postman
T031 quickstart.md
```

## Implementation Strategy

### MVP First (User Story 1)

1. Completar Phase 1 y Phase 2
2. Completar US1 y validar su prueba independiente
3. Confirmar 200 con clave válida y conservación de 503
4. Continuar con US2 y luego US3

### Incremental Delivery

1. Base compartida completa
2. Entrega US1: autorización administrativa
3. Entrega US2: rechazos seguros y trazabilidad
4. Entrega US3: documentación y cliente Postman
5. Ejecutar Polish y validación completa

## Notes

- [P] identifica tareas que no comparten archivo ni dependen de tareas incompletas
- Toda tarea de historia debe conservar su etiqueta [US1], [US2] o [US3]
- Los rechazos deben ocurrir antes de lógica de negocio y persistencia de cotizaciones
- No almacenar API Key ni JWT en logs, documentación o respuestas
