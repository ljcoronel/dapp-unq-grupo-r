# Contrato HTTP de scraping protegido

## `POST /scrape`

### Headers

| Header | Requerido | Valor |
|---|---|---|
| `X-API-Key` | Sí | API key activa cuyo permiso es `scraping`. |

La clave no se acepta en query string, cuerpo ni como JWT Bearer.

### Parámetros

| Parámetro | Tipo | Requerido | Default |
|---|---|---|---|
| `numberOfPlayers` | integer positivo | No | `1000` |

### Respuestas

- `200 OK`: autorización válida y comportamiento de scraping existente.
- `401 Unauthorized`: falta `X-API-Key`, está vacío, tiene formato inválido, no
  coincide o no posee el permiso `scraping`. El cuerpo contiene un mensaje
  genérico en español y nunca la clave.
- `400 Bad Request`: `numberOfPlayers` no es válido.
- `503 Service Unavailable`: el proveedor o catálogo no está disponible.

La validación ocurre antes de invocar el servicio de scraping.
