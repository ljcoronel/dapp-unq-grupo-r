# Data Model: Datos de jugadores de futbol

## Entidades principales

### Jugador

Representa al futbolista dentro del catálogo del sistema.

Campos principales:
- id: identificador único del jugador
- nombre: nombre del futbolista
- liga: nombre de la competencia a la que pertenece
- equipo: nombre del club
- posicion: rol principal del jugador (por ejemplo, delantero, mediocampista, defensa, portero)
- pases: número de pases ejecutados
- tiros: cantidad de tiros realizados
- intercepciones: número de intercepciones
- calificaciones: valor de desempeño o rating general de la fuente externa
- minutosJugados: tiempo de participación del jugador
- goles: cantidad de goles convertidos
- asistencias: cantidad de pases de gol o asistencias
- tirosAlArco: veces que remató al arco
- pasesRealizados: total de pases completados
- tarjetasAmarillas: cantidad de tarjetas amarillas
- tarjetasRojas: cantidad de tarjetas rojas
- ratingGeneral: valoración consolidada del desempeño general
- createdAt: fecha de la sincronización o registro inicial
- updatedAt: última actualización del snapshot

Reglas de validación:
- id no puede duplicarse
- nombre, liga, equipo y posicion no pueden ser nulos ni vacíos tras sanitización
- cada jugador debe pertenecer a una liga y equipo consistentes
- si faltan estadísticos obligatorios, el registro debe rechazarse
- el modelo debe evitar mezclar jugadores de equipos o ligas distintas en una misma fila

Relaciones:
- Un `Jugador` pertenece a una sola `Liga` y un solo `Equipo` dentro de esa liga
- Un `Jugador` puede tener varios snapshots históricos de sincronización asociados a diferentes fechas

### Liga

Representa la competencia de origen del jugador. Las ligas soportadas son:
- Premier League (Inglaterra)
- Bundesliga (Alemania)
- La Liga (España)
- Serie A (Italia)
- Ligue 1 (Francia)

Reglas:
- El nombre de la liga debe estar normalizado para mantener consistencia semántica
- Un equipo debe estar asociado a una liga válida

### Equipo

Representa el club del jugador dentro de una liga específica.

Reglas:
- El equipo debe pertenecer a una liga soportada
- No puede existir un jugador asociado a un equipo que no coincida con la liga indicada

### SnapshotEstadistico

Representa la histórica de estadísticas por jugador y por momento de sincronización.

Campos principales:
- id
- jugadorId
- fechaSincronizacion
- payloadEstadistico: valores del snapshot capturado
- source: indicador de proveedor externo (`FOOTBALL_DATA`, `WHOSCORED`)

Reglas:
- Cada snapshot debe quedar asociado a una fecha y un jugador válido
- El último snapshot persistido es el origen de respuesta cuando el servicio externo no responde

## DTOs

### JugadorRequestDTO

Record Java para encapsular la entrada de una request de creación o actualización.

Campos:
- id: String
- nombre: String
- equipo: String
- liga: String
- posicion: String
- pases: String
- tiros: String
- intercepciones: String
- calificaciones: String
- minutosJugados: String
- goles: String
- asistencias: String
- tirosAlArco: String
- pasesRealizados: String
- tarjetasAmarillas: String
- tarjetasRojas: String
- ratingGeneral: String

Validación y limpieza:
- Trimming y sanitización por constructor del record
- Rechazo de valores nulos o vacíos para los campos obligatorios
- Normalización de nombres de liga/equipo/posición a formato consistente

### JugadorResponseDTO

Record Java para encapsular la salida de la API.

Campos:
- id: String
- nombre: String
- equipo: String
- pases: String
- tiros: String
- intercepciones: String
- calificaciones: String
- minutosJugados: String
- goles: String
- asistencias: String
- tirosAlArco: String
- pasesRealizados: String
- tarjetasAmarillas: String
- tarjetasRojas: String
- ratingGeneral: String

Se usa para responder a `GET /players` y `GET /players/{id}`.

## Excepciones del dominio y de la capa API

- JugadorNotFoundException: cuando el id no existe
- InvalidPlayerDataException: cuando faltan campos obligatorios o la información es incoherente
- DuplicatePlayerException: cuando se intenta crear o registrar un jugador duplicado
- IllegalArgumentException: para request mal formadas o filtros inválidos
- MethodArgumentNotValidException: para errores de validación de DTOs

Estas excepciones deben ser capturadas por `GlobalExceptionHandler` con un método específico por excepción para responder con mensajes claros y códigos HTTP consistentes.

## Contract mapping

- GET /players -> `List<JugadorResponseDTO>`
- GET /players/{id} -> `JugadorResponseDTO`
- Filtros: `liga`, `equipo`, `posicion` como query parameters opcionales

## Implementation notes

La capa de modelo debe validar invariantes del negocio, la capa de servicio debe resolver entidades y decidir si una operación es válida, y la capa de persistence debe persistir y recuperar snapshots. Los controllers deben limitarse a serializar/deserializar DTOs y enviar la operación al service.
