# Feature Specification: Catálogo de jugadores por liga

**Feature Branch**: `[002-catalogo-jugadores]`

**Created**: 2026-09-27

**Status**: Draft

**Input**: User description: "We will display a catalog of players from five different leagues. A table will be shown for each league, with each row representing a player. The columns for each table will be: Name (full), Position (e.g., defender, midfielder), Team, Matches Played, Goals, Assists, and Penalties. Each table will display 10 players. The leagues to be shown are the Premier League (England), Bundesliga (Germany), Primera División (Spain), Serie A (Italy), and Ligue 1 (France). For each league, the league name must be displayed, followed by the table of players. Design: it must be possible to expand or collapse each table. Initially, all tables are expanded. Clicking the league name toggles the expansion or collapse of the table. If any player data is unavailable, a hyphen should be displayed. UI must be in spanish"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consultar jugadores de las ligas (Priority: P1)

Una persona interesada en fútbol quiere consultar los jugadores y sus estadísticas organizados por liga. Puede revisar cinco tablas, cada una con diez jugadores y los mismos datos para facilitar la comparación.

**Why this priority**: Mostrar el catálogo y sus estadísticas es el propósito principal de la funcionalidad.

**Independent Test**: Verificar que se muestran las cinco ligas solicitadas y que cada una presenta diez jugadores con las siete columnas indicadas.

**Acceptance Scenarios**:

1. **Given** la persona abre el catálogo, **When** se muestra el contenido, **Then** ve una tabla para Premier League (Inglaterra), Bundesliga (Alemania), Primera División (España), Serie A (Italia) y Ligue 1 (Francia).
2. **Given** se muestra una tabla de liga, **When** la persona revisa sus filas y encabezados, **Then** encuentra diez jugadores y las columnas Nombre completo, Posición, Equipo, Partidos jugados, Goles, Asistencias y Penales.
3. **Given** el catálogo está visible, **When** la persona lee títulos, encabezados, controles y mensajes de la interfaz, **Then** estos están en español.

---

### User Story 2 - Mostrar y ocultar tablas por liga (Priority: P2)

Una persona quiere enfocarse en una liga o reducir el espacio ocupado por el catálogo, por lo que expande u oculta la tabla correspondiente mediante el nombre de la liga.

**Why this priority**: El control individual permite navegar un catálogo extenso sin ocultar información de otras ligas.

**Independent Test**: Abrir el catálogo, confirmar que todas las tablas empiezan expandidas y alternar cada encabezado de liga para comprobar que solo cambia la tabla asociada.

**Acceptance Scenarios**:

1. **Given** la persona abre el catálogo, **When** se completa la presentación inicial, **Then** las cinco tablas están expandidas.
2. **Given** la tabla de una liga está expandida, **When** la persona selecciona el nombre de esa liga, **Then** esa tabla se colapsa y las demás conservan su estado.
3. **Given** la tabla de una liga está colapsada, **When** la persona selecciona el nombre de esa liga, **Then** esa tabla vuelve a expandirse y muestra sus diez jugadores.

---

### User Story 3 - Interpretar datos incompletos (Priority: P2)

Una persona consulta un jugador cuyo dato estadístico o descriptivo no está disponible y necesita distinguir claramente esa ausencia sin confundirla con un valor real.

**Why this priority**: Una representación uniforme de los datos ausentes evita mostrar información engañosa y mantiene legible la tabla.

**Independent Test**: Revisar registros con uno o más datos no disponibles y confirmar que cada celda sin dato muestra un guion.

**Acceptance Scenarios**:

1. **Given** un dato de jugador no está disponible, **When** se muestra su fila, **Then** la celda correspondiente presenta un guion (`-`).
2. **Given** solo algunos datos de un jugador no están disponibles, **When** se muestra su fila, **Then** los datos disponibles se conservan y solo las celdas sin dato muestran un guion.

### Edge Cases

- Si un registro no tiene ninguno de los datos solicitados, la fila conserva las siete celdas y muestra un guion en cada dato no disponible.
- Si el nombre completo, la posición o el equipo no están disponibles, se aplica la misma representación con guion que para las estadísticas.
- Si una tabla está colapsada, sus filas no se muestran hasta que la persona la expanda; su encabezado de liga permanece visible y accionable.
- La falta de un dato de un jugador no debe modificar la cantidad de filas de la liga ni impedir que se muestren los demás datos disponibles.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: La interfaz MUST mostrar una sección para cada una de estas cinco ligas: Premier League (Inglaterra), Bundesliga (Alemania), Primera División (España), Serie A (Italia) y Ligue 1 (Francia).
- **FR-002**: La interfaz MUST mostrar diez jugadores en la tabla de cada liga.
- **FR-003**: Cada tabla MUST presentar, en este orden, las columnas Nombre completo, Posición, Equipo, Partidos jugados, Goles, Asistencias y Penales.
- **FR-004**: La interfaz MUST mostrar todas las tablas expandidas al abrir el catálogo.
- **FR-005**: La persona MUST poder expandir o colapsar cada tabla seleccionando el nombre de su liga, sin cambiar el estado de las otras tablas.
- **FR-006**: La interfaz MUST mostrar un guion (`-`) en cada campo del jugador cuyo dato no esté disponible, sin sustituir otros datos disponibles.
- **FR-007**: Todo el texto visible de la funcionalidad MUST estar en español.

### Key Entities *(include if feature involves data)*

- **Liga**: Competición identificada por su nombre y país; contiene la lista de diez jugadores y el estado visible u oculto de su tabla.
- **Jugador**: Persona asociada a una liga y a un equipo, con nombre completo, posición y estadísticas de partidos jugados, goles, asistencias y penales. Cada dato puede no estar disponible.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El catálogo presenta las cinco ligas especificadas y exactamente diez filas de jugador en cada tabla.
- **SC-002**: El 100 % de las tablas incluye los siete encabezados requeridos en el orden especificado.
- **SC-003**: En la presentación inicial, el contenido de las cinco tablas está visible; al alternar una liga, solo cambia la visibilidad de su propia tabla.
- **SC-004**: El 100 % de los campos de jugador sin dato disponible se representa con un guion, mientras los demás campos conservan su contenido.
- **SC-005**: El 100 % del texto visible propio del catálogo está en español.

## Assumptions

- Los datos de los jugadores son provistos por la fuente de datos disponible para la aplicación; esta especificación no define una temporada ni una política de actualización.
- Cada jugador mostrado pertenece a la liga de la tabla en la que aparece.
- Cuando la fuente no provea un valor, se considera que ese dato no está disponible y se aplica la representación con guion.
- La interfaz debe conservar sus cinco secciones en pantallas de distintos tamaños; el contenido de las tablas puede requerir desplazamiento horizontal en pantallas estrechas.
