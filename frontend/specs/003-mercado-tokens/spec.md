# Feature Specification: Mercado de Tokens de Jugadores

**Feature Branch**: `003-mercado-tokens`

**Created**: 2026-11-08

**Status**: Draft

**Input**: User description: "Rediseño de la interfaz con sidebar de navegación, catálogo unificado de jugadores con filtros y ordenamiento, perfil de usuario, compra y venta de tokens con órdenes y tolerancia de precio, portfolio con historial de operaciones, estrategia de valuación configurable, y visualización de cotizaciones en gráficos."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Navegar la aplicación mediante sidebar (Priority: P1)

Un usuario autenticado ve un sidebar permanente en el lateral izquierdo de la pantalla con las secciones "Catálogo", "Comerciar", "Portfolio" y "Estrategia". Al seleccionar cualquier sección, el contenido principal de la pantalla se actualiza para mostrar la vista correspondiente. El sidebar permanece visible en todo momento.

**Why this priority**: La navegación es la base estructural de toda la aplicación. Sin ella, el usuario no puede acceder a ninguna de las demás funcionalidades.

**Independent Test**: Verificar que el sidebar se muestra permanentemente y que al seleccionar cada sección, se carga la vista correspondiente.

**Acceptance Scenarios**:

1. **Given** el usuario está autenticado, **When** accede a la aplicación, **Then** ve un sidebar a la izquierda con las secciones "Catálogo", "Comerciar", "Portfolio" y "Estrategia".
2. **Given** el usuario está en cualquier vista, **When** selecciona una sección del sidebar, **Then** el contenido principal cambia a la vista correspondiente y el sidebar permanece visible.
3. **Given** el usuario está en cualquier vista, **When** observa la pantalla, **Then** el sidebar está siempre presente y su tamaño es el necesario para albergar las secciones sin truncar texto.

---

### User Story 2 - Consultar el catálogo unificado de jugadores (Priority: P1)

Un usuario quiere consultar todos los jugadores disponibles en una única tabla, sin discriminar por liga. Puede ver las métricas de cada jugador (nombre, equipo, liga, apariciones, minutos jugados, goles, asistencias, tarjetas amarillas y porcentaje de éxito en los pases). Puede filtrar por liga, equipo y nacionalidad, y ordenar de forma ascendente o descendente por cualquier métrica.

**Why this priority**: El catálogo es la funcionalidad central que permite al usuario conocer a los jugadores antes de comerciar tokens.

**Independent Test**: Verificar que se muestran todos los jugadores en una tabla unificada con las columnas indicadas, que los filtros funcionan y que el ordenamiento ascendente/descendente aplica correctamente.

**Acceptance Scenarios**:

1. **Given** el usuario selecciona "Catálogo" en el sidebar, **When** se carga la vista, **Then** ve una tabla con todos los jugadores sin agrupación por liga.
2. **Given** la tabla de jugadores se muestra, **When** el usuario revisa las columnas, **Then** encuentra: nombre, equipo, liga, apariciones, minutos jugados, goles, asistencias, tarjetas amarillas y porcentaje de éxito en los pases.
3. **Given** la tabla de jugadores se muestra, **When** el usuario filtra por liga, **Then** solo se muestran los jugadores de la liga seleccionada.
4. **Given** la tabla de jugadores se muestra, **When** el usuario filtra por equipo, **Then** solo se muestran los jugadores del equipo seleccionado.
5. **Given** la tabla de jugadores se muestra, **When** el usuario filtra por nacionalidad, **Then** solo se muestran los jugadores de esa nacionalidad.
6. **Given** la tabla de jugadores se muestra, **When** el usuario selecciona ordenar por una métrica de forma ascendente, **Then** los jugadores se reordenan de menor a mayor según esa métrica.
7. **Given** la tabla de jugadores se muestra, **When** el usuario selecciona ordenar por una métrica de forma descendente, **Then** los jugadores se reordenan de mayor a menor según esa métrica.
8. **Given** el usuario ha aplicado filtros, **When** aplica un ordenamiento, **Then** el ordenamiento se aplica sobre los resultados ya filtrados.

---

### User Story 3 - Emitir una orden de compra de tokens (Priority: P1)

Un usuario quiere comprar tokens de un jugador de fútbol. Desde la sección "Comerciar", presiona el botón "Comprar" y se abre un modal donde indica la cantidad de tokens (máximo 100, número entero), selecciona el jugador, y establece una tolerancia de variación de precio. El precio unitario lo determina su Estrategia de valuación. El sistema muestra el precio total sin tolerancia y con tolerancia. Si el precio total con tolerancia supera los créditos disponibles del usuario, puede ver por qué no puede realizar la compra.

**Why this priority**: La compra de tokens es una de las dos operaciones comerciales fundamentales de la plataforma.

**Independent Test**: Abrir el modal de compra, completar los campos, verificar los cálculos de precio total y tolerancia, y confirmar que la validación de créditos disponibles funciona.

**Acceptance Scenarios**:

1. **Given** el usuario no tiene una Estrategia de valuación configurada, **When** presiona el botón "Comprar", **Then** el sistema no permite abrir el modal de compra y muestra un mensaje indicando que debe configurar una Estrategia antes de operar.
2. **Given** el usuario tiene una Estrategia de valuación configurada y está en la sección "Comerciar", **When** presiona el botón "Comprar", **Then** se abre un modal con los campos: cantidad de tokens, jugador de fútbol y tolerancia de variación de precio.
3. **Given** el modal de compra está abierto, **When** el usuario intenta ingresar una cantidad mayor a 100, **Then** el sistema no permite enviar la orden.
4. **Given** el modal de compra está abierto, **When** el usuario completa los campos, **Then** el sistema muestra el precio total sin tolerancia (cantidad × precio unitario según Estrategia) y el precio total con tolerancia (precio total + tolerancia).
5. **Given** el precio total con tolerancia supera los créditos disponibles del usuario, **When** el usuario revisa el modal, **Then** puede ver que no puede realizar la compra porque sus créditos son insuficientes.
6. **Given** el usuario completa una orden de compra válida, **When** confirma la operación, **Then** la orden de compra queda registrada como activa.
7. **Given** el modal de compra está abierto, **When** el usuario selecciona un jugador, **Then** el precio unitario viene determinado por la Estrategia de valuación del usuario.

---

### User Story 4 - Emitir una orden de venta de tokens (Priority: P1)

Un usuario quiere vender tokens de un jugador. Desde la sección "Comerciar", presiona el botón "Vender" y se abre un modal donde indica la cantidad de tokens (no puede superar la cantidad de tokens disponibles), selecciona el jugador, y establece una tolerancia de variación de precio. El precio unitario lo determina su Estrategia. La tolerancia indica cuánto menos está dispuesto a recibir por cada token. Los tokens puestos en venta se bloquean y no pueden ser vendidos nuevamente hasta que la orden se cierre.

**Why this priority**: La venta de tokens es la otra operación comercial fundamental de la plataforma.

**Independent Test**: Abrir el modal de venta, completar los campos, verificar la validación de tokens disponibles, los cálculos de precio y la lógica de bloqueo de tokens.

**Acceptance Scenarios**:

1. **Given** el usuario no tiene una Estrategia de valuación configurada, **When** presiona el botón "Vender", **Then** el sistema no permite abrir el modal de venta y muestra un mensaje indicando que debe configurar una Estrategia antes de operar.
2. **Given** el usuario tiene una Estrategia de valuación configurada y está en la sección "Comerciar", **When** presiona el botón "Vender", **Then** se abre un modal con los campos: cantidad de tokens, jugador de fútbol y tolerancia de variación de precio.
3. **Given** el modal de venta está abierto, **When** el usuario intenta ingresar una cantidad mayor a sus tokens disponibles para ese jugador, **Then** el sistema no permite enviar la orden.
4. **Given** el modal de venta está abierto, **When** el usuario completa los campos, **Then** el sistema muestra el precio total sin tolerancia (cantidad × precio unitario según Estrategia) y el precio total con tolerancia (precio total − tolerancia total).
5. **Given** el usuario tiene 5 tokens del jugador B y una orden de venta activa de 3 tokens de ese jugador, **When** intenta emitir otra orden de venta del jugador B, **Then** solo puede vender hasta 2 tokens.
6. **Given** el usuario completa una orden de venta válida, **When** confirma la operación, **Then** la orden de venta queda registrada como activa y los tokens correspondientes quedan bloqueados.
7. **Given** el modal de venta está abierto, **When** el usuario selecciona un jugador, **Then** el precio unitario viene determinado por la Estrategia de valuación del usuario.

---

### User Story 5 - Ver órdenes activas de compra y venta (Priority: P1)

Un usuario quiere ver sus órdenes de compra y de venta activas al ingresar a la sección "Comerciar".

**Why this priority**: Visualizar las órdenes activas es esencial para que el usuario conozca su situación comercial vigente antes de emitir nuevas órdenes.

**Independent Test**: Acceder a la sección "Comerciar" y verificar que se listan las órdenes de compra activas y las órdenes de venta activas del usuario.

**Acceptance Scenarios**:

1. **Given** el usuario selecciona "Comerciar" en el sidebar, **When** se carga la vista, **Then** ve sus órdenes de compra activas, sus órdenes de venta activas y los botones "Comprar" y "Vender".
2. **Given** el usuario no tiene órdenes activas, **When** se carga la vista, **Then** se muestra una indicación clara de que no hay órdenes activas.

---

### User Story 6 - Consultar el portfolio (Priority: P2)

Un usuario quiere ver su portfolio: para cada jugador, la cantidad de tokens que posee, el precio promedio de compra y el valor actual de todos los tokens. También quiere ver el historial de todas las operaciones concretadas (compras y ventas), con tipo de operación (compra en verde, venta en rojo), jugador, cantidad de tokens, importe total y fecha.

**Why this priority**: El portfolio permite al usuario evaluar el rendimiento de sus inversiones y tomar decisiones informadas sobre futuras transacciones.

**Independent Test**: Acceder a "Portfolio" y verificar que se muestran las tenencias con sus métricas y el historial con codificación de colores.

**Acceptance Scenarios**:

1. **Given** el usuario selecciona "Portfolio" en el sidebar, **When** se carga la vista, **Then** ve una lista de sus tenencias por jugador con: cantidad de tokens, precio promedio de compra y valor actual de todos los tokens.
2. **Given** el usuario está en la vista de Portfolio, **When** revisa el historial de operaciones, **Then** ve cada operación con: tipo (compra en verde, venta en rojo), jugador de fútbol, cantidad de tokens comerciados, importe total y fecha de operación.
3. **Given** el usuario no tiene tokens ni operaciones, **When** accede al Portfolio, **Then** se muestra una indicación clara de que el portfolio está vacío.

---

### User Story 7 - Configurar la estrategia de valuación (Priority: P2)

Un usuario quiere ver su estrategia de valuación actual y, si lo desea, cambiarla. Al presionar "Cambiar Estrategia", puede elegir entre Estrategia A o Estrategia B. Cada estrategia tiene métricas con pesos que el usuario configura mediante inputs. La Estrategia A requiere que la suma de los pesos valga 1. La Estrategia B requiere que la suma de los pesos configurables valga 0.95, ya que el 0.05 restante lo completa la propia Estrategia.

**Why this priority**: La estrategia de valuación determina el precio de los tokens y es requisito previo para emitir órdenes de compra y venta.

**Independent Test**: Acceder a "Estrategia", verificar que se muestra la estrategia actual, cambiar a cada tipo de estrategia configurando pesos válidos e inválidos.

**Acceptance Scenarios**:

1. **Given** el usuario no tiene una Estrategia de valuación configurada, **When** selecciona "Estrategia" en el sidebar, **Then** se muestra un mensaje indicando que debe crear una Estrategia antes de poder operar, junto con la opción de crearla.
2. **Given** el usuario selecciona "Estrategia" en el sidebar y ya tiene una Estrategia configurada, **When** se carga la vista, **Then** ve la estrategia de valuación actual con las métricas afectadas y el peso de cada una.
3. **Given** el usuario está en la vista de Estrategia, **When** presiona "Cambiar Estrategia", **Then** se abre un modal que le permite seleccionar Estrategia A o Estrategia B.
4. **Given** el usuario selecciona la Estrategia A en el modal, **When** aparecen los inputs de métricas, **Then** ve un input por cada una de las métricas: "goals", "assists", "passSuccessPercentage", "aerialsWon", "timesManOfTheMatch".
5. **Given** el usuario configura la Estrategia A, **When** la suma de los pesos no es igual a 1, **Then** el sistema no permite confirmar la configuración.
6. **Given** el usuario configura la Estrategia A, **When** la suma de los pesos es igual a 1, **Then** el sistema permite confirmar y aplica la nueva estrategia.
7. **Given** el usuario selecciona la Estrategia B en el modal, **When** aparecen los inputs de métricas, **Then** ve un input por cada una de las métricas: "passSuccessPercentage", "aerialsWon", "minutesPlayed", "appearances".
8. **Given** el usuario configura la Estrategia B, **When** la suma de los pesos configurables no es igual a 0.95, **Then** el sistema no permite confirmar la configuración.
9. **Given** el usuario configura la Estrategia B, **When** la suma de los pesos configurables es igual a 0.95, **Then** el sistema permite confirmar y aplica la nueva estrategia, completando internamente el 0.05 restante.

---

### User Story 8 - Ver el perfil de usuario (Priority: P2)

Un usuario quiere acceder a su perfil para ver su nombre de usuario. Presiona el botón "Ir a mi perfil" del menú desplegable y el sistema lo lleva a una vista de perfil.

**Why this priority**: El perfil es una funcionalidad básica esperada que actualmente está incompleta (el botón existe pero no hace nada).

**Independent Test**: Presionar "Ir a mi perfil" y verificar que se navega a la vista de perfil donde aparece el nombre de usuario.

**Acceptance Scenarios**:

1. **Given** el usuario abre el menú desplegable de perfil, **When** presiona "Ir a mi perfil", **Then** el sistema navega a la vista de perfil del usuario.
2. **Given** el usuario está en la vista de perfil, **When** revisa la información mostrada, **Then** ve su nombre de usuario.

---

### User Story 9 - Visualizar la evolución de cotización de un jugador (Priority: P3)

Un usuario quiere ver la evolución de la cotización de un jugador en un gráfico de líneas. Utiliza un buscador para ingresar el nombre del jugador. Si se encuentra el jugador, se muestra el gráfico con la evolución de su cotización.

**Why this priority**: La visualización de cotizaciones es valiosa para la toma de decisiones pero no es crítica para las operaciones comerciales básicas.

**Independent Test**: Buscar un jugador por nombre y verificar que se muestra un gráfico de líneas con la evolución de su cotización.

**Acceptance Scenarios**:

1. **Given** el usuario accede al buscador de cotizaciones, **When** ingresa el nombre de un jugador existente, **Then** se muestra un gráfico de líneas con la evolución de su cotización.
2. **Given** el usuario accede al buscador de cotizaciones, **When** ingresa el nombre de un jugador que no existe, **Then** el sistema indica que no se encontró el jugador.

---

### User Story 10 - Visualizar distribución del portfolio en gráfico de torta (Priority: P3)

Un usuario quiere ver el valor actual de todos sus tokens en un gráfico de torta, discriminando por jugador. Si tiene tokens de más de 10 jugadores, los que representan menos en el portfolio se agrupan en una categoría "Otros".

**Why this priority**: La visualización gráfica del portfolio complementa la información tabular pero no es esencial para las operaciones.

**Independent Test**: Acceder al portfolio y verificar que se muestra un gráfico de torta con la distribución por jugador, con agrupación en "Otros" cuando corresponda.

**Acceptance Scenarios**:

1. **Given** el usuario tiene tokens de varios jugadores, **When** accede al Portfolio, **Then** ve un gráfico de torta que muestra el valor actual de sus tokens discriminado por jugador.
2. **Given** el usuario tiene tokens de más de 10 jugadores, **When** se muestra el gráfico de torta, **Then** los jugadores que representan menos se agrupan en una categoría "Otros", mostrando como máximo 10 segmentos individuales más "Otros".
3. **Given** el usuario no tiene tokens, **When** accede al Portfolio, **Then** no se muestra el gráfico de torta o se indica que no hay datos.

### Edge Cases

- Si el usuario no tiene créditos suficientes para cubrir el precio total con tolerancia, debe poder ver el desglose de precios pero no emitir la orden de compra.
- Si el usuario no tiene tokens del jugador seleccionado, no puede emitir una orden de venta para ese jugador.
- Si la cantidad de tokens a comprar es 0 o negativa, el sistema rechaza la orden.
- Si la cantidad de tokens a vender es 0 o negativa, el sistema rechaza la orden.
- Si la tolerancia es negativa, el sistema rechaza la orden.
- Si el usuario intenta ingresar una cantidad de tokens con decimales, el sistema solo acepta números enteros.
- Si el usuario intenta ingresar créditos con más de dos decimales, el sistema aplica el formato de hasta dos decimales.
- Si todos los tokens de un jugador están bloqueados por órdenes de venta activas, el usuario no puede emitir nuevas órdenes de venta para ese jugador.
- Si el usuario tiene tokens de más de 10 jugadores en su portfolio, la agrupación en "Otros" en el gráfico de torta debe incluir a los jugadores de menor representación.
- Si el nombre del jugador buscado en el gráfico de cotizaciones no se encuentra, el sistema muestra un mensaje informativo.
- Si la Estrategia A o B tiene pesos que no suman el valor requerido (1 o 0.95 respectivamente), el sistema muestra un mensaje de error y no permite confirmar.
- Si el usuario no tiene una Estrategia configurada, no debería poder emitir órdenes de compra ni de venta.

## Requirements *(mandatory)*

### Functional Requirements

**Navegación y estructura**

- **FR-001**: La interfaz MUST mostrar un sidebar a la izquierda de la pantalla que permanezca visible en todo momento.
- **FR-002**: El sidebar MUST contener las secciones: "Catálogo", "Comerciar", "Portfolio" y "Estrategia".
- **FR-003**: El sidebar MUST tener el tamaño necesario para albergar todas las secciones sin truncar texto.
- **FR-004**: Al seleccionar una sección del sidebar, MUST actualizarse el contenido principal de la pantalla con la vista correspondiente.

**Catálogo de jugadores**

- **FR-005**: La sección "Catálogo" MUST mostrar todos los jugadores en una única tabla, sin discriminar por liga.
- **FR-006**: Por cada jugador, la tabla MUST mostrar: nombre, equipo, liga, apariciones, minutos jugados, goles, asistencias, tarjetas amarillas y porcentaje de éxito en los pases.
- **FR-007**: El usuario MUST poder filtrar los jugadores por liga, equipo y nacionalidad.
- **FR-008**: El usuario MUST poder ordenar los jugadores de forma ascendente y descendente en base a cada una de sus métricas.

**Perfil de usuario**

- **FR-009**: El botón "Ir a mi perfil" del menú desplegable MUST navegar al usuario a la vista de perfil.
- **FR-010**: En la vista de perfil MUST mostrarse el nombre de usuario del usuario.

**Comerciar — Vista general**

- **FR-011**: La sección "Comerciar" MUST mostrar las órdenes de compra activas del usuario, las órdenes de venta activas del usuario y dos botones: "Comprar" y "Vender".
- **FR-012**: La cantidad de tokens en toda operación MUST ser un número entero.
- **FR-013**: Los créditos MUST expresarse como número decimal de hasta dos decimales.
- **FR-014**: El usuario MUST NOT poder emitir una orden de compra ni de venta si no tiene una Estrategia de valuación configurada. El sistema MUST mostrar un mensaje indicando que debe configurar una Estrategia antes de operar.

**Compra de tokens**

- **FR-015**: Al presionar "Comprar" (con Estrategia configurada) MUST abrirse un modal con los campos: cantidad de tokens a comprar, jugador de fútbol y tolerancia de variación de precio.
- **FR-016**: La cantidad de tokens a comprar MUST NOT superar el número 100.
- **FR-017**: El precio unitario en una orden de compra MUST ser el determinado por la Estrategia de valuación del usuario.
- **FR-018**: La tolerancia aplica al precio total de la orden. El precio total con tolerancia es: (cantidad × precio unitario) + tolerancia.
- **FR-019**: El modal de compra MUST mostrar el precio total sin tolerancia y el precio total con tolerancia.
- **FR-020**: Si el precio total con tolerancia supera los créditos disponibles del usuario, MUST mostrarse una indicación visual que explique por qué no puede realizar la compra.

**Venta de tokens**

- **FR-021**: Al presionar "Vender" (con Estrategia configurada) MUST abrirse un modal con los campos: cantidad de tokens a vender, jugador de fútbol y tolerancia de variación de precio.
- **FR-022**: La cantidad de tokens a vender MUST NOT superar la cantidad de tokens disponibles (no bloqueados) que tenga el usuario para ese jugador.
- **FR-023**: El precio unitario en una orden de venta MUST ser el determinado por la Estrategia de valuación del usuario.
- **FR-024**: La tolerancia de venta indica cuánto menos por token está dispuesto a recibir el usuario. El precio total con tolerancia es: (cantidad × precio unitario) − (cantidad × tolerancia por token).
- **FR-025**: El modal de venta MUST mostrar el precio total sin tolerancia y el precio total con tolerancia.
- **FR-026**: Los tokens puestos en venta MUST quedar bloqueados mientras la orden esté activa, reduciendo la cantidad de tokens disponibles para nuevas órdenes de venta de ese jugador.

**Portfolio**

- **FR-027**: La sección "Portfolio" MUST mostrar, por cada jugador que el usuario posea, la cantidad de tokens, el precio promedio de compra y el valor actual de todos los tokens.
- **FR-028**: La sección "Portfolio" MUST mostrar un historial de todas las operaciones de compra y venta concretadas por el usuario.
- **FR-029**: Cada operación del historial MUST mostrar: tipo de operación (compra en verde, venta en rojo), jugador de fútbol, cantidad de tokens comerciados, importe total y fecha de operación.
- **FR-030**: El portfolio MUST incluir un gráfico de torta que muestre el valor actual de todos los tokens del usuario discriminado por jugador.
- **FR-031**: Si el usuario tiene tokens de más de 10 jugadores, los que representen menos en el portfolio MUST agruparse en una categoría "Otros".

**Estrategia de valuación**

- **FR-032**: La sección "Estrategia" MUST mostrar la estrategia de valuación actual del usuario con las métricas afectadas y el peso de cada una.
- **FR-033**: Si el usuario no tiene una Estrategia configurada, la sección "Estrategia" MUST mostrar un mensaje indicando que debe crear una Estrategia, junto con la opción de crearla.
- **FR-034**: La sección MUST incluir un botón "Cambiar Estrategia" que abra un modal para seleccionar Estrategia A o Estrategia B.
- **FR-035**: Al seleccionar Estrategia A, MUST aparecer un input por cada métrica: "goals", "assists", "passSuccessPercentage", "aerialsWon", "timesManOfTheMatch".
- **FR-036**: La suma de los pesos de la Estrategia A MUST ser igual a 1 para poder confirmar la configuración.
- **FR-037**: Al seleccionar Estrategia B, MUST aparecer un input por cada métrica: "passSuccessPercentage", "aerialsWon", "minutesPlayed", "appearances".
- **FR-038**: La suma de los pesos configurables de la Estrategia B MUST ser igual a 0.95, ya que el 0.05 restante lo completa la propia Estrategia.

**Visualización de datos**

- **FR-039**: MUST existir un gráfico de líneas que muestre la evolución de la cotización de un jugador.
- **FR-040**: MUST existir un buscador donde el usuario pueda ingresar el nombre del jugador para ver su gráfico de cotización.
- **FR-041**: Si se encuentra el jugador buscado, MUST mostrarse el gráfico con la evolución de su cotización. Si no se encuentra, MUST mostrarse un mensaje informativo.

### Key Entities

- **Usuario**: Persona autenticada en la plataforma. Posee un nombre de usuario, créditos disponibles, un portfolio de tokens, órdenes activas y una estrategia de valuación configurada.
- **Jugador**: Futbolista con nombre, equipo, liga, nacionalidad y métricas estadísticas (apariciones, minutos jugados, goles, asistencias, tarjetas amarillas, porcentaje de éxito en pases). Cada jugador tiene una cotización que evoluciona en el tiempo.
- **Token**: Unidad de valor asociada a un jugador que el usuario puede comprar y vender. Se comercia siempre como número entero.
- **Orden de Compra**: Solicitud emitida por un usuario para adquirir una cantidad de tokens de un jugador a un precio determinado por su Estrategia, con una tolerancia de variación de precio que amplía el máximo dispuesto a pagar.
- **Orden de Venta**: Solicitud emitida por un usuario para vender una cantidad de tokens de un jugador a un precio determinado por su Estrategia, con una tolerancia que reduce el mínimo dispuesto a aceptar. Los tokens de la orden quedan bloqueados.
- **Estrategia de Valuación**: Configuración del usuario que define cómo se calcula el precio de los tokens. Puede ser Estrategia A (5 métricas, pesos que suman 1) o Estrategia B (4 métricas configurables con pesos que suman 0.95, más 0.05 completado internamente).
- **Operación Concretada**: Registro histórico de una transacción completada entre dos usuarios, con tipo (compra/venta), jugador, cantidad, importe total y fecha.
- **Cotización**: Valor de un jugador en un momento dado, cuya evolución temporal se visualiza en gráficos de líneas.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El usuario puede navegar entre todas las secciones (Catálogo, Comerciar, Portfolio, Estrategia) sin perder el contexto del sidebar en ningún momento.
- **SC-002**: El catálogo muestra todos los jugadores en una tabla unificada con las 9 columnas especificadas, y los filtros (liga, equipo, nacionalidad) reducen correctamente los resultados.
- **SC-003**: El usuario puede emitir una orden de compra en menos de 2 minutos, visualizando los precios calculados antes de confirmar.
- **SC-004**: El usuario puede emitir una orden de venta en menos de 2 minutos, visualizando los precios calculados antes de confirmar.
- **SC-005**: El bloqueo de tokens por órdenes de venta activas impide que el usuario ofrezca más tokens de los que realmente tiene disponibles, en el 100% de los casos.
- **SC-006**: El portfolio muestra correctamente las tenencias, precios promedio, valores actuales, historial de operaciones y gráfico de torta.
- **SC-007**: La configuración de estrategia valida correctamente las sumas de pesos (1 para Estrategia A, 0.95 para Estrategia B) en el 100% de los intentos.
- **SC-008**: El gráfico de cotización se muestra correctamente cuando el usuario busca un jugador existente.
- **SC-009**: El botón "Ir a mi perfil" navega exitosamente a la vista de perfil donde se muestra el nombre de usuario.
- **SC-010**: Todas las validaciones de datos (enteros para tokens, máximo dos decimales para créditos, máximo 100 tokens en compra, disponibilidad en venta) funcionan correctamente.
- **SC-011**: Un usuario sin Estrategia de valuación configurada no puede emitir órdenes de compra ni de venta, y recibe un mensaje claro indicando que debe configurar una Estrategia.

## Assumptions

- El usuario ya se encuentra autenticado al acceder a las funcionalidades descritas. El flujo de autenticación existente (login/registro) se mantiene sin cambios.
- Los datos de jugadores (métricas, ligas, equipos, nacionalidades) son provistos por el servicio existente de la aplicación. La especificación asume que dicho servicio puede incluir las nuevas métricas requeridas (apariciones, minutos jugados, tarjetas amarillas, porcentaje de éxito en los pases).
- Los créditos del usuario y los tokens que posee son gestionados y provistos por el sistema existente.
- El matching de órdenes (compra contra venta) es procesado por el sistema; esta especificación describe las reglas de matching desde la perspectiva del usuario pero no define el mecanismo de procesamiento.
- El menú desplegable con "Ir a mi perfil" y "Cerrar sesión" continúa existiendo; el sidebar no reemplaza dicho menú sino que complementa la navegación.
- La vista del buscador de cotizaciones de jugadores puede integrarse dentro de la sección "Catálogo" o como una subsección accesible, ya que el usuario no especificó una sección independiente en el sidebar para ella.
- El texto y la interfaz continúan estando en español, como en la versión actual de la aplicación.
- La suma de pesos de la Estrategia B que debe ser 0.95 (no 0.5 como se menciona en la descripción original al decir "el 0.5 restante"). Se interpreta que el usuario se refirió al 0.05 restante (1 − 0.95 = 0.05), manteniendo coherencia con el valor declarado de 0.95.
