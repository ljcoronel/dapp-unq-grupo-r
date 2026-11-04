# Feature Specification: Mercado de tokens de jugadores

**Feature Branch**: `014-token-market`

**Created**: 2026-11-04

**Status**: Draft

**Input**: User description: "Como Usuario, quiero poder comprar y vender tokens de Jugadores para obtener un beneficio económico, mediante órdenes con precio y tolerancia, liquidación entre usuarios, bloqueo de saldos y prioridad de ejecución contra el Superusuario."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Comprar tokens de un jugador (Priority: P1)

Como Usuario, quiero emitir una orden de compra indicando un jugador, una cantidad de tokens, un precio máximo total y una tolerancia al alza, para adquirir tokens cuando exista una oferta compatible.

**Why this priority**: Es la vía principal para que un Usuario incorpore tokens y habilita el intercambio económico.

**Independent Test**: Crear una orden de compra válida, verificar que queda Pendiente y que los créditos quedan bloqueados; crear una venta compatible y comprobar la transferencia, la liquidación y el estado Ejecutada de ambas órdenes.

**Acceptance Scenarios**:

1. **Given** un Usuario con créditos disponibles suficientes, **When** emite una orden de compra de un jugador por una cantidad entera de hasta 100 tokens y un precio válido, **Then** la orden queda Pendiente y se bloquea de su saldo el precio total más la tolerancia al alza.
2. **Given** un Usuario que emite una orden de compra por 20 créditos con una tolerancia de 2 créditos, **When** la orden queda Pendiente, **Then** se bloquean 22 créditos de su saldo.
3. **Given** una orden de compra Pendiente y una orden de venta Pendiente del mismo jugador y cantidad, **When** el precio de la venta está dentro del rango aceptado por la compra, **Then** se ejecuta una única operación, el comprador recibe los tokens, el vendedor recibe los créditos al precio de liquidación y ambas órdenes pasan a Ejecutada.
4. **Given** una orden de compra con tolerancia cero, **When** existe una venta al mismo precio total, **Then** las órdenes coinciden y se ejecutan.
5. **Given** un Usuario con créditos insuficientes para cubrir el precio total más la tolerancia, **When** intenta emitir una orden de compra, **Then** el sistema rechaza la orden y no bloquea créditos.

---

### User Story 2 - Vender tokens de un jugador (Priority: P1)

Como Usuario, quiero emitir una orden de venta indicando un jugador, una cantidad de tokens, un precio mínimo total y una tolerancia a la baja, para vender tokens que ya poseo.

**Why this priority**: Completa el intercambio y permite que los Usuarios obtengan créditos por sus tenencias.

**Independent Test**: Crear una orden de venta válida, verificar que queda Pendiente y que los tokens quedan bloqueados; crear una compra compatible y comprobar la transferencia, la liquidación y el estado Ejecutada de ambas órdenes.

**Acceptance Scenarios**:

1. **Given** un Usuario con suficientes tokens libres del jugador, **When** emite una orden de venta válida, **Then** la orden queda Pendiente y se bloquea la cantidad de tokens ofertada.
2. **Given** una orden de venta Pendiente y una orden de compra Pendiente del mismo jugador y cantidad, **When** el precio de la compra está dentro del rango aceptado por la venta, **Then** se ejecuta una única operación, el comprador recibe los tokens, el vendedor recibe los créditos al precio de liquidación y ambas órdenes pasan a Ejecutada.
3. **Given** un Usuario sin suficientes tokens libres, **When** intenta emitir una orden de venta por una cantidad superior a sus tokens disponibles, **Then** el sistema rechaza la orden y no bloquea tokens.

---

### User Story 3 - Ejecución prioritaria con el Superusuario (Priority: P1)

Como Usuario, quiero que mi orden de compra se ejecute automáticamente contra el Superusuario cuando este tenga suficientes tokens, para obtener liquidez inmediata sin esperar a otro Usuario.

**Why this priority**: El inventario del Superusuario constituye la liquidez inicial prioritaria del mercado.

**Independent Test**: Configurar al Superusuario con suficientes tokens de un jugador, crear una compra válida y comprobar que se crea la venta correspondiente del Superusuario y ambas órdenes quedan Ejecutadas inmediatamente.

**Acceptance Scenarios**:

1. **Given** el Superusuario con al menos la cantidad solicitada de tokens libres, **When** un Usuario crea una orden de compra, **Then** se crea una orden de venta del Superusuario para el mismo jugador y cantidad, con tolerancia cero, y la operación se ejecuta inmediatamente.
2. **Given** el Superusuario con menos tokens que los solicitados, **When** un Usuario crea una orden de compra, **Then** no se crea una venta del Superusuario y la orden de compra continúa Pendiente para buscar otra coincidencia.
3. **Given** una orden creada por el Superusuario, **When** el sistema intenta buscar coincidencias, **Then** nunca se ejecuta contra otra orden del mismo Superusuario.

---

### User Story 4 - Gestionar y consultar órdenes (Priority: P2)

Como Usuario, quiero consultar mis órdenes Pendientes, cancelar una orden propia y consultar el historial de mis órdenes Ejecutadas, para controlar mis operaciones.

**Why this priority**: La visibilidad y el control sobre órdenes bloqueadas son necesarios para operar con confianza.

**Independent Test**: Crear una compra y una venta Pendientes, listarlas, cancelar cada una y verificar la liberación del saldo bloqueado; luego ejecutar una orden y verificar que aparece en el historial Ejecutada.

**Acceptance Scenarios**:

1. **Given** un Usuario con órdenes Pendientes, **When** consulta el registro de órdenes, **Then** solo ve sus órdenes Pendientes de compra y venta.
2. **Given** un Usuario que cancela una orden Pendiente propia, **When** confirma la eliminación, **Then** la orden pasa a Cancelada y se liberan los créditos o tokens bloqueados.
3. **Given** un Usuario con órdenes Ejecutadas, **When** consulta el historial, **Then** ve sus órdenes Ejecutadas de compra y venta, incluyendo jugador, cantidad, precio liquidado y contraparte.
4. **Given** una orden de otro Usuario, **When** un Usuario intenta cancelarla o consultarla como propia, **Then** la operación es rechazada y la orden permanece sin cambios.

### Edge Cases

- La cantidad de tokens debe ser un entero positivo; cantidades cero, negativas o fraccionarias se rechazan.
- Los precios y tolerancias deben ser no negativos y tener como máximo dos decimales; valores con más decimales se rechazan en vez de redondearse silenciosamente.
- Una compra no puede solicitar más de 100 tokens por orden ni comprometer, entre precio total y tolerancia, más créditos libres de los que tiene el comprador.
- Una venta no puede superar los tokens libres del vendedor; tokens ya bloqueados por otra orden no cuentan como disponibles.
- La coincidencia exige el mismo jugador y la misma cantidad de tokens; las órdenes parciales no están contempladas.
- Las órdenes no vencen y permanecen Pendientes hasta ejecutarse o cancelarse.
- Una orden no puede ejecutarse contra otra orden del mismo Usuario.
- Si dos órdenes podrían coincidir simultáneamente, solo una liquidación puede consumirlas; la otra orden permanece Pendiente o busca otra coincidencia válida.
- La operación debe ser atómica: si falla cualquier transferencia o actualización de estado, no se conserva una liquidación parcial ni se pierden bloqueos.
- La tolerancia cero del Superusuario exige que los límites del rango sean inclusivos.
- Si no hay intersección de rangos de precio, ambas órdenes permanecen Pendientes.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST permitir a un Usuario crear una orden de compra para un único jugador, una cantidad entera positiva de tokens, un precio total y una tolerancia al alza.
- **FR-002**: El sistema MUST permitir a un Usuario crear una orden de venta para un único jugador, una cantidad entera positiva de tokens, un precio total y una tolerancia a la baja.
- **FR-003**: El sistema MUST validar que una orden de compra no solicite más de 100 tokens y que la suma de su precio total más la tolerancia al alza no supere los créditos libres del Usuario.
- **FR-004**: El sistema MUST validar que una orden de venta no oferte más tokens libres de los que posee el Usuario.
- **FR-005**: El sistema MUST aceptar créditos y tolerancias con un máximo de dos decimales y rechazar valores negativos o con mayor precisión.
- **FR-006**: Toda orden creada correctamente MUST comenzar en estado Pendiente.
- **FR-007**: Al crear una compra Pendiente, el sistema MUST bloquear de los créditos libres del comprador un importe igual al precio total más la tolerancia al alza mientras la orden permanezca Pendiente.
- **FR-008**: Al crear una venta Pendiente, el sistema MUST bloquear de los tokens libres del vendedor la cantidad comprometida mientras la orden permanezca Pendiente.
- **FR-009**: El sistema MUST considerar compatible una compra y una venta solo cuando pertenecen a Usuarios distintos, refieren al mismo jugador, tienen la misma cantidad y sus rangos de precio se superponen en al menos un valor válido.
- **FR-010**: El rango de una compra MUST incluir los precios desde su precio total hasta su precio total más la tolerancia al alza; el rango de una venta MUST incluir los precios desde su precio total menos la tolerancia a la baja hasta su precio total.
- **FR-011**: Al coincidir órdenes, el sistema MUST elegir un precio total dentro de ambos rangos, con como máximo dos decimales. Como criterio determinista, utilizará el precio base de la orden que estaba Pendiente antes de iniciar la coincidencia, siempre que pertenezca a ambos rangos.
- **FR-012**: Al ejecutar una operación, el comprador MUST recibir los tokens, se le MUST debitar el importe liquidado y el vendedor MUST recibir ese importe y se le MUST debitar la cantidad de tokens; los créditos bloqueados por la compra que no se utilicen por ser el precio final inferior al importe reservado deben desbloquearse.
- **FR-013**: Al ejecutar una operación, ambas órdenes consumidas MUST pasar a estado Ejecutada y registrar jugador, cantidad, precio liquidado, comprador, vendedor y fecha de ejecución.
- **FR-014**: La liquidación de una coincidencia MUST ser atómica y no debe dejar transferencias parciales, órdenes Ejecutadas incompletas ni bloqueos inconsistentes.
- **FR-015**: Cuando un Usuario cree una orden de compra, el sistema MUST priorizar la ejecución contra el Superusuario si este tiene suficientes tokens libres del jugador solicitado.
- **FR-016**: Para esa ejecución prioritaria, el sistema MUST crear una orden de venta a nombre del Superusuario con la misma cantidad y jugador, tolerancia cero, y ejecutarla inmediatamente contra la compra del Usuario.
- **FR-017**: Si el Superusuario no tiene suficientes tokens libres, el sistema MUST omitir la venta prioritaria y continuar la búsqueda normal de una orden de venta compatible.
- **FR-018**: El sistema MUST impedir que una orden de compra y una de venta del mismo Usuario se ejecuten entre sí, incluido el Superusuario.
- **FR-019**: El Usuario MUST poder cancelar una orden Pendiente propia; al cancelarla, la orden MUST pasar a Cancelada y liberarse todo saldo bloqueado.
- **FR-020**: El sistema MUST rechazar la cancelación de órdenes de otros Usuarios y la cancelación de órdenes ya Ejecutadas o Canceladas.
- **FR-021**: El sistema MUST permitir consultar a cada Usuario el registro de sus órdenes Pendientes de compra y venta.
- **FR-022**: El sistema MUST permitir consultar a cada Usuario el historial de sus órdenes Ejecutadas de compra y venta, con sus datos de liquidación.
- **FR-023**: Las órdenes MUST permanecer Pendientes sin vencimiento hasta que se ejecuten o sean canceladas por su propietario.

### Key Entities

- **Usuario**: Participante del mercado con saldo de créditos y tenencias de tokens libres o bloqueados.
- **Superusuario**: Usuario especial que aporta liquidez prioritaria mediante sus tenencias disponibles.
- **Jugador**: Único activo deportivo al que refiere cada orden y token.
- **Orden de compra**: Compromiso Pendiente o finalizado para adquirir una cantidad de tokens a un precio total y tolerancia al alza.
- **Orden de venta**: Compromiso Pendiente o finalizado para transferir una cantidad de tokens a un precio total y tolerancia a la baja.
- **Operación**: Liquidación de una orden de compra y una de venta compatibles, con precio elegido dentro de ambos rangos.
- **Saldo de créditos**: Crédito total del Usuario, distinguido entre disponible, bloqueado y transferido.
- **Tenencia de tokens**: Cantidad de tokens de un jugador del Usuario, distinguida entre disponible, bloqueada y transferida.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El 100% de las órdenes válidas de compra y venta queda Pendiente, Ejecutada o Cancelada según la transición realizada, sin estados ambiguos.
- **SC-002**: El 100% de las liquidaciones ejecutadas conserva la cantidad de tokens, el importe de créditos y la titularidad de comprador y vendedor, sin creación ni pérdida neta de activos.
- **SC-003**: El 100% de las órdenes de compra y venta incompatibles por jugador, cantidad, rango o propietario permanece sin ejecutar.
- **SC-004**: El 100% de las compras que pueden ser abastecidas por el Superusuario se ejecuta automáticamente antes de quedar disponible para coincidencias ordinarias.
- **SC-005**: En pruebas de operación normal, al menos el 95% de los Usuarios puede crear una orden válida, consultar su estado y cancelar una orden Pendiente sin asistencia.
- **SC-006**: En el 100% de las consultas de registro e historial, un Usuario solo puede observar sus propias órdenes y obtiene datos suficientes para identificar cada operación.
- **SC-007**: El 100% de los importes persistidos y liquidados conserva como máximo dos decimales, y el 100% de las cantidades de tokens liquidadas es entero.

## Assumptions

- Los rangos de precio son inclusivos en ambos extremos; esto permite que tolerancia cero signifique coincidencia exacta.
- En una orden de compra, el importe reservado mientras está Pendiente es `precio total + tolerancia al alza`; por ejemplo, una orden de 20 créditos con tolerancia de 2 créditos bloquea 22 créditos.
- La orden que ya estaba Pendiente antes de iniciarse la coincidencia se considera la orden base para determinar el precio de liquidación; si ese precio no pertenece al rango contrapuesto, se elige un valor de dos decimales dentro de la intersección.
- Una orden se consume completa: no se realizan ejecuciones parciales ni se dividen órdenes.
- Cada Usuario comienza con 100 créditos disponibles, salvo que una funcionalidad existente de administración establezca otro saldo explícito.
- El Superusuario se identifica por las reglas de identidad ya existentes y sus tenencias libres pueden utilizarse como inventario de liquidez.
- Autenticación, autorización, alta de Usuarios, alta de Jugadores y administración de tenencias existentes quedan fuera del alcance y se reutilizan.
- La visualización puede ordenar las órdenes por fecha de creación descendente; no se requiere vencimiento automático.
