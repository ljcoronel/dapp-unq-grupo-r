# Modelo de datos

## TeamEntity

Tabla: `teams`.

| Campo | Persistencia | Regla |
|---|---|---|
| `id` | `@Id`, `@GeneratedValue(IDENTITY)` | Identificador interno |
| `externalId` | `@Column(nullable=false, unique=true)` | Identificador WhoScored obligatorio y unico |
| `name` | `@Column(nullable=false)` | Nombre recibido, no ficticio |

No tiene referencias a otras entidades.

## NewLeagueEntity

Tabla: `whoscored_leagues`.

| Campo | Persistencia | Regla |
|---|---|---|
| `id` | `@Id`, `@GeneratedValue(IDENTITY)` | Identificador interno |
| `externalId` | `@Column(nullable=false, unique=true)` | Identificador WhoScored obligatorio y unico |
| `name` | `@Column(nullable=false)` | Nombre de la competicion |
| `country` | `@Column(nullable=false)` | Region/pais recibido |

No tiene referencias a otras entidades.

## WhoScoredEntity

Tabla: `whoscored_players`.

Conserva exactamente los campos de instancia actuales: identidad, estadisticas, `team` y `league`. `id` sera generado internamente y `externalId` sera `nullable=false, unique=true`. `team` y `league` seran relaciones `ManyToOne` obligatorias con claves foraneas no nulas. Los valores numericos opcionales admiten `null`; cero no se transforma.

## Relaciones

Muchos `WhoScoredEntity` conocen una `TeamEntity` y una `NewLeagueEntity`. Equipo y liga no conocen jugadores ni entre si. La normalizacion reutiliza una entidad por `externalId` dentro de cada lote y los repositorios garantizan unicidad en base de datos.

## Reglas de actualizacion

1. Rechazar/excluir jugador sin `playerId`, `teamId`, nombre de equipo o identificador de liga valido.
2. Deduplicar por `playerId`.
3. Persistir equipos y ligas antes de jugadores.
4. Ante lote vacio, respuesta no parseable o error externo, conservar el snapshot valido y comunicar el error.
5. No limitar la lista a diez registros.
