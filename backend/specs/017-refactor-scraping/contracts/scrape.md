# Contrato HTTP: scraping de cotizaciones

## `POST /quotes/scrape`

Inicia el flujo de scraping existente. La operación es pública y no requiere credenciales.

### Seguridad

- `X-API-Key` no es requerido.
- Si se incluye `X-API-Key`, el endpoint ignora el valor, sea válido, inválido o vacío.
- Esta excepción aplica exclusivamente a `POST /quotes/scrape`; no cambia la autenticación de otras operaciones.
- En OpenAPI, declarar `security: []` para esta operación. Mantener `ApiKeyAuth` para operaciones que lo requieren.

### Parámetros

| Nombre | Ubicación | Tipo | Requerido | Valor predeterminado/validación |
|---|---|---|---|---|
| `numberOfPlayers` | Query | Entero | No | Predeterminado: `50`. El valor proporcionado debe ser un entero positivo. No se define límite superior en esta funcionalidad. |

### Respuestas

| Estado | Significado |
|---|---|
| `200 OK` | El flujo existente se procesa. La operación no declara un cuerpo de respuesta. |
| `400 Bad Request` | El query parameter no se puede convertir a entero o no es positivo. |

La omisión del parámetro conserva el default 50 y no es un error. El resultado funcional de scraping y la persistencia permanecen sin cambios.

## Referencia

El contrato publicado se encuentra en `backend/app/src/main/resources/static/openapi.yaml`.
