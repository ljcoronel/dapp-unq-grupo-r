# Plan de implementacion: Proteccion de POST /quotes/recalculate

**Branch**: `feature/api-key` | **Fecha**: 2026-11-24 | **Spec**: [spec.md](./spec.md)

**Feature branch solicitada**: `006-api-key-recalculate` (no creada; se conserva el branch activo).

## Resumen

Restringir `POST /quotes/recalculate` a la autoridad `scraping` obtenida mediante `X-API-Key`, reutilizando `ApiKeyAuthenticationFilter`, `ApiKeyService` y `SecurityConfig`. La clave ausente o vacia devuelve 401; una clave incorrecta o malformada devuelve 403; la ausencia de una credencial activa configurada devuelve 500. Los intentos se registran sin secretos y con Correlation ID. La estrategia de recálculo y la respuesta exitosa permanecen intactas.

La fuente de requisitos es `spec.md`, segun la decision del usuario. Se permite ampliar `ApiKeyService` para diferenciar configuracion ausente de credencial incorrecta. No se crean filtros ni providers, no se modifica `JwtAuthenticationFilter` ni la logica de cotizaciones y se conserva la respuesta actual de `POST /quotes/scrape`.

## Contexto tecnico

**Lenguaje/version**: Java 25.

**Dependencias principales**: Spring Boot 4.0, Spring Security, Spring Data JPA, PasswordEncoder BCrypt, Springdoc OpenAPI.

**Almacenamiento**: PostgreSQL; credencial existente en `api_keys` como hash, permiso y estado activo. No se persisten intentos de autenticacion.

**Pruebas**: JUnit 5, Mockito, MockMvc, Spring Security Test y Testcontainers PostgreSQL.

**Plataforma/estructura**: Servicio REST en `backend/app` dentro del monorepo.

**Objetivos de rendimiento**: No se especifica un objetivo numerico. Las solicitudes rechazadas no deben iniciar consultas de cotizaciones ni el recálculo; solo se permite la verificacion de la credencial.

**Restricciones**:
- Para recálculo: 401 si falta la clave o esta vacia; 403 ante clave distinta o malformada; 500 si no existe una credencial activa configurada.
- La validacion del recálculo no debe normalizar la clave recibida para convertir una entrada diferente en valida.
- La API Key autoriza por si sola; JWT no es obligatorio ni reemplaza la clave.
- No cambiar la regla de scraping existente, el JWT ni la logica del servicio de recálculo.
- Los logs y errores no incluyen API Key, JWT ni cuerpo sensible.

**Alcance**: Dos rutas POST (`/quotes/scrape` y `/quotes/recalculate`) comparten el filtro y la autoridad; solo la segunda adquiere los nuevos codigos 403/500 y los requisitos de auditoria.

## Verificacion constitucional

| Principio | Resultado | Aplicacion |
|---|---|---|
| Monorepo | PASS | Los cambios se limitan al backend y sus documentos de contrato/Postman. |
| Arquitectura por capas | PASS | La autenticacion queda en filtro/servicio de seguridad; no se altera la logica de `QuoteService` ni la responsabilidad del controlador. |
| Modelo enriquecido | PASS | No se agregan reglas de negocio al modelo de cotizacion. |
| Validacion por capas | PASS | La credencial se valida antes de invocar la operacion de recálculo. |
| Pruebas y calidad | PASS | Se agregan escenarios MockMvc y se actualizan las llamadas existentes solicitadas; la coleccion Postman y OpenAPI se actualizan. La modificacion de pruebas existentes esta autorizada expresamente por el requerimiento. |
| Idioma/documentacion | PASS | La documentacion de la feature y los mensajes siguen en español; los identificadores tecnicos conservan las convenciones del proyecto. |

No hay violaciones constitucionales pendientes ni complejidad adicional que justificar. Revaluacion posterior al diseño: PASS; los artefactos mantienen los limites de arquitectura, no agregan persistencia de logs y no cambian la funcionalidad de recálculo.

## Estructura del proyecto

### Documentacion de esta feature

```text
backend/specs/006-api-key-recalculate/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/
    └── recalculate.md
```

### Codigo y pruebas existentes que se planifica modificar

```text
backend/app/src/main/java/com/dappunq/
├── security/
│   ├── ApiKeyAuthenticationFilter.java
│   └── SecurityConfig.java
└── service/
    └── ApiKeyService.java

backend/app/src/main/resources/static/openapi.yaml
backend/postman/token-valuation.postman_collection.json

backend/app/src/test/java/com/dappunq/
├── security/ApiKeyAuthenticationFilterTest.java
└── e2e/
    ├── QuoteRecalculateE2ETest.java
    ├── ValuationConfigurationE2ETest.java
    └── HoldingsInvariantE2ETest.java
```

**Decision de estructura**: Se reutiliza el backend REST existente. No se crea un filtro, provider, modulo ni tabla de auditoria nuevos. La ausencia de una fila activa de permiso `scraping` se considera credencial administrativa no configurada; los intentos se registran en logs.

## Enfoque de implementacion

1. **Servicio de credenciales**: ampliar `ApiKeyService` con un resultado tipado que separe configuracion ausente, encabezado ausente/vacio, formato/valor incorrecto y clave valida. La ausencia de credencial activa en `api_keys` representa configuracion ausente. La validacion de recálculo compara el valor exacto sin recortarlo ni normalizarlo. Conservar el comportamiento preexistente del validador usado por `/quotes/scrape`.
2. **Filtro y autorizacion**: hacer que `shouldNotFilter` limite el filtro a POST de `/quotes/scrape` y `/quotes/recalculate`. En recálculo responder 401, 403 o 500 de acuerdo al resultado de validacion, con cuerpo JSON generico; conservar 401 y su mensaje vigente para solicitudes de scraping rechazadas. Las claves validas crean la autoridad `scraping`; `SecurityConfig` cambia la regla de recálculo a `hasAuthority("scraping")` y mantiene el `exceptionHandling` existente.
3. **Trazabilidad**: para intentos del recálculo, registrar timestamp, ruta, resultado y Correlation ID. Reutilizar un ID entrante si existe; de lo contrario generar uno. Limpiar el contexto de logging al terminar y no registrar encabezados de credenciales, JWT ni cuerpos.
4. **Contrato y clientes**: actualizar `openapi.yaml` con el esquema `ApiKeyAuth` (header `X-API-Key`) y respuestas 401/403/500, conservando 200 y 503. Mantener el esquema y el contrato existentes de `/quotes/scrape`. Agregar `X-API-Key: {{apiKey}}` al request Postman del recálculo y quitar la descripcion de endpoint publico.
5. **Pruebas**: probar 200 con clave valida y sin JWT; 401 sin clave, vacia y con solo Bearer; 403 con clave incorrecta o malformada; 500 sin credencial activa; ausencia de invocacion de recálculo ante rechazos; log seguro y Correlation ID. Actualizar los requests existentes a `/quotes/recalculate` para usar la clave valida.
6. **Validacion**: ejecutar las pruebas de filtro y E2E afectadas con Testcontainers y comprobar compilacion/test de `backend/app`.

## Orden de trabajo previsto

- **Fase 0 - Investigacion**: documentar el filtro, almacenamiento y validacion existentes, el contrato HTTP publicado y la ausencia de trazabilidad Correlation ID.
- **Fase 1 - Diseno**: describir entidades/estados, fijar el contrato de recálculo y proporcionar pasos ejecutables de validacion.
- **Fase de implementacion posterior**: aplicar los cambios a servicio/filtro/configuracion, OpenAPI/Postman y pruebas. Los cambios de codigo no forman parte de esta salida de planificacion.

## Complejidad

No se justifica infraestructura nueva. La unica ampliacion transversal prevista es el resultado tipado en el servicio de API keys, necesario para cumplir de forma inequívoca los codigos 403 y 500 definidos en la especificacion.
