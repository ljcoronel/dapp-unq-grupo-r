# Research: Migracion a WhoScored

## Decision: Persistencia JPA para las entidades activas

`LeagueEntity`, `TeamEntity` y `PlayerEntity` usaran `@Entity`, `@Table`, `@Id`, `@GeneratedValue` y `@Column`. `externalId`/externalID sera `nullable = false` y `unique = true` en cada tabla. `PlayerEntity` conocera una liga y un equipo mediante relaciones `ManyToOne`; liga y equipo no tendran referencias inversas. `LeagueEntity.java` y `PlayerEntity.java` no se conservaran en el resultado final.

## Decision: Upsert idempotente por identificador externo

Se agregaran repositorios para `LeagueEntity`, `TeamEntity` y `PlayerEntity` con `findByExternalId`. El servicio normalizara el JSON y resolvera cada registro existente por `externalId`, actualizando sus campos o creando uno nuevo. El orden sera `LeagueEntity`/`TeamEntity`, luego `PlayerEntity`, dentro de una transaccion. La restriccion unica y una prueba de repeticion del mismo lote garantizaran que el upsert no cree duplicados.

## Decision: Eliminacion completa de FootballData

Se eliminaran `PlayerCatalogStartupRunner.java`, `PlayerCatalogRefreshService.java`, `FootballDataClient.java`, `League.java`, `backend/app/src/main/java/com/dappunq/dto/footballdata/` y todos los tests especificos de FootballData. Tambien se eliminaran imports, configuracion y dependencias huérfanas. Cualquier archivo no relacionado que resulte innecesario se marcara `@Deprecated` y se retirara solo si no tiene referencias.

## Decision: Snapshot seguro y validacion

Un lote vacio, invalido o fallido se informara mediante la excepcion/mecanismo existente y no ejecutara reemplazos destructivos. Los registros sin `playerId` o `teamId` se excluyen; cero numerico se conserva como valor valido. La deduplicacion se hara por `externalId` antes de persistir.

## Decision: Prueba real acotada

Se agregara una prueba de integracion del servicio que invoque la URL real de WhoScored con `numberOfPlayers = "1"`. No se simulara ese caso feliz. Las pruebas deterministas de persistencia, errores y normalizacion usaran fixtures o mocks para no depender de red.

## Alternatives considered

- Agregar listas `players` a equipo/liga: rechazado porque viola las restricciones de variables de instancia y hace bidireccionales relaciones que no son necesarias.
- Reutilizar `LeagueEntity` o `PlayerEntity`: rechazado; ambos archivos no deben existir en la implementacion final. `LeagueEntity`, `TeamEntity` y `PlayerEntity` son las entidades activas del proveedor.
- Mantener FootballData deprecado: rechazado por la rectificacion; se exige eliminar codigo y tests relacionados.
- Proteger `/scrape` con JWT: rechazado explicitamente; la ruta debe permanecer publica.
