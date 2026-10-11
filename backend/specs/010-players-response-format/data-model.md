# Modelo de datos y respuesta

## PlayerEntity -> PlayerResponse

La respuesta publica excluye el `id` autogenerado de `PlayerEntity` y conserva el
resto de los datos con sus tipos y nulabilidad actuales:

| Campo | Tipo | Nulo | Origen |
|---|---|---:|---|
| externalId | Integer/Long normalizado | No | `PlayerEntity.externalId` |
| name | String | No | `PlayerEntity.name` |
| team | TeamResponse | No | `PlayerEntity.team` |
| league | LeagueResponse | No | `PlayerEntity.league` |
| age | Integer | Si | `PlayerEntity.age` |
| heightCM | Integer | Si | `PlayerEntity.heightCM` |
| weightKG | Integer | Si | `PlayerEntity.weightKG` |
| position | String | Si | `PlayerEntity.position` |
| appearances | Integer | Si | `PlayerEntity.appearances` |
| minutesPlayed | Integer | Si | `PlayerEntity.minutesPlayed` |
| goals | Integer | Si | `PlayerEntity.goals` |
| assists | Integer | Si | `PlayerEntity.assists` |
| yellowCards | Integer | Si | `PlayerEntity.yellowCards` |
| redCards | Integer | Si | `PlayerEntity.redCards` |
| avgShotsPerGame | Float | Si | `PlayerEntity.avgShotsPerGame` |
| passSuccessPercentage | Float | Si | `PlayerEntity.passSuccessPercentage` |
| aerialsWon | Float | Si | `PlayerEntity.aerialsWon` |
| timesManOfTheMatch | Integer | Si | `PlayerEntity.timesManOfTheMatch` |

`externalId` es el identificador publico del jugador; `id` no forma parte del
record. Los valores numericos se mantienen como wrappers para que `null` no se
convierta en cero.

## TeamEntity -> TeamResponse

`TeamResponse` contiene `externalId` y `name`. El `id` interno de `TeamEntity` no
se expone. Si la relacion esta ausente, el mapeo debe mantener la representacion
de nulabilidad definida por el contrato y no crear un equipo ficticio.

## LeagueEntity -> LeagueResponse

Se reutiliza `LeagueResponse` y su representacion publica vigente para conservar la
relacion de liga de cada jugador. No se cambia la consulta por identificador.

## Reglas de listado

- `GET /players` devuelve una sola lista plana.
- `findAll()` conserva el orden entregado por el repositorio; no se agregan listas
  internas ni elementos artificiales.
- Un catalogo sin jugadores se serializa como `[]`.
- Una excepcion de lectura se transforma en `CatalogUnavailableException`, con el
  mismo contrato HTTP 503 existente.
