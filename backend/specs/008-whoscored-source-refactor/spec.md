# Especificación de funcionalidad: Refactor de fuente a WhoScored

**Rama de funcionalidad**: `008-whoscored-source-refactor`

**Fecha de creación**: 2026-10-07

**Estado**: Borrador

**Entrada**: Descripción del usuario: "Como desarrollador, quiero hacer un refactor a mi programa. Quiero que mi fuente de datos para jugadores de futbol deje de ser FootballData y en su lugar sea WhoScored. La extracción de datos de FootballData debe dejar de ser utilizada en su totalidad."

## Escenarios de usuario y pruebas

### Historia de usuario 1 - Obtener el catálogo desde WhoScored (Prioridad: P1)

Como desarrollador y consumidor del catálogo, quiero que los datos de jugadores se obtengan exclusivamente desde WhoScored para ampliar las métricas disponibles de cada jugador y reducir las métricas asociadas a cada liga.

**Por qué esta prioridad**: Cambiar la fuente de datos es el objetivo principal del refactor y debe conservar el valor del catálogo actual.

**Prueba independiente**: Ejecutar la actualización del catálogo y comprobar que los jugadores publicados provienen de WhoScored, incluyen las nuevas métricas disponibles y no se realiza ninguna extracción desde FootballData.

**Escenarios de aceptación**:

1. **Dado** que WhoScored entrega datos válidos para las ligas configuradas, **cuando** se actualiza el catálogo, **entonces** los jugadores quedan disponibles con las métricas actuales y las métricas adicionales que la fuente pueda ofrecer.
2. **Dado** que la actualización se ejecuta con WhoScored disponible, **cuando** finaliza, **entonces** ninguna operación de extracción, solicitud o fallback utiliza FootballData.
3. **Dado** que un jugador de WhoScored tiene un dato ausente, **cuando** se publica en el catálogo, **entonces** se conserva la regla vigente para representar ese dato sin desplazar las demás columnas.

---

### Historia de usuario 2 - Mantener las consultas existentes (Prioridad: P1)

Como visitante o consumidor de la API, quiero seguir consultando el catálogo de jugadores con las mismas cinco ligas y reglas de presentación, mientras cada jugador expone un conjunto ampliado de métricas.

**Por qué esta prioridad**: El refactor debe cambiar el proveedor sin introducir una regresión en las capacidades que ya utilizan los consumidores.

**Prueba independiente**: Consultar el listado y el detalle de jugadores después de la actualización, verificando que las rutas públicas, la agrupación por liga, la ausencia de límite artificial de jugadores y la representación de datos faltantes funcionan.

**Escenarios de aceptación**:

1. **Dado** que existen jugadores sincronizados desde WhoScored, **cuando** se consulta el listado, **entonces** se muestran las cinco ligas soportadas y todos los jugadores válidos que WhoScored haya entregado para cada liga.
2. **Dado** que existe un jugador sincronizado desde WhoScored, **cuando** se consulta su detalle por identificador, **entonces** se devuelve el mismo registro normalizado que utiliza el listado.
3. **Dado** que se contrae o expande una liga, **cuando** se interactúa con su nombre, **entonces** el comportamiento del catálogo permanece igual y no afecta a las demás ligas.

---

### Historia de usuario 3 - Conservar datos ante fallos de WhoScored (Prioridad: P2)

Como operador del sistema, quiero que un fallo temporal de WhoScored se informe claramente y no destruya el último catálogo válido almacenado.

**Por qué esta prioridad**: La disponibilidad del catálogo no debe depender de que el proveedor responda correctamente en cada actualización.

**Prueba independiente**: Simular una respuesta vacía, inválida o fallida de WhoScored y comprobar que se informa el problema y se conserva el snapshot anterior válido.

**Escenarios de aceptación**:

1. **Dado** que WhoScored no responde o devuelve un error, **cuando** se intenta actualizar una liga, **entonces** se conserva el snapshot anterior de esa liga y se registra un fallo comprensible.
2. **Dado** que WhoScored devuelve datos vacíos o inválidos, **cuando** se intenta actualizar una liga, **entonces** no se reemplazan datos válidos por un catálogo vacío o corrupto.
3. **Dado** que una liga se actualiza correctamente y otra falla, **cuando** termina la actualización, **entonces** solo se reemplaza la liga exitosa y se conserva la última versión válida de la liga fallida.

### Casos límite

- WhoScored no entrega jugadores para una liga configurada.
- WhoScored entrega una cantidad variable de jugadores para una liga.
- La fuente entrega nombres, ligas, equipos o estadísticas incompletas.
- La fuente entrega dos registros con el mismo identificador.
- WhoScored aplica límites de acceso, demora o cambia temporalmente el formato de una respuesta.
- Existe configuración, código, tarea programada, documentación o prueba heredada que todavía intenta activar FootballData.
- La actualización falla después de haber recibido datos válidos para algunas ligas, pero no para todas.

## Requisitos

### Requisitos funcionales

- **RF-001**: El sistema DEBE utilizar WhoScored como única fuente externa activa para obtener los datos de jugadores.
- **RF-002**: El sistema DEBE transformar los datos de WhoScored al modelo de jugadores que ya utilizan las consultas y el catálogo, conservando identificador, nombre, liga, equipo, posición y estadísticas soportadas.
- **RF-003**: El sistema DEBE actualizar desde WhoScored las cinco ligas actualmente soportadas: Premier League, Bundesliga, Primera División, Serie A y Ligue 1.
- **RF-003a**: El sistema DEBE conservar las cinco ligas como agrupaciones del catálogo, pero DEBE reducir la información propia de cada liga a sus datos de identificación y asociación necesarios para el catálogo, sin exigir métricas agregadas de liga que ya no sean provistas.
- **RF-003b**: El sistema DEBE ampliar la información de cada jugador con todas las métricas válidas que WhoScored pueda ofrecer y que sean compatibles con el modelo del catálogo, además de las métricas ya existentes.
- **RF-004**: El sistema NO DEBE realizar solicitudes, scraping, extracción, sincronización, fallback ni actualización de datos desde FootballData.
- **RF-005**: El sistema NO DEBE requerir credenciales, configuración, tareas programadas ni componentes de FootballData para iniciar, actualizar o consultar el catálogo.
- **RF-006**: El sistema DEBE conservar las consultas públicas existentes del catálogo y del detalle de jugadores, incluyendo sus reglas actuales de identificación, filtros y respuestas.
- **RF-007**: El sistema NO DEBE imponer un límite artificial de cantidad de jugadores por liga; el catálogo DEBE incluir todos los jugadores válidos que WhoScored pueda ofrecer para cada una de las cinco ligas.
- **RF-008**: El sistema DEBE representar los datos faltantes de acuerdo con la regla vigente del catálogo, sin convertir valores numéricos iguales a cero en datos ausentes, tanto para métricas existentes como para métricas adicionales.
- **RF-009**: Cuando una actualización de WhoScored sea exitosa y contenga datos válidos, el sistema DEBE reemplazar el snapshot previo de esa liga por los datos nuevos normalizados.
- **RF-010**: Cuando una actualización de WhoScored falle, sea vacía o inválida, el sistema DEBE conservar el último snapshot válido de esa liga y comunicar el fallo mediante el mecanismo existente.
- **RF-011**: El sistema DEBE aislar el resultado de cada liga durante una actualización, de modo que el fallo de una liga no elimine ni invalide snapshots válidos de las demás.
- **RF-012**: El sistema DEBE rechazar o excluir registros que no puedan identificarse de forma confiable, sin crear jugadores ficticios ni identificadores alternativos.
- **RF-013**: La eliminación de FootballData DEBE abarcar código ejecutable, configuración, credenciales, tareas de extracción, dependencias, documentación operativa y pruebas que mantengan su uso activo.
- **RF-014**: La aplicación DEBE conservar el funcionamiento de la información de usuarios y de cualquier otra funcionalidad no relacionada con la fuente de jugadores.

### Entidades principales

- **Fuente de jugadores**: Proveedor externo activo que entrega la información normalizada para el catálogo; para esta funcionalidad es WhoScored.
- **Jugador**: Registro publicado por el sistema con identificador, identidad, liga, equipo, posición, estadísticas existentes y métricas adicionales disponibles desde WhoScored.
- **Snapshot de liga**: Último conjunto válido de jugadores almacenado para una liga y disponible para consultas cuando una actualización posterior falla.
- **Liga**: Competencia soportada por el catálogo, identificada por sus datos básicos y asociada a todos sus jugadores; no requiere métricas agregadas que hayan sido retiradas del alcance.

## Criterios de éxito

### Resultados medibles

- **CS-001**: En el 100% de las actualizaciones exitosas, los jugadores publicados se obtienen de WhoScored y no se realiza ninguna solicitud ni extracción desde FootballData.
- **CS-002**: El 100% de las consultas existentes del listado y detalle de jugadores continúa respondiendo con el contrato y las reglas funcionales vigentes.
- **CS-003**: En el 100% de las ligas configuradas, una actualización exitosa reemplaza el snapshot previo únicamente con registros válidos de WhoScored.
- **CS-004**: En el 100% de los fallos, respuestas vacías o respuestas inválidas de WhoScored, el último snapshot válido de cada liga afectada permanece consultable.
- **CS-005**: En el 100% de los catálogos publicados se muestran todos los jugadores válidos entregados por WhoScored para cada liga, sin truncamiento artificial, y se conservan las reglas de datos faltantes.
- **CS-006**: El 100% de los jugadores publicados conserva las métricas existentes y expone las métricas adicionales válidas entregadas por WhoScored, mientras las ligas solo exponen los datos de identificación y asociación definidos para esta versión.
- **CS-007**: Una revisión de código, configuración, tareas, dependencias y pruebas no encuentra ningún uso activo de FootballData como fuente, fallback o requisito de ejecución.
- **CS-008**: Al menos el 95% de las consultas de catálogo ejecutadas después del refactor producen una respuesta equivalente en navegación y agrupación a la versión anterior, con las métricas de jugador ampliadas y las métricas de liga reducidas según el nuevo alcance.
- **CS-009**: El 100% de las pruebas de regresión de usuarios y del catálogo no relacionado con el proveedor externo continúa pasando tras el cambio.

## Supuestos

- WhoScored puede proporcionar información suficiente para normalizar los campos que ya consume el catálogo.
- El alcance se limita al cambio de fuente y a los ajustes necesarios para ampliar las métricas de jugador y reducir las métricas de liga; no agrega nuevas ligas, filtros ni pantallas.
- Las métricas exactas pueden variar según lo que WhoScored entregue en cada actualización; el sistema no debe inventar métricas ausentes ni bloquear la sincronización por una métrica opcional faltante.
- El almacenamiento local y la estrategia vigente de snapshots se conservan, salvo los cambios necesarios para eliminar la dependencia de FootballData.
- El identificador de jugador que entregue WhoScored será estable y se utilizará según las reglas de identidad ya definidas por el dominio.
- Los mensajes de error, observabilidad y actualización parcial reutilizan los mecanismos existentes del sistema.
- No se requiere migrar históricos originados exclusivamente en FootballData para presentarlos como datos nuevos de WhoScored; cualquier histórico conservado debe identificarse según las reglas vigentes del sistema.
