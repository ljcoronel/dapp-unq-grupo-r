# Modelo de datos y validación

## Cambios de modelo

Esta funcionalidad no agrega ni modifica entidades de dominio, entidades de persistencia, relaciones, estados ni tablas. No se requieren migraciones. El scraping, su persistencia y sus respuestas funcionales se mantienen sin cambios.

## Contrato de entrada

| Dato | Ubicación | Tipo | Requerido | Regla |
|---|---|---|---|---|
| `numberOfPlayers` | Query string | Entero | No | Si se omite, se usa el valor predeterminado 50. Si se proporciona, debe convertirse a entero y ser positivo; de lo contrario, se responde 400. No se incorpora un límite superior nuevo. |
| `X-API-Key` | Encabezado | Texto | No | Para `POST /quotes/scrape` se ignora totalmente: su ausencia, valor vacío, validez o invalidez no modifica el acceso. |

## Flujo y relaciones

`POST /quotes/scrape` conserva la relación existente entre controlador y servicio. La petición no introduce una nueva entidad de solicitud persistida: el controlador convierte el entero validado a texto para invocar el servicio existente. El filtro y Spring Security definen el acceso, sin alterar el modelo de scraping.

Las demás operaciones mantienen los mecanismos de autenticación actuales. En particular, `POST /quotes/recalculate` sigue protegido por API key.
