# Modelo de datos y estados

## Cliente administrativo autorizado

Consumidor de sistema o tarea programada que invoca `POST /quotes/recalculate`. Se autentica con `X-API-Key`; no requiere JWT y un JWT por si solo no autoriza la operación.

## Credencial administrativa

La credencial se representa mediante la fila existente de `ApiKeyEntity` con permiso `scraping`, persistida en `api_keys`.

| Campo existente | Uso |
|---|---|
| `permission` | Identifica el permiso `scraping`. |
| `keyHash` | Hash BCrypt; el secreto en texto plano no se persiste. |
| `active` | Indica si la credencial se encuentra habilitada. |
| `createdAt`, `updatedAt` | Fechas de creación y actualización de la credencial. |

No se agrega una entidad ni una tabla. La ausencia de una credencial activa de permiso `scraping` se trata como configuración del servidor ausente y produce 500 en el endpoint de recálculo.

### Validacion y transiciones

| Estado/entrada | Resultado HTTP para recálculo | Siguiente acción |
|---|---:|---|
| No hay credencial activa configurada | 500 | Registrar el intento; detener el filtro. |
| `X-API-Key` ausente o vacio, con credencial configurada | 401 | Registrar el intento; no invocar el recálculo. |
| Clave no coincidente, malformada o ambigua | 403 | Registrar el intento; no invocar el recálculo. |
| Clave exacta y valida | Autorizado | Crear autenticacion con autoridad `scraping` y continuar. |

La validación de la ruta nueva no debe normalizar una clave diferente para hacerla coincidir. La credencial ausente se determina antes de clasificar el valor recibido. Para `/quotes/scrape` se conserva el comportamiento actual de rechazo 401.

## Intento de recálculo

Registro de auditoria emitido como log; no es una entidad persistida.

| Atributo | Regla |
|---|---|
| Marca de tiempo | Presente en cada resultado registrado. |
| Ruta | Ruta del endpoint, sin query/body sensible. |
| Resultado de autenticacion | Resultado discreto (autorizado, credencial ausente, clave ausente/invalida o configuración ausente). |
| Correlation ID | Se reutiliza el que ya acompaña la solicitud o se genera uno. |
| API Key/JWT | No se registran ni se incluyen en mensajes de error. |

Cada intento termina en una decision de autenticacion antes de entrar al flujo de negocio. El Correlation ID puede compartirse con el contexto de logging durante la solicitud y se limpia al finalizar.

## Cotizacion

Entidad de negocio existente cuyo valor solo puede cambiar cuando la autorización de `POST /quotes/recalculate` tuvo éxito. No cambian sus campos, relaciones ni reglas de cálculo; el resultado exitoso conserva el contrato actual 200 y los errores operativos existentes, incluido 503.
