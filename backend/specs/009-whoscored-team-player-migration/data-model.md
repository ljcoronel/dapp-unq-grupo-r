# Modelo de datos

## NewLeagueEntity

Tabla: `new_leagues`.

`NewLeagueEntity` es la entidad activa para ligas WhoScored. Su `externalId` es obligatorio y unico. Conserva nombre y pais/region sin metricas agregadas heredadas. `LeagueEntity.java` no existe en la implementacion final.

## TeamEntity

Tabla: `teams`.

| Campo | Persistencia | Regla |
|---|---|---|
| `id` | `@Id`, `@GeneratedValue(IDENTITY)` | Identificador interno |
| `externalId` | `@Column(nullable=false, unique=true)` | Identificador WhoScored obligatorio y unico |
| `name` | `@Column(nullable=false)` | Nombre recibido, no ficticio |

No tiene referencias a otras entidades.

## WhoScoredEntity

Tabla: `whoscored_players`.

`WhoScoredEntity` conserva los campos de instancia actuales y representa al jugador enriquecido por WhoScored. `id` sera generado internamente y `externalId`/externalID sera `nullable=false, unique=true`. `team` y `league` seran relaciones `ManyToOne` obligatorias con claves foraneas no nulas. Los valores numericos opcionales admiten `null`; cero no se transforma.

## Relaciones

Muchos `WhoScoredEntity` conocen una `TeamEntity` y una `NewLeagueEntity`. Equipo y liga no conocen jugadores ni entre si. La normalizacion reutiliza una entidad por `externalId` dentro de cada lote y los repositorios garantizan unicidad en base de datos.

## Reglas de upsert

1. Rechazar/excluir jugador sin `playerId`, `teamId`, nombre de equipo o identificador de liga valido.
2. Deduplicar por `playerId` dentro del lote.
3. Buscar por `externalId`; actualizar el registro encontrado o crear un `WhoScoredEntity` nuevo.
4. Persistir `NewLeagueEntity` y `TeamEntity` antes de `WhoScoredEntity` para satisfacer las claves foraneas.
5. Ante lote vacio, respuesta no parseable o error externo, conservar el snapshot valido y comunicar el error.
6. No limitar la lista a diez registros.
