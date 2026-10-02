# Modelo de datos: contrato OpenAPI

El contrato es un artefacto versionado, no introduce tablas ni entidades de persistencia. La UI carga la misma definicion YAML empaquetada como recurso web.

## Contrato de API

- **Identidad**: metadatos OpenAPI `title`, `version` y `description`.
- **Version vigente inicial**: `1.0.0`.
- **Responsabilidad**: definir rutas, metodos, proposito, entradas, salidas, errores, esquemas y requisitos de seguridad publicados.
- **Regla de versionado**: un cambio incompatible no se publica bajo la misma version mayor; cada operacion implementada debe aparecer una sola vez en este contrato.

## Operaciones de API

| Operacion | Entrada | Salidas principales | Seguridad actual | Efecto |
|---|---|---|---|---|
| `POST /login` | `UserRequest` | `200 UserResponse`, cabecera `Authorization`; `400 MessageError` | Publica | Emite un JWT; no persiste cambios de usuario |
| `POST /register` | `UserRequest` | `200 UserResponse`; `400 MessageError` | Publica | Crea un usuario; la UI debe advertir y confirmar antes del envio |
| `GET /users/{id}/` | `id` entero positivo de 64 bits | `200 UserResponse`; `400` por entrada invalida; `404 MessageError` | Publica segun SecurityConfig actual | Lectura |
| `GET /players` | Sin parametros | `200` lista de cinco grupos de `PlayerResponse`; `503 PlayerError` | Publica | Lectura |
| `GET /players/{id}` | `id` entero positivo de 32 bits | `200 PlayerResponse`; `400 PlayerError`; `404 PlayerError` | Publica | Lectura |

Las rutas y codigos se verificaran contra la implementacion vigente durante la implementacion. No se declararan respuestas `401`/`403` para estas rutas mientras la politica existente las permita anonimamente.

## Esquemas intercambiados

### `UserRequest`

Representa credenciales de login o registro. Requiere `nombre` no vacio y sin espacios internos, y `password` no vacia de 4 a 16 caracteres. Ambos valores se recortan en el DTO; la credencial no se almacena en la especificacion ni en ejemplos persistentes.

### `UserResponse`

Respuesta publica de autenticacion/perfil. Contiene el campo requerido `nombre`; no incluye el password ni el hash.

### `PlayerResponse`

Respuesta de jugador: `id`, `nombre`, `seccion`, `equipo`, `liga` y estadisticas `partidosJugados`, `goles`, `asistencias`, `penaltis`. Las estadisticas faltantes se representan como nulas. El contrato debe describir la nulabilidad y obligatoriedad segun el DTO serializado.

### `LeagueResponse`

Objeto `liga` anidado con `codigo`, `nombre` y `pais`.

### `MessageError` y `PlayerError`

Esquemas de error existentes, sin remodelado: `MessageError` usa `message` (autenticacion y errores de negocio del perfil) y `PlayerError` usa `mensaje` (catalogo de jugadores). El manejador global de conversion de tipos devuelve actualmente `PlayerError` tambien para un `id` de usuario no numerico; el YAML y las pruebas conservaran ese comportamiento hasta un cambio versionado independiente de la API.

## Esquema de seguridad

- `bearerAuth`: HTTP Bearer, formato descriptivo `JWT`.
- El esquema se ofrece en Swagger UI para credenciales temporales de prueba.
- No se aplica como requisito global ni por operacion mientras ninguna de las cinco rutas lo requiera.
- El token se recibe en la cabecera `Authorization` de una respuesta exitosa de login. No se incluiran tokens de ejemplo ni se persistira una credencial ingresada en UI.

## Solicitud de prueba interactiva

Una solicitud iniciada desde Swagger UI contiene la operacion elegida, los parametros/cuerpo ingresados, el estado de ejecucion y el resultado HTTP. Las credenciales se mantienen en el estado temporal de la UI. Para `POST /register`, la ejecucion requiere confirmacion explicita tras advertir que se creara una cuenta.

## Estados del contrato

La edicion del YAML actualiza la version del contrato de forma explicita. Cambios compatibles conservan version mayor; cambios incompatibles requieren una nueva version mayor. La primera consolidacion establece la linea base `1.0.0`; contratos anteriores se usan como contexto historico, no como especificaciones publicadas simultaneamente.
