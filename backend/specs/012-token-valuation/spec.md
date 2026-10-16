# Feature Specification: Valuacion de tokens de jugadores

**Feature Branch**: `012-token-valuation`

**Created**: 2026-10-16

**Status**: Draft

**Input**: User description: "Diseñar tokens para asignar valor a cada Jugador, dos estrategias de valuación configurables por Usuario, valuación semanal y tenencia inicial por un Superusuario."

## Clarifications

### Session 2026-10-16

- Q: ¿Qué tipo recibe el método `WhoScoredScrapingService.scrape` para la cantidad de jugadores? → A: `String`; la ejecución semanal debe invocarlo con `"50"`.
- Q: ¿Debe existir una forma manual de ejecutar la actualización fuera del Scheduler? → A: Sí; exponer `POST /quotes/recalculate` sin autenticación ni otra protección.
- Q: ¿Qué valor debe utilizarse cuando un Jugador no tenga alguna métrica requerida por su Estrategia? → A: Cero; la métrica faltante se normaliza como 0 y el Jugador continúa siendo valuado.
- Q: ¿Qué debe conservarse cuando se ejecuta una valuación dominical o manual? → A: Un historial del valor unitario de cada Token de cada Jugador que el Usuario poseía en ese momento.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Configurar una estrategia y valuar tokens (Priority: P1)

Como Usuario, quiero elegir una de las dos Estrategias de Valuacion disponibles y asignar el peso de cada metrica para que mis Token de cada Jugador tengan un valor acorde a mis preferencias.

**Why this priority**: Sin una estrategia configurada no existe una valuacion personalizada para los Token del Usuario. Es el flujo principal que entrega valor.

**Independent Test**: Se puede seleccionar una estrategia, completar sus pesos y consultar que cada Token de un mismo Jugador del Usuario tenga el mismo valor en creditos, con el valor guardado en el historial de la valuacion.

**Acceptance Scenarios**:

1. **Given** un Usuario sin estrategia configurada, **When** selecciona una de las dos estrategias, asigna un peso valido a cada metrica y confirma, **Then** la estrategia queda configurada como actual y sus Token se valuan inmediatamente usando las metricas vigentes de los Jugadores.
2. **Given** un Usuario con Token de dos Jugadores y una estrategia configurada, **When** consulta sus Token, **Then** cada Token de un Jugador muestra el mismo valor que los demas Token de ese Jugador, aunque pueda diferir del valor del otro Jugador.
3. **Given** que dos Usuarios configuran la misma estrategia con pesos diferentes, **When** se valuan sus Token del mismo Jugador, **Then** cada Usuario puede obtener un valor diferente para ese Jugador.
4. **Given** un Usuario que intenta confirmar pesos invalidos, **When** confirma la configuracion, **Then** el sistema rechaza la configuracion, informa el motivo y conserva la estrategia anterior, si existia.

---

### User Story 2 - Actualizar la estrategia para la proxima valuacion (Priority: P1)

Como Usuario que ya tiene una estrategia configurada, quiero elegir la otra estrategia o modificar la estrategia actual para que el cambio se aplique de forma programada y pueda anticipar desde cuando regira el nuevo valor.

**Why this priority**: Evita cambios retroactivos y asegura que todos los Token se comparen con una valuacion consistente durante cada periodo.

**Independent Test**: Se puede solicitar un cambio de estrategia, comprobar que queda pendiente y verificar que se vuelve actual en el siguiente domingo a las 03:00, junto con la valuacion correspondiente.

**Acceptance Scenarios**:

1. **Given** un Usuario con una estrategia actual configurada, **When** configura la otra estrategia y confirma el cambio, **Then** la nueva configuracion queda como unica estrategia pendiente y se informa que comenzara a regir el proximo domingo a las 03:00.
2. **Given** un Usuario con una estrategia actual configurada, **When** modifica sus pesos y confirma la configuracion, **Then** la modificacion queda como unica estrategia pendiente para el proximo domingo a las 03:00 y no altera los valores actuales antes de esa fecha.
3. **Given** un Usuario con un cambio pendiente, **When** modifica nuevamente la estrategia pendiente o solicita otra configuracion valida, **Then** la configuracion pendiente anterior se reemplaza por la nueva y sigue existiendo como maximo una estrategia pendiente.
4. **Given** un Usuario con un cambio pendiente, **When** llega el proximo domingo a las 03:00, **Then** la estrategia pendiente pasa a ser actual y sus Token se valuan con ella despues de actualizar la informacion de los Jugadores.
5. **Given** un Usuario sin ninguna estrategia seleccionada y configurada, **When** intenta dejar una estrategia pendiente sin completar una primera configuracion, **Then** el sistema rechaza la solicitud y exige configurar primero una estrategia actual.

---

### User Story 3 - Ejecutar la valuacion semanal de todos los usuarios (Priority: P1)

Como sistema, necesito actualizar la informacion de los Jugadores y luego valuar los Token de todos los Usuarios cada domingo a las 03:00 para mantener los valores sincronizados con las metricas mas recientes.

**Why this priority**: La valuacion periodica es el mecanismo comun que mantiene vigente el mercado y aplica los cambios programados de estrategia.

**Independent Test**: Con metricas nuevas y estrategias configuradas, se ejecuta una ventana dominical y se verifica el orden de actualizacion y valuacion, y que cada Usuario reciba valores basados en su propia estrategia.

**Acceptance Scenarios**:

1. **Given** que existen Jugadores, Usuarios con estrategias configuradas y Token en tenencia, **When** llega el domingo a las 03:00, **Then** primero se actualiza la informacion de los Jugadores y luego se valuan los Token de cada Usuario con su estrategia actual.
2. **Given** que dos Usuarios tienen distintas configuraciones, **When** se completa la valuacion semanal, **Then** cada conjunto de Token queda valuado con la configuracion correspondiente a su Usuario.
3. **Given** un Usuario sin estrategia elegida y configurada, **When** se ejecuta la valuacion semanal, **Then** sus Token no reciben una valuacion por estrategia y el sistema conserva su estado pendiente sin afectar a los demas Usuarios.

---

### User Story 4 - Administrar la cantidad y tenencia inicial de tokens (Priority: P2)

Como Superusuario, quiero que el sistema cree y mantenga exactamente 100 Token por Jugador bajo mi Tenencia inicial, para disponer del inventario completo que luego podran poseer los Usuarios.

**Why this priority**: Define la unidad de inventario del mercado y la tenencia inicial requerida para todos los Jugadores.

**Independent Test**: Para cada Jugador se consulta el inventario inicial y se comprueba que contiene exactamente 100 Token, todos bajo la Tenencia del Superusuario.

**Acceptance Scenarios**:

1. **Given** un Jugador registrado en el sistema, **When** se crea o inicializa su inventario, **Then** existen exactamente 100 Token asociados a ese Jugador y todos pertenecen al Superusuario.
2. **Given** un Jugador con 100 Token, **When** se procesa una valuacion, **Then** no se crean ni eliminan Token y todos conservan el mismo valor por Token para ese Jugador.

### Edge Cases

- Si un peso es negativo, falta una metrica, excede el limite permitido o la suma de pesos no es valida, la configuracion se rechaza sin modificar la configuracion vigente.
- Si la actualizacion de informacion de Jugadores falla o queda incompleta durante la ventana dominical, no se debe realizar una valuacion con informacion parcialmente actualizada; se debe conservar la ultima valuacion valida e informar el fallo.
- Si una valuacion dominical se reintenta, no debe duplicar Token ni aplicar dos veces el mismo cambio de estrategia.
- Si un Usuario configura una estrategia antes de la primera ventana dominical, sus Token se valuan inmediatamente; si luego solicita otra configuracion, esta queda pendiente para la siguiente ventana.
- Si un Usuario ya tiene una estrategia pendiente y confirma otra configuracion valida, la nueva configuracion reemplaza la pendiente anterior, sin acumular mas de una.
- Un Usuario no puede tener una estrategia pendiente mientras no tenga una estrategia actual seleccionada y configurada.
- Si un Jugador no tiene una o mas metricas requeridas para una estrategia, cada metrica faltante se considera 0 y sus Token reciben igualmente un valor calculado.
- Los Token de un Jugador pueden tener un valor distinto al de otro Jugador, pero nunca valores distintos entre si dentro del mismo Usuario y Jugador.
- El historial de una valuacion debe reflejar la cantidad de Token que el Usuario poseia al momento de ejecutarse; una valuacion posterior agrega un nuevo registro y no sobrescribe el historial anterior.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST mantener exactamente 100 Token por cada Jugador, sin crear o eliminar Token como consecuencia de una valuacion.
- **FR-002**: El sistema MUST usar "creditos" como unidad monetaria y mantener un unico valor por Token para cada combinacion de Usuario y Jugador en una valuacion.
- **FR-003**: El sistema MUST permitir que el Superusuario tenga inicialmente la Tenencia de los 100 Token de cada Jugador.
- **FR-004**: El sistema MUST ofrecer exactamente dos Estrategias de Valuacion predefinidas a todos los Usuarios.
- **FR-005**: Cada Estrategia MUST declarar las metricas de Jugador que utiliza, sin fijar por si misma el peso de esas metricas.
- **FR-006**: El sistema MUST permitir que cada Usuario configure un peso para cada metrica requerida por la Estrategia elegida.
- **FR-007**: El sistema MUST validar que los pesos de una configuracion sean no negativos, incluyan todas las metricas requeridas y cumplan la regla de suma definida para la configuracion.
- **FR-008**: El sistema MUST calcular el valor de cada Token usando la Estrategia y los pesos configurados por el Usuario sobre las metricas vigentes del Jugador.
- **FR-008a**: Si al Jugador le falta una metrica requerida por la Estrategia, el sistema MUST utilizar el valor 0 para esa metrica y continuar el calculo de su valor.
- **FR-008b**: Cada ejecucion dominical o manual de valuacion MUST guardar un registro historico del valor unitario de cada Token de cada Jugador que cada Usuario poseia en el momento de la ejecucion, junto con la cantidad poseida y la fecha de la valuacion.
- **FR-009**: El sistema MUST aislar la configuracion de cada Usuario, de modo que cambiar los pesos de un Usuario no modifique los valores de los Token de otro Usuario.
- **FR-010**: El sistema MUST valuar inmediatamente los Token del Usuario cuando este configura por primera vez una Estrategia valida.
- **FR-011**: El sistema MUST permitir que un Usuario con una Estrategia actual seleccionada y configurada elija y configure la otra Estrategia, o modifique los pesos de la Estrategia actual.
- **FR-012**: El sistema MUST registrar como maximo una Estrategia pendiente por Usuario; una nueva configuracion valida reemplazara la configuracion pendiente anterior si existiera.
- **FR-013**: El sistema MUST programar toda seleccion o modificacion posterior a la primera configuracion para el proximo domingo a las 03:00 y no alterar la valuacion actual antes de ese momento.
- **FR-014**: El sistema MUST rechazar la creacion de una Estrategia pendiente cuando el Usuario no tenga ninguna Estrategia seleccionada y configurada como actual.
- **FR-015**: Cada domingo a las 03:00 el sistema MUST actualizar primero la informacion conocida de los Jugadores y luego valuar los Token de todos los Usuarios con estrategia configurada.
- **FR-015a**: El sistema MUST exponer `POST /quotes/recalculate` sin autenticacion ni otra proteccion; el endpoint MUST ejecutar el mismo flujo que la ventana dominical, incluyendo `scrape("50")` antes del recálculo.
- **FR-016**: La ejecucion dominical MUST aplicar la configuracion pendiente, si existe, antes de calcular la nueva valuacion; si no existe, MUST conservar la Estrategia actual.
- **FR-017**: El sistema MUST dejar sin valuacion por estrategia a los Usuarios que no tengan una estrategia elegida y configurada, sin impedir la valuacion de los demas Usuarios.
- **FR-018**: El sistema MUST conservar la ultima valuacion valida y comunicar el error si no puede completar la actualizacion de Jugadores o la valuacion semanal.
- **FR-019**: El sistema MUST permitir consultar, por Usuario y Jugador, la estrategia aplicada, la configuracion vigente o pendiente, la fecha de vigencia y el valor por Token en creditos.
- **FR-020**: Esta funcionalidad MUST mantener separadas las reglas de valuacion de las reglas de transferencia o adquisicion de Tenencia; la forma en que los Usuarios reciben Token queda fuera de alcance.

### Key Entities

- **Jugador**: Persona cuyos datos y metricas determinan el valor de sus Token.
- **Token**: Unidad indivisible asociada a un Jugador; existen exactamente 100 por Jugador y todos tienen el mismo valor por Usuario y periodo de valuacion.
- **Usuario**: Actor que configura una Estrategia y consulta los valores de los Token que posee.
- **Superusuario**: Actor que mantiene la Tenencia inicial de todos los Token de todos los Jugadores.
- **Estrategia de Valuacion**: Una de las dos alternativas ofrecidas por la aplicacion, definida por el conjunto de metricas que utiliza.
- **Configuracion de Estrategia**: Eleccion de una Estrategia y sus pesos por Usuario, con estado actual o pendiente y una fecha de aplicacion; cada Usuario puede tener como maximo una configuracion pendiente.
- **Valuacion**: Resultado del calculo del valor por Token para un Usuario y Jugador en una fecha determinada, expresado en creditos.
- **Historial de Valuacion**: Registro inmutable del valor unitario y la cantidad de Token poseida por un Usuario y Jugador en cada ejecucion dominical o manual.
- **Metrica de Jugador**: Dato actualizado del Jugador utilizado por una Estrategia para calcular un valor.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El 100% de los Jugadores registrados tiene exactamente 100 Token asociados y el 100% de esos Token pertenece inicialmente al Superusuario.
- **SC-002**: En cada ventana dominical, el 100% de los Usuarios con estrategia configurada recibe una valuacion basada en la informacion de Jugadores actualizada en esa misma ventana, o un estado explicito de error sin valuacion parcial.
- **SC-003**: Para el 100% de las consultas, los Token del mismo Jugador pertenecientes al mismo Usuario muestran un unico valor por Token dentro del periodo consultado.
- **SC-004**: Un Usuario puede completar su primera configuracion valida y consultar la valuacion inicial en menos de 3 minutos en al menos el 95% de los intentos de prueba.
- **SC-005**: El 100% de los cambios solicitados despues de la primera configuracion permanece sin efecto hasta el proximo domingo a las 03:00 y se aplica en esa ventana si la informacion de Jugadores se actualiza correctamente.
- **SC-006**: Al menos el 95% de los Usuarios de prueba puede distinguir que estrategia, pesos, fecha de vigencia y valor por Token se encuentran activos o pendientes sin asistencia.

## Assumptions

- Los pesos se expresan como porcentajes no negativos y deben sumar exactamente 100; cada estrategia define previamente sus metricas requeridas.
- Los valores en creditos pueden tener hasta dos decimales para su consulta y comparacion.
- La hora de referencia para "domingo a las 03:00" es la zona horaria de negocio del sistema, asumida como America/Argentina/Buenos_Aires.
- La informacion de Jugadores y sus metricas proviene de una fuente existente y su actualización forma parte de la ventana de valuacion, sin definir aqui el mecanismo de obtencion.
- La aplicacion controla la existencia de un unico Superusuario para la Tenencia inicial.
- Las transferencias, compras, ventas y cualquier otra forma de asignar Token a Usuarios quedan fuera del alcance de esta especificacion.
- La autenticacion, autorizacion general y administracion de Jugadores existentes se reutilizan de las funcionalidades actuales.
