# Investigación: acceso público al scraping

## Decisión: excluir solo la ruta y el método exactos en el filtro

**Decisión**: El filtro API key debe dejar pasar sin validación únicamente `POST /quotes/scrape`, comparando la ruta exacta en vez de aceptar cualquier ruta que termine en `/scrape`.

**Justificación**: El filtro actual procesa solicitudes POST a scraping y recálculo y valida `X-API-Key` antes de continuar. La selección actual por sufijo puede incluir otras rutas si se agregan en el futuro. La exclusión explícita satisface el alcance de la especificación y evita una exención accidental.

**Alternativas consideradas**:
- Excluir cualquier ruta terminada en `/scrape`: descartada porque amplía la excepción a rutas distintas.
- Omitir la comprobación solo cuando no exista el encabezado: descartada porque las claves inválidas/vacías también deben ignorarse.

## Decisión: cambiar la regla de autorización solo para el endpoint público

**Decisión**: La regla de Spring Security para `POST /quotes/scrape` debe permitir acceso sin autenticación; conservar sin cambios las reglas restantes.

**Justificación**: El filtro no es el único control de acceso. La configuración actual exige la autoridad `scraping` para `POST /quotes/scrape`; ignorar la API key en el filtro sin modificar esa regla aún produce rechazo. La modificación debe ser limitada a un matcher de método y ruta exactos.

**Alternativas consideradas**:
- Cambiar solo el filtro: descartada porque Spring Security continuaría exigiendo la autoridad.
- Permitir todas las operaciones `/quotes/**`: descartada porque también expondría rutas administrativas protegidas.
- Cambiar la regla general de `anyRequest`: descartada porque debilitaría otros endpoints.

## Decisión: mantener el contrato del parámetro y reflejar el comportamiento real

**Decisión**: Conservar el default existente de `numberOfPlayers=50`; mantener 400 para un valor que no se pueda convertir a entero o no sea positivo. No agregar un máximo. Se actualiza la especificación para coincidir con este comportamiento, confirmado por el usuario.

**Justificación**: `QuoteControllerRest` define `@RequestParam(defaultValue = "50")`. Por lo tanto, la ausencia del query parameter usa el valor 50 y no produce 400. La conversión de tipo de Spring MVC y la validación existente en el servicio cubren los errores observados.

**Alternativas consideradas**:
- Hacer obligatorio el query parameter: descartada por la decisión expresa de conservar el default actual.
- Introducir un límite superior durante este cambio: descartada porque el servicio actual no define uno y la funcionalidad no debe redefinir la validación.

## Decisión: declarar seguridad vacía a nivel de operación OpenAPI

**Decisión**: Usar `security: []` en la operación `POST /quotes/scrape`, conservando el esquema `ApiKeyAuth` y las declaraciones de las demás operaciones.

**Justificación**: El archivo OpenAPI publicado es `backend/app/src/main/resources/static/openapi.yaml`; la operación de scraping declara actualmente `ApiKeyAuth`. Una declaración vacía en la operación hace explícito que no aplica seguridad sin alterar otras operaciones.

**Alternativas consideradas**:
- Eliminar el esquema `ApiKeyAuth`: descartada porque otras operaciones protegidas lo usan.
- Omitir la declaración de seguridad de la operación: menos explícita y más frágil frente a futuras declaraciones de seguridad global.

## Decisión: reutilizar las pruebas MockMvc existentes

**Decisión**: Ampliar el E2E de scraping y usar la prueba existente de recálculo como regresión; añadir cobertura del contrato OpenAPI en el paquete `contract`.

**Justificación**: El proyecto ya usa MockMvc con Spring Security y Testcontainers PostgreSQL para estos endpoints. `ScrapingApiKeyE2ETest` cubre actualmente autorización con clave; `QuoteRecalculateE2ETest` verifica el 401 sin clave para una operación protegida. Los tests E2E se mantienen en el paquete dedicado exigido por la constitución.

**Alternativas consideradas**:
- Probar únicamente el filtro aislado: descartada como cobertura suficiente porque no comprobaría el matcher final de Spring Security ni la integración con el controlador.
- Invocar el proveedor externo de scraping en pruebas: descartada para mantener las pruebas deterministas; se conserva el mock existente.

## Referencias del repositorio revisadas

- `backend/app/src/main/java/com/dappunq/security/ApiKeyAuthenticationFilter.java`
- `backend/app/src/main/java/com/dappunq/security/SecurityConfig.java`
- `backend/app/src/main/java/com/dappunq/controller/QuoteControllerRest.java`
- `backend/app/src/main/java/com/dappunq/service/WhoScoredScrapingService.java`
- `backend/app/src/main/resources/static/openapi.yaml`
- `backend/app/src/test/java/com/dappunq/e2e/ScrapingApiKeyE2ETest.java`
- `backend/app/src/test/java/com/dappunq/e2e/QuoteRecalculateE2ETest.java`
- `backend/specs/006-api-key-recalculate/research.md`
