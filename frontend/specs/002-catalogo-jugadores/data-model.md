# Data Model: Catálogo de jugadores por liga

## Liga

Representa uno de los cinco grupos que devuelve `GET /players`, además de su presentación en Home.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| nombre | string | Nombre visible de la competición. |
| pais | string | País asociado a la competición. |
| jugadores | Jugador[] | Registros recibidos en el arreglo de esa liga. |
| expandida | boolean | Estado local de visibilidad; inicialmente `true` para las cinco ligas. |

Las listas se mantienen en el orden devuelto por el endpoint, que se asume corresponde al orden de ligas de la especificación: Premier League (Inglaterra), Bundesliga (Alemania), Primera División (España), Serie A (Italia) y Ligue 1 (Francia).

## Jugador

Representa un registro recibido dentro de una lista de liga. Los campos descriptivos y estadísticos conservan la forma del contrato del servidor.

| Campo | Tipo de origen | Presentación |
|-------|----------------|--------------|
| id | number | Identidad del registro; no es una columna visible requerida. |
| nombre | string o ausente | Nombre completo; ausente se presenta como `-`. |
| seccion | string, null o ausente | Posición; `null` o ausente se presenta como `-`. |
| equipo | string o ausente | Equipo; ausente se presenta como `-`. |
| liga | objeto `{ codigo, nombre, pais }` | Metadatos de la liga asociada. |
| partidosJugados | number, null o ausente | Partidos jugados; `null` o ausente se presenta como `-`. |
| goles | number, null o ausente | Goles; `null` o ausente se presenta como `-`. |
| asistencias | number, null o ausente | Asistencias; `null` o ausente se presenta como `-`. |
| penaltis | number, null o ausente | Penales; `null` o ausente se presenta como `-`. |

Los ceros numéricos son valores disponibles y se muestran como `0`.

## Relaciones y restricciones

- La respuesta contiene cinco listas; cada una se muestra como una sección de liga.
- Cada jugador pertenece a una liga y se presenta dentro del grupo que lo contiene.
- La especificación requiere diez jugadores por lista; no se generan registros sustitutos si la fuente devuelve menos.
- Cada tabla presenta las columnas en orden: Nombre completo, Posición, Equipo, Partidos jugados, Goles, Asistencias y Penales.
- El estado de expansión es independiente para cada Liga y no altera los datos recibidos.
