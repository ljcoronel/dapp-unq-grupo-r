# Investigación: formato de respuesta de jugadores

## Decisión: reutilizar la persistencia existente como fuente del listado

`PlayerCatalogReadService.findCatalog()` debe invocar `PlayerRepository.findAll()` y
mapear cada `PlayerEntity` a `PlayerResponse`. No se consultara `LeagueRepository`
para construir grupos ni se crearan listas vacias por liga.

**Razonamiento:** el requisito pide una lista plana y descarta explicitamente el
repositorio de ligas. `PlayerEntity` ya contiene las relaciones `TeamEntity` y
`LeagueEntity`, por lo que la asociacion correcta se conserva en cada elemento.

**Alternativas consideradas:** mantener `findAllByOrderByIdAsc()` de ligas y hacer
una consulta por liga; se descarta porque conserva la envoltura anidada, puede
generar trabajo innecesario y hace depender el listado del catalogo de ligas.

## Decisión: DTOs planos alineados con las entidades, sin identificadores internos

`PlayerResponse` tendra todos los campos publicos de `PlayerEntity` excepto `id`:
`externalId`, `name`, `team`, `league`, `age`, `heightCM`, `weightKG`, `position`,
`appearances`, `minutesPlayed`, `goals`, `assists`, `yellowCards`, `redCards`,
`avgShotsPerGame`, `passSuccessPercentage`, `aerialsWon` y
`timesManOfTheMatch`. `TeamResponse` representara `TeamEntity` con `externalId` y
`name`, tambien sin `id`.

**Razonamiento:** coincide con RF-003 y evita exponer claves internas de la base.
Los tipos wrapper existentes (`Integer` y `Float`) conservan la diferencia entre
`null` y cero durante la serializacion JSON.

**Alternativas consideradas:** conservar los nombres historicos en espanol o
devolver directamente entidades JPA; se descartan porque no representan todos los
campos solicitados y exponen detalles de persistencia.

## Decisión: mantener el detalle por identificador y los errores vigentes

`findPlayer()` seguira usando `findByExternalId`, y el manejo de
`CatalogUnavailableException` permanecera igual. Solo se cambia la forma del
listado y el constructor compartido de `PlayerResponse`.

**Razonamiento:** RF-009 limita el cambio a `GET /players` y RF-008 exige conservar
el error 503 y su cuerpo.

## Decisión: actualizar contrato OpenAPI y pruebas existentes

El esquema de `GET /players` se documentara como `array` de `PlayerResponse`, con
respuesta vacia `[]`, y el esquema de jugador se reemplazara por los nombres y
tipos nuevos. Las pruebas de agrupacion heredadas se adaptaran a la lista plana;
se agregaran verificaciones para campos opcionales, ceros, lista vacia y error 503.

**Razonamiento:** la especificacion exige que documentacion y respuestas observables
sean consistentes, y el contrato anterior es intencionalmente incompatible.

## Observación constitucional

La constitucion indica que el servicio deberia retornar modelos y que el controlador
deberia mapear DTOs, mientras que el requisito explicito solicita que
`PlayerCatalogReadService.findCatalog()` retorne `List<PlayerResponse>` y el codigo
actual ya usa DTOs en ese servicio. El plan conserva esa frontera existente para
hacer el cambio solicitado sin ampliar el alcance a una refactorizacion de capas;
esta desviacion queda documentada como una decision consciente del feature.
