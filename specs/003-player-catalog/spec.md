# Especificación de la funcionalidad: Catálogo de jugadores por liga

**Rama de funcionalidad**: `003-player-catalog`

**Fecha de creación**: 2026-09-25

**Estado**: Borrador

**Entrada**: Descripción del usuario: "Vamos a mostrar un catálogo de jugadores de 5 ligas distintas. Se mostrará una tabla por cada liga. Cada fila de la tabla corresponderá a un jugador. Las columnas de cada tabla serán: Nombre (completo), Sección (si es defensa, mediocampo, etc), Equipo, Partidos jugados, Goles, Asistencia, Penaltis. Se mostrará 10 jugadores por cada tabla. Las ligas a mostrar son Premier League (Inglaterra), Bundesliga (Alemania), Primera División (España), Serie A (Italia), Ligue 1 (Francia). Para cada liga, se debe mostrar su nombre y debajo la tabla con los jugadores. Diseño: se debe poder expandir o contraer cada tabla. Inicialmente, todas se encuentran expandidas. Para expandir o contraer una tabla, debe presionarse el nombre de su liga. Si algún dato de un jugador no se obtiene, entonces se escribe un guión."

## Escenarios de usuario y pruebas

### Historia de usuario 1 - Consultar el catálogo completo por liga (Prioridad: P1)

Como visitante, quiero ver los jugadores agrupados por liga en tablas claras para comparar rápidamente sus datos principales.

**Por qué esta prioridad**: Es el objetivo principal de la funcionalidad y entrega valor desde la primera visita.

**Prueba independiente**: Abrir el catálogo y verificar que aparecen exactamente las cinco ligas, cada una con su nombre y una tabla debajo que contiene hasta 10 jugadores.

**Escenarios de aceptación**:

1. **Dado** que el catálogo está disponible, **cuando** el visitante lo abre, **entonces** se muestran Premier League (Inglaterra), Bundesliga (Alemania), Primera División (España), Serie A (Italia) y Ligue 1 (Francia).
2. **Dado** que una liga tiene jugadores disponibles, **cuando** se muestra su tabla, **entonces** contiene 10 filas de jugadores como máximo y cada fila corresponde a un jugador de esa liga.
3. **Dado** que se muestra una fila, **entonces** sus columnas aparecen en este orden: Nombre, Sección, Equipo, Partidos jugados, Goles, Asistencia y Penaltis.

---

### Historia de usuario 2 - Expandir y contraer tablas (Prioridad: P1)

Como visitante, quiero expandir o contraer la tabla de cada liga presionando su nombre para controlar cuánto contenido veo.

**Por qué esta prioridad**: Permite recorrer cinco tablas sin perder contexto y es una interacción explícita del diseño solicitado.

**Prueba independiente**: Abrir el catálogo, comprobar el estado inicial y presionar sucesivamente los nombres de las ligas para verificar los cambios de visibilidad.

**Escenarios de aceptación**:

1. **Dado** que el catálogo acaba de abrirse, **entonces** las cinco tablas están expandidas y sus filas son visibles.
2. **Dado** que una tabla está expandida, **cuando** se presiona el nombre de su liga, **entonces** solo esa tabla se contrae y sus filas dejan de ser visibles.
3. **Dado** que una tabla está contraída, **cuando** se presiona nuevamente el nombre de su liga, **entonces** esa tabla se expande y sus filas vuelven a ser visibles.
4. **Dado** que existen otras tablas con un estado definido, **cuando** se cambia el estado de una liga, **entonces** las demás conservan su estado.

---

### Historia de usuario 3 - Interpretar datos incompletos (Prioridad: P2)

Como visitante, quiero identificar claramente cuándo un dato no está disponible sin que la tabla se desordene o muestre información engañosa.

**Por qué esta prioridad**: Los datos externos pueden ser incompletos; un marcador uniforme mantiene la tabla legible y evita confundir ausencia con cero.

**Prueba independiente**: Mostrar jugadores con uno o más datos faltantes y comprobar que cada celda afectada muestra un guión.

**Escenarios de aceptación**:

1. **Dado** que no se obtiene cualquier dato de un jugador, **cuando** se renderiza la fila, **entonces** la celda correspondiente muestra `-`.
2. **Dado** que un dato numérico tiene valor cero, **cuando** se renderiza la fila, **entonces** se muestra `0` y no se reemplaza por un guión.
3. **Dado** que falta un dato en una fila, **entonces** las demás celdas conservan sus valores y la fila permanece alineada con las columnas.

### Casos límite

- Si una liga no tiene jugadores disponibles, se mantiene su nombre y se muestra una tabla vacía con un mensaje claro, sin inventar filas.
- Si la fuente entrega más de 10 jugadores para una liga, solo se muestran 10.
- Si la fuente entrega menos de 10 jugadores, se muestran únicamente los disponibles.
- Si un jugador carece de nombre o de liga, el registro no se publica como fila válida; la interfaz no debe mostrar una fila imposible de identificar.
- Si un dato es nulo, vacío o no puede interpretarse, se muestra `-`; un valor numérico igual a cero se conserva como `0`.
- Si dos jugadores tienen el mismo nombre, se muestran como filas separadas cuando pertenecen a registros distintos.
- Si falla la carga de datos, se informa al visitante con un mensaje comprensible sin presentar datos parciales como si fueran completos.

## Requisitos

### Requisitos funcionales

- **RF-001**: El sistema DEBE mostrar exactamente cinco secciones de liga: Premier League (Inglaterra), Bundesliga (Alemania), Primera División (España), Serie A (Italia) y Ligue 1 (Francia).
- **RF-002**: Cada sección DEBE mostrar el nombre de la liga y, debajo, una tabla asociada exclusivamente a esa liga.
- **RF-003**: Cada tabla DEBE mostrar como máximo 10 jugadores y DEBE incluir solo jugadores pertenecientes a la liga de la sección.
- **RF-004**: Cada fila DEBE representar un único jugador y mostrar las columnas, en este orden: Nombre completo, Sección, Equipo, Partidos jugados, Goles, Asistencia y Penaltis.
- **RF-005**: El sistema DEBE mostrar inicialmente expandidas las cinco tablas.
- **RF-006**: El visitante DEBE poder alternar entre expandir y contraer cada tabla presionando el nombre de su liga.
- **RF-007**: Cambiar el estado de una liga NO DEBE cambiar el estado de las otras ligas.
- **RF-008**: Cuando no se obtenga un dato de un jugador, el sistema DEBE mostrar `-` en la celda correspondiente.
- **RF-009**: El sistema NO DEBE sustituir un valor numérico igual a cero por `-`.
- **RF-010**: El sistema DEBE conservar la alineación y el orden de las columnas aunque una o varias celdas de una fila muestren `-`.
- **RF-011**: Si una liga no tiene jugadores disponibles, el sistema DEBE mantener visible su sección y comunicar que no hay jugadores disponibles, sin crear registros ficticios.
- **RF-012**: Ante un fallo de carga, el sistema DEBE mostrar un mensaje comprensible e indicar que el catálogo no pudo cargarse, sin presentar datos incompletos como definitivos.
- **RF-013**: La interacción para expandir o contraer una tabla DEBE poder identificarse como control de esa liga y comunicar su estado expandido o contraído a quienes navegan con tecnologías de asistencia.

### Entidades principales

- **Liga**: Competencia mostrada en el catálogo; tiene nombre visible, país y una colección de hasta 10 jugadores.
- **Jugador**: Persona representada en una fila; tiene nombre completo, sección, equipo y las estadísticas visibles solicitadas.
- **Estadísticas del jugador**: Valores de partidos jugados, goles, asistencias y penaltis; cada valor puede estar disponible, ser cero o no estar disponible.

## Criterios de éxito

### Resultados medibles

- **CS-001**: En el 100% de las aperturas exitosas del catálogo se muestran las cinco ligas solicitadas, cada una con su nombre visible.
- **CS-002**: En el 100% de las tablas se muestran como máximo 10 filas y todas las filas pertenecen a la liga correspondiente.
- **CS-003**: En el 100% de las filas las siete columnas aparecen en el orden definido y permanecen alineadas cuando hay datos faltantes.
- **CS-004**: En el 100% de las aperturas exitosas las cinco tablas comienzan expandidas.
- **CS-005**: En una prueba de interacción, el visitante puede contraer y volver a expandir cada tabla presionando su nombre, sin alterar el estado de las otras cuatro tablas.
- **CS-006**: El 100% de los campos no obtenidos se presenta como `-`, mientras que el 100% de los valores numéricos iguales a cero se presenta como `0`.
- **CS-007**: Al menos el 95% de los visitantes que cuentan con datos cargados pueden identificar la liga, el jugador y sus estadísticas principales sin explicación adicional.
- **CS-008**: Cuando ocurre un fallo de carga, el 100% de los casos muestra un mensaje de error comprensible y no muestra información parcial como catálogo completo.

## Supuestos

- El catálogo se consulta desde la aplicación existente y recibe datos de jugadores ya normalizados por la fuente disponible.
- La versión inicial muestra una lista fija de cinco ligas; no incluye alta, baja, edición, paginación ni filtros adicionales.
- “Sección” representa la posición o grupo del jugador, por ejemplo defensa, mediocampo o delantero.
- “Asistencia” y “Penaltis” se muestran como estadísticas independientes; “Penaltis” representa la métrica entregada por la fuente sin inferir un subtipo no especificado.
- Los nombres de las ligas y los encabezados se muestran en español según la descripción funcional.
- Un dato vacío, nulo, ilegible o no entregado se considera no disponible y se representa con `-`.
- La cantidad de jugadores se limita a 10 por liga en el catálogo, aunque la fuente pueda proporcionar más registros.
