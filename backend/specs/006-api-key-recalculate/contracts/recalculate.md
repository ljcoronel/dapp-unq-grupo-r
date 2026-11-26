# Contrato HTTP: recálculo protegido

## `POST /quotes/recalculate`

Ejecuta el flujo actual de recálculo. La API Key administrativa es obligatoria; un JWT no la sustituye y no es necesario cuando la API Key es válida.

### Autenticacion

OpenAPI declara `ApiKeyAuth` como esquema `apiKey`, enviado en el encabezado:

| Header | Requerido | Descripcion |
|---|---|---|
| `X-API-Key` | Si | Clave activa del permiso `scraping`. |

No se aceptan credenciales alternativas en query string o cuerpo.

### Solicitud

- Método: `POST`
- Ruta: `/quotes/recalculate`
- Cuerpo: ninguno

### Respuestas

| Estado | Significado |
|---:|---|
| `200 OK` | Clave valida; se ejecuta la estrategia activa y se devuelve el resultado actual de recálculo. |
| `401 Unauthorized` | Falta `X-API-Key` o su valor esta vacio. Tambien aplica si solo se envia un JWT Bearer. |
| `403 Forbidden` | La clave recibida no coincide o esta malformada/ambigua. El flujo de recálculo no se inicia. |
| `500 Internal Server Error` | No existe credencial activa configurada para validar el endpoint. El flujo de recálculo no se inicia. |
| `503 Service Unavailable` | Fallo operativo del flujo existente de scraping o recálculo. |

Las respuestas de error contienen un mensaje generico en español y no revelan la clave configurada ni la recibida.

### Autorizacion y trazabilidad

El filtro establece la autoridad `scraping` solo tras validar la credencial. Los intentos se registran con timestamp, ruta, resultado y Correlation ID; se genera un ID cuando la solicitud no trae uno. Los logs excluyen la API Key, JWT y cuerpo.
