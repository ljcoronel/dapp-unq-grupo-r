# Investigacion: proteccion de POST /quotes/recalculate

## Decision: Reutilizar el filtro y la autoridad de scraping existentes

**Decision**: Extender `ApiKeyAuthenticationFilter.shouldNotFilter` para procesar los POST de `/quotes/recalculate` ademas del endpoint de scraping, y autenticar con `SimpleGrantedAuthority("scraping")`.

**Rationale**: El filtro ya valida `X-API-Key`, evita que las solicitudes no autorizadas lleguen al controlador y esta registrado en `SecurityConfig`. Reutilizarlo cumple el alcance sin crear filtros ni `AuthenticationProvider`.

**Alternatives considered**:
- Crear un filtro administrativo separado: descartado porque duplicaria la autenticacion y el ciclo de seguridad.
- Proteger el endpoint solo con JWT: descartado porque un JWT de usuario no debe autorizar el recálculo.

## Decision: Clasificar el resultado de validacion en ApiKeyService

**Decision**: Agregar un resultado tipado que permita al filtro distinguir credencial activa no configurada, encabezado ausente/vacio, clave incorrecta/malformada y clave valida. La credencial existente de permiso `scraping` en `api_keys` es la configuracion del servidor; la falta de una fila activa equivale a configuracion ausente. La nueva ruta valida el valor exacto, sin recortarlo ni normalizarlo.

**Rationale**: El metodo actual `isValidScrapingApiKey` devuelve solo booleano, por lo que no permite mapear configuracion ausente a 500 y discrepancia de clave a 403. La persistencia existente almacena un hash BCrypt, no el secreto plano. Mantener el validador actual para `/quotes/scrape` evita alterar su comportamiento de autenticacion.

**Alternatives considered**:
- Dos consultas separadas para comprobar configuracion y luego coincidencia: descartado porque duplica acceso a datos y puede observar estados distintos.
- Nueva propiedad de clave secreta en configuracion externa: descartada para esta feature porque el flujo vigente genera/rota la clave y guarda su hash en `api_keys`.

## Decision: Mantener codigos de error diferentes por ruta

**Decision**: En `/quotes/recalculate`, responder 401 por ausencia/vacio, 403 por clave diferente o malformada, y 500 si no hay credencial activa configurada. En `/quotes/scrape`, conservar el 401 actual para rechazos.

**Rationale**: `spec.md` define estos resultados para el nuevo endpoint; las pruebas existentes de scraping esperan 401 y no deben cambiar su contrato. Los rechazos se atienden en el filtro antes de consultar cotizaciones o invocar recálculo.

**Alternatives considered**:
- Devolver 401 ante cualquier fallo en ambos endpoints: descartado porque no cumpliria el contrato de `/quotes/recalculate` especificado.
- Usar el `accessDeniedHandler` comun para 403: descartado porque `SecurityConfig` actualmente serializa los rechazos como 401; el filtro debe responder los estados requeridos directamente para la ruta nueva sin cambiar el manejo de errores global.

## Decision: Registrar metadatos de intentos sin persistencia nueva

**Decision**: Registrar por intento de recálculo un timestamp, ruta, resultado y Correlation ID; usar el ID entrante si esta disponible o generar uno. No registrar API Key, JWT, encabezados de autenticacion ni cuerpo.

**Rationale**: La especificacion requiere auditoria trazable pero no persistencia; el repositorio no tiene un mecanismo actual de Correlation ID. Los logs existentes son la opcion de menor alcance. El contexto del identificador debe retirarse al finalizar el filtro para evitar filtraciones entre solicitudes.

**Alternatives considered**:
- Agregar una tabla de auditoria: descartado porque no se requiere persistencia y ampliaria innecesariamente el modelo.
- Registrar la solicitud completa: descartado por riesgo de exponer secretos.

## Decision: Documentar API Key y errores en OpenAPI y Postman

**Decision**: Declarar `ApiKeyAuth` como `apiKey` en el header `X-API-Key` para `/quotes/recalculate`; documentar 401, 403, 500 y conservar 200/503. Agregar el header variable `{{apiKey}}` al request Postman existente.

**Rationale**: El OpenAPI publicado se mantiene en `app/src/main/resources/static/openapi.yaml`, y la coleccion de valoracion incluye el request manual de recálculo. El esquema actual de `/quotes/scrape` se conserva para evitar cambios colaterales en ese contrato.

**Alternatives considered**:
- Marcar el endpoint como publico o con seguridad vacia: descartado porque contradice la proteccion requerida.
- Cambiar la autenticacion del scraping a JWT: descartado por incompatibilidad con la arquitectura y el contrato existentes.

## Patrones existentes relevantes

- `ApiKeyAuthenticationFilter` se agrega antes de `UsernamePasswordAuthenticationFilter`.
- `SecurityConfig` ya protege `POST /quotes/scrape` con autoridad `scraping`.
- `ApiKeyService` consulta la fila activa de permiso `scraping` y verifica el hash BCrypt.
- Los E2E usan MockMvc con Spring Security y Testcontainers PostgreSQL; el filtro tiene pruebas unitarias separadas.
- No se encontro infraestructura de Correlation ID en `backend/app/src/main`; se requiere generar y limpiar el identificador para estos intentos.
