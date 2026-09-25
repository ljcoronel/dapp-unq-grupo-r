# Investigación: catálogo de jugadores

## Decisiones

### Integración con football-data

- Usar un `RestClient` como bean, construido a partir del `RestClient.Builder`
  administrado por Spring Boot, con base URL
  `https://api.football-data.org/v4`.
- Solicitar `/competitions/{codigo}/scorers` y aplicar además un límite
  defensivo local después de descartar registros no publicables.
- Mantener los cinco códigos en una configuración inmutable (`PL`, `BL1`, `PD`,
  `SA`, `FL1`) junto con el nombre visible y país.
- Configurar `X-Auth-Token` con una propiedad agrupada mediante
  `@ConfigurationProperties`; su valor debe provenir de una variable de entorno
  obligatoria como `${FOOTBALL_DATA_AUTH_TOKEN}`. Nunca incluir credenciales en
  código, pruebas, Postman, logs o excepciones.
- Deserializar la respuesta con records Java anidados que representen
  `competition`, `scorers`, `player` y `team`. La integración debe seleccionar
  únicamente `name`, `section`, `team.name`, `playedMatches`, `goals`,
  `assists` y `penalties`.
- Ejecutar un `ApplicationRunner` al arrancar. Una excepción de una liga debe
  registrarse y conservar su snapshot anterior; `GET /players` nunca vuelve a
  consultar la API externa.
- Convertir respuestas no-2xx, timeouts y errores de deserialización en una
  excepción explícita de integración. Configurar timeouts finitos y no usar
  reintentos infinitos ni capturas amplias que oculten errores.

### Persistencia y lectura

- Modelar `Liga` y `Jugador` como dominio sin anotaciones JPA, y usar entidades
  de persistencia separadas (`LigaEntity`, `JugadorEntity`) con relación
  uno-a-muchos y repositorios Spring Data.
- En cada arranque, una respuesta exitosa no vacía reemplaza completamente el
  snapshot de esa liga con los jugadores válidos recibidos, limitados a diez:
  primero se eliminan los jugadores anteriores de esa liga y luego se insertan
  los nuevos dentro de una transacción. Así no quedan jugadores obsoletos. Si
  la lista externa es vacía no se insertan ni se eliminan jugadores; la liga
  permanece representable. Si la request falla, se conserva el snapshot anterior.
- `GET /players` lee todas las ligas en un orden fijo y arma una lista de cinco
  listas, incluyendo listas vacías. El controller no conoce el cliente externo.
- Exponer un DTO/record propio, no el payload de football-data ni entidades JPA.
  Los valores `null` se conservan en JSON para que el frontend pueda mostrarlos
  como `-`; el cero se conserva como cero.

### Calidad y seguridad

- Probar el mapeo y las invariantes del modelo con JUnit unitario.
- Probar persistencia y refresco con PostgreSQL real mediante Testcontainers.
- Probar `GET /players` con MockMvc verificando forma, orden y que el servicio
  de lectura no invoca el cliente externo.
- Mantener `GET /players` público y sin token JWT, como ya declara
  `SecurityConfig`; actualizar la collection de Postman sin agregar un header
  `Authorization` a esta request. El `X-Auth-Token` solo pertenece al cliente
  interno que consume football-data.

## Alternativas consideradas

- **WebClient**: descartado porque el requerimiento exige `RestClient` y la
  sincronización es una operación bloqueante de arranque, no un flujo reactivo.
- **Persistir el JSON completo**: descartado; acopla el esquema local a campos
  irrelevantes y contradice la selección explícita de datos.
- **Consultar football-data en cada `GET /players`**: descartado porque viola
  el requisito de que toda interacción de usuario use únicamente PostgreSQL.
- **Una tabla plana con el nombre de liga repetido**: descartada frente a una
  relación liga-jugador, que mantiene la identidad de las cinco secciones y
  permite listas vacías.
- **Borrar todas las ligas antes de un refresh global**: descartado; una falla
  externa no debe eliminar un snapshot válido de otra liga. El reemplazo se
  hace por liga y de forma transaccional, únicamente después de recibir una
  respuesta exitosa no vacía.
- **`@PostConstruct` o `@Async` para la carga**: descartados porque dificultan
  el ciclo de vida y pueden abrir una ventana donde se aceptan lecturas sin
  snapshot coherente; `ApplicationRunner` hace explícita la política de arranque.

## Riesgos y decisiones pendientes de implementación

- La disponibilidad del proveedor, límites de uso y respuestas no-2xx deben
  tratarse como errores de integración observables en logs; no se agregan
  reintentos infinitos ni valores inventados.
- Si la política de negocio requiere que un refresh global falle como unidad,
  el servicio puede encapsular las cinco operaciones en una transacción; la
  implementación debe preservar como mínimo que una respuesta vacía no genere
  filas y que el endpoint nunca haga llamadas externas.
- El `FootballDataService` actual contiene un token literal: debe eliminarse y
  la credencial debe rotarse fuera de este cambio si estuvo expuesta.
