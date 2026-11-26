# Plan de implementación: acceso público al scraping de cotizaciones

**Rama de funcionalidad**: `017-refactor-scraping`  
**Rama Git activa al generar este plan**: `feature/ranking-jugadores` (se mantiene sin cambios, por decisión del usuario)  
**Fecha**: 2026-11-26  
**Especificación**: [spec.md](./spec.md)

## Resumen

Hacer público exclusivamente `POST /quotes/scrape`, sin validar ni requerir `X-API-Key`, conservando el flujo de scraping y el comportamiento del parámetro `numberOfPlayers`. El filtro existente debe omitir únicamente el método POST en la ruta exacta `/quotes/scrape`, y Spring Security debe permitir esa misma operación; el resto de rutas protegidas conserva sus reglas. Modificar directamente `docs/openapi.yaml` para declarar explícitamente que la operación no tiene requisitos de seguridad.

La firma del controlador, la lógica de negocio y la persistencia no cambian. Se conserva el valor predeterminado actual `numberOfPlayers=50` cuando se omite; los valores no numéricos o no positivos continúan devolviendo 400. La especificación fue reconciliada con ese comportamiento existente.

## Contexto técnico

**Lenguaje/versión**: Java 25.  
**Dependencias principales**: Spring Boot 4, Spring Security, Spring MVC, Gradle.  
**Almacenamiento**: PostgreSQL existente; no se requieren cambios de esquema ni persistencia.  
**Pruebas**: JUnit 5, MockMvc, Mockito y Testcontainers para PostgreSQL en pruebas E2E.  
**Plataforma objetivo**: Servicio web REST del backend Spring.  
**Tipo de proyecto**: API web dentro del monorepo.  
**Objetivos de rendimiento**: Sin cambios; conservar los tiempos y el flujo de scraping existentes.  
**Restricciones**:

- La excepción de autenticación aplica solamente a `POST /quotes/scrape`; no usar coincidencias por sufijo ni ampliar el permiso a otras rutas/métodos.
- El filtro debe ignorar el encabezado en esa operación, sin importar si está ausente, vacío, válido o inválido.
- No modificar la autenticación por defecto ni las reglas de las demás operaciones.
- Actualizar directamente el OpenAPI existente en `docs/openapi.yaml`; no crear otro archivo OpenAPI ni buscar la especificación en otras rutas.
- No modificar la firma del controlador, el comportamiento de scraping ni el almacenamiento.
- Mantener `numberOfPlayers=50` como default. El comportamiento actual valida enteros positivos; no existe un límite superior definido y este plan no agrega uno.
- La rama Git activa no coincide con la rama del feature. Se generan los documentos en el directorio solicitado sin cambiar de rama.

**Alcance**: Configuración del filtro y de Spring Security, contrato OpenAPI, especificación/casos de prueba y cobertura automatizada focalizada. No se agrega un endpoint, autenticación nueva, rate limiting, lógica de scraping ni cambios de frontend.

## Verificación de la constitución

### Antes del diseño

- **Arquitectura por capas**: Cumple. El cambio está confinado a autenticación/configuración, contrato y pruebas; no traslada responsabilidades entre Controller, Service, Model y Persistence.
- **Modelo rico**: Cumple. No se modifican modelos ni reglas de dominio.
- **Validación por capas**: Cumple al preservar el binding del parámetro en Spring MVC y la validación existente del servicio.
- **Pruebas y calidad**: Cumple. La cobertura E2E usará MockMvc y permanecerá en el paquete `com.dappunq.e2e`; las pruebas del filtro y del contrato permanecen en sus propios paquetes.
- **Documentación e idioma**: Cumple. Los documentos nuevos y actualizados se redactan en español.
- **Complejidad**: No se introducen capas, dependencias ni almacenamiento nuevos.

### Después del diseño

Los artefactos mantienen los mismos límites: sin entidades ni migraciones, sin cambios de negocio/controlador, con pruebas E2E segregadas y una declaración OpenAPI explícita de seguridad vacía solo para la operación de scraping. La verificación constitucional sigue aprobada.

## Decisiones de diseño

1. **Filtro API key**: ajustar la exclusión para reconocer únicamente `POST` y la ruta exacta `/quotes/scrape`. El encabezado no se consulta ni valida en esa operación. Mantener el comportamiento vigente del filtro para las operaciones que protege.
2. **Spring Security**: configurar únicamente `POST /quotes/scrape` como permitido sin autenticación. Omitir el filtro por sí solo no alcanza: la regla actual exige la autoridad `scraping`, lo que seguiría rechazando al cliente sin API key. Las reglas restantes permanecen intactas.
3. **Controlador y validaciones**: no cambiar la firma `scrape(@RequestParam(defaultValue = "50") int numberOfPlayers)` ni el servicio. Ausencia usa 50; errores de conversión y valores no positivos resultan en 400 según el comportamiento actual.
4. **OpenAPI**: modificar directamente `docs/openapi.yaml` y declarar `security: []` en la operación `POST /quotes/scrape`; no crear otro archivo OpenAPI ni eliminar o alterar el esquema `ApiKeyAuth` usado por otras operaciones.
5. **Pruebas**: ampliar la prueba E2E de scraping para cubrir ausencia, valor arbitrario/vacío de API key, default y errores de parámetro; conservar una regresión para una ruta protegida ajena. Añadir/actualizar pruebas focalizadas del filtro y el contrato.

## Estrategia de pruebas

- **E2E con MockMvc** en `backend/app/src/test/java/com/dappunq/e2e/`:
  - Solicitud válida sin API key: procesamiento normal, estado exitoso y servicio invocado.
  - Solicitud válida con API key válida, inválida, arbitraria o vacía: mismo resultado exitoso; el valor se ignora.
  - Solicitud sin `numberOfPlayers`: usa el default 50 y continúa el procesamiento.
  - Parámetro no numérico o no positivo: 400 y sin ejecutar scraping.
  - Solicitud sin API key a `POST /quotes/recalculate`: mantiene el 401 actual y no inicia el flujo de recálculo.
- **Prueba unitaria del filtro** en `backend/app/src/test/java/com/dappunq/security/`: verificar que solo la combinación exacta POST + ruta de scraping evita la validación API key y que rutas/métodos ajenos no quedan exentos.
- **Contrato OpenAPI** en `backend/app/src/test/java/com/dappunq/contract/`: verificar `security: []` en `/quotes/scrape` y que la declaración de seguridad de otras operaciones no se amplíe ni elimine.
- Usar el PostgreSQL de Testcontainers ya establecido y mocks para aislar el scraping externo; no depender de una conexión real al proveedor de datos.

## Estructura del proyecto

### Documentación de esta funcionalidad

```text
backend/specs/017-refactor-scraping/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/
    └── scrape.md
```

### Código existente afectado en la fase de implementación

```text
backend/app/src/main/java/com/dappunq/
├── security/
│   ├── ApiKeyAuthenticationFilter.java
│   └── SecurityConfig.java
└── controller/
    └── QuoteControllerRest.java             # sin cambios previstos

docs/
└── openapi.yaml                              # modificar este archivo existente

backend/app/src/test/java/com/dappunq/
├── security/
│   └── ApiKeyAuthenticationFilterTest.java
├── e2e/
│   ├── ScrapingApiKeyE2ETest.java
│   └── QuoteRecalculateE2ETest.java
└── contract/
    └── ScrapingOpenApiContractTest.java     # prueba focalizada prevista
```

**Decisión de estructura**: Reutilizar el módulo Spring `backend/app`, el filtro de API key ya registrado y los paquetes existentes de pruebas. No se crea un módulo ni una capa adicional.

## Guía de implementación

1. Aplicar primero la exclusión exacta en el filtro y el permiso exacto en Spring Security, sin cambiar las reglas de acceso generales.
2. Modificar `docs/openapi.yaml` para expresar el acceso público en la operación, sin tocar `ApiKeyAuth` usado por otras rutas.
3. Actualizar las pruebas E2E, del filtro y del contrato para cubrir los casos enumerados arriba.
4. Ejecutar las pruebas focalizadas descritas en [quickstart.md](./quickstart.md), revisar el diff y confirmar que no hubo cambios de comportamiento en otras rutas.

## Seguimiento de complejidad

No hay violaciones de la constitución ni complejidad adicional que justificar.
