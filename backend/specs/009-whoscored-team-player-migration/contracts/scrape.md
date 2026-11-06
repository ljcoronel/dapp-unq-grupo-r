# Contrato HTTP de scraping

## `POST /scrape`

Ruta publica. No requiere `Authorization`, aunque el resto de la API conserve autenticacion.

### Query

| Parametro | Tipo | Requerido | Default | Regla |
|---|---|---|---|---|
| `numberOfPlayers` | integer positivo | no | `1000` | Se convierte a `String` para `WhoScoredScrapingService`; el caso de prueba real usa `1` |

### Respuestas

- `200 OK`: scraping y persistencia completados.
- `400 Bad Request`: parametro no positivo o con formato invalido.
- `5xx`: WhoScored no responde, devuelve JSON invalido o la persistencia falla; el ultimo catalogo valido permanece intacto.

La ruta no invoca FootballData ni requiere credenciales de ese proveedor.
