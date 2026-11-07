# Validacion rapida

## Prerrequisitos

- Java 25 y las dependencias del proyecto backend instaladas.
- PostgreSQL/Testcontainers disponible para las pruebas de persistencia.
- Ejecutar los comandos desde `backend`.

## Validacion automatizada

1. Ejecutar las pruebas focalizadas del catalogo:

   `./gradlew test --tests "com.dappunq.e2e.PlayerCatalogE2ETest" --tests "com.dappunq.e2e.PlayerCatalogGroupingE2ETest" --tests "com.dappunq.e2e.PlayerIncompleteDataE2ETest" --tests "com.dappunq.e2e.PlayerByIdE2ETest"`

2. Confirmar que `GET /players` devuelve un array de objetos, no un array de
   arrays, y que un catalogo vacio devuelve `[]`.

3. Confirmar en las pruebas de servicio que `PlayerRepository.findAll()` es la
   unica lectura usada por `findCatalog()` y que `LeagueRepository` no se invoca.

4. Confirmar que los campos con `null` permanecen ausentes o nulos segun la
   configuracion JSON y que los ceros validos permanecen como `0` o `0.0`.

5. Confirmar que un fallo de lectura mantiene HTTP 503 y el cuerpo
   `{"mensaje":"El catálogo de jugadores no está disponible"}`.

6. Confirmar que `GET /players/{id}` conserva su respuesta y sus errores 400/404.

El contrato completo se encuentra en `contracts/players-api.yaml`; el mapeo de
campos se encuentra en `data-model.md`.
