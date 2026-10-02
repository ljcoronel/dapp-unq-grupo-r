# Modelo de datos: catálogo de jugadores

## Dominio

### Liga

- `codigo`: código externo estable (`PL`, `BL1`, `PD`, `SA`, `FL1`).
- `nombre`: nombre visible de la competencia.
- `pais`: país mostrado por la aplicación.
- `jugadores`: colección ordenada de cero a diez `Jugador`.

Invariantes: el código y nombre no son vacíos; una liga pertenece al catálogo
fijo de cinco ligas; la colección nunca supera diez elementos.

### Jugador

- `id`: identificador entero positivo recibido desde `player.id` en
  football-data; es la identidad del jugador y no se genera localmente.
- `nombre`: nombre completo no vacío; es obligatorio para publicar la fila.
- `seccion`: posición/grupo recibido, opcional.
- `equipo`: nombre del equipo, obligatorio para publicar la fila según la
  especificación de registros identificables.
- `partidosJugados`, `goles`, `asistencias`, `penaltis`: estadísticas enteras
  opcionales; `0` es un valor válido y `null` significa no disponible.

Un jugador sin nombre o equipo no se transforma en registro publicable. Los
campos opcionales se conservan como `null`, sin convertirlos en cero.

## Payload externo (records Java)

`FootballDataResponse(CompetitionPayload competition, List<ScorerPayload> scorers)`,
`CompetitionPayload(String name)`, `ScorerPayload(PlayerPayload player,
TeamPayload team, Integer playedMatches, Integer goals, Integer assists,
Integer penalties)`, `PlayerPayload(Integer id, String name, String section)` y
`TeamPayload(String name)`.

El mapper descarta todo campo no listado y asocia el código/pais de la
configuración de la request, no datos no confiables del payload. El `id` se
propaga desde `player.id` sin reemplazo ni transformación.

## Persistencia

### `LigaEntity`

- `id`: UUID o Long generado por PostgreSQL/JPA.
- `codigo`: `varchar`, único y no nulo.
- `nombre`: `varchar`, no nulo.
- `pais`: `varchar`, no nulo.
- relación `@OneToMany(mappedBy = "liga", cascade = ALL, orphanRemoval = true)`.

### `JugadorEntity`

- `id`: entero recibido de `player.id`, `PRIMARY KEY`, único y no nulo; no usa
  generación automática de PostgreSQL/JPA.
- `liga_id`: foreign key no nula.
- `nombre`, `seccion`, `equipo`: columnas de texto; nombre y equipo no nulos.
- `partidos_jugados`, `goles`, `asistencias`, `penaltis`: columnas enteras
  anulables.
- índice por `liga_id` y restricción de unicidad opcional sobre `(liga_id, id)`
  solo como integridad de relación, no por nombre (los homónimos son válidos).
  Como `id` es la clave primaria, su unicidad es global entre ligas.

El servicio mapea dominio a entidades antes de guardar y entidades a DTOs al
leer. El orden de jugadores debe persistirse explícitamente (por ejemplo,
`orden` entero) para que la respuesta sea estable.

## Política de reemplazo al arrancar

El `ApplicationRunner` procesa las cinco ligas en el orden configurado. Para
cada respuesta exitosa con uno o más jugadores válidos, el servicio reemplaza
atómicamente la colección anterior de esa liga por la nueva colección truncada
a diez. No se realiza una actualización incremental: los jugadores que ya no
aparecen en la respuesta se eliminan de la base de datos. Una respuesta vacía
no modifica la colección existente y un error de la API externa conserva el
snapshot anterior.

## Respuesta de `GET /players`

Lista exterior fija de cinco elementos, en el orden:
Premier League, Bundesliga, Primera División, Serie A, Ligue 1.
Cada elemento es una lista de objetos jugador con `id`, `nombre`, `seccion`,
`equipo`, `partidosJugados`, `goles`, `asistencias` y `penaltis`.
Una liga vacía se representa como `[]`; no se crean filas ficticias.

## Respuesta de `GET /players/{id}`

Retorna un único objeto jugador con los mismos campos de `GET /players`,
incluyendo el `id` entero que identifica el registro. Un `id` positivo que no
exista responde `404 Not Found`; valores no enteros o no positivos no
identifican jugadores válidos y deben rechazarse como una request inválida.
