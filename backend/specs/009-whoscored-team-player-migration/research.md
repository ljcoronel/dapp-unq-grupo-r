# Research: Migracion a WhoScored

## Decision: Persistencia JPA para las tres entidades

`TeamEntity`, `NewLeagueEntity` y `WhoScoredEntity` usaran `@Entity`, `@Table`, `@Id`, `@GeneratedValue` y `@Column`. `externalId` sera `nullable = false` y `unique = true` en cada tabla. Las relaciones viviran solo desde `WhoScoredEntity`: `@ManyToOne(optional = false)` hacia equipo y liga, con `@JoinColumn` no nulo. No se agregaran campos de coleccion ni back-references a equipo o liga.

## Decision: Repositorios separados y persistencia por etapas

Se agregaran repositorios para equipo, liga WhoScored y jugador WhoScored, con busquedas por `externalId`. El servicio normalizara el JSON y usara `saveAll` en orden equipo/liga, jugador. Esto respeta la identidad unica y evita que `TeamEntity` o `NewLeagueEntity` conozcan otras entidades.

## Decision: WhoScored como unica fuente activa

`PlayerCatalogRefreshService` dejara de depender de `FootballDataClient` y de su mapper. El scraper sera el adaptador activo. `FootballDataClient`, sus DTOs y pruebas especificas permaneceran fisicamente y se marcaran `@Deprecated`, sin beans ni llamadas desde los flujos activos.

## Decision: Snapshot seguro y validacion

Un lote vacio, invalido o fallido se informara mediante la excepcion/mecanismo existente y no ejecutara reemplazos destructivos. Los registros sin `playerId` o `teamId` se excluyen; cero numerico se conserva como valor valido. La deduplicacion se hara por `externalId` antes de persistir.

## Decision: Prueba real acotada

Se agregara una prueba de integracion del servicio que invoque la URL real de WhoScored con `numberOfPlayers = "1"`. No se simulara ese caso feliz. Las pruebas deterministas de persistencia, errores y normalizacion usaran fixtures o mocks para no depender de red.

## Alternatives considered

- Agregar listas `players` a equipo/liga: rechazado porque viola las restricciones de variables de instancia y hace bidireccionales relaciones que no son necesarias.
- Reutilizar `LeagueEntity`/`PlayerEntity`: rechazado porque el nuevo proveedor requiere external ids y atributos propios, y mezclaria el modelo heredado con el nuevo.
- Eliminar FootballData: rechazado por RF-010; la migracion exige deprecacion sin borrar archivos.
- Proteger `/scrape` con JWT: rechazado explicitamente; la ruta debe permanecer publica.
