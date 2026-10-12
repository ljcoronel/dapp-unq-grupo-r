# Especificación de funcionalidad: Migración del catálogo a WhoScored

**Rama de funcionalidad**: `009-whoscored-team-player-migration`

**Fecha de creación**: 2026-10-10

**Estado**: Borrador

**Entrada**: Descripción del usuario: "Como desarrollador, quiero migrar el proveedor de datos del catálogo de jugadores de FootballData a WhoScored, introduciendo el modelo de Equipo y enriqueciendo la información del jugador, eliminando el código anterior."

## Escenarios de usuario y pruebas

### Historia de usuario 1 - Obtener el catálogo desde WhoScored (Prioridad: P1)

Como desarrollador y consumidor del catálogo, quiero que la información de jugadores se obtenga desde WhoScored para disponer de datos actuales y más completos.

**Por qué esta prioridad**: La migración de la fuente es el objetivo principal y debe conservar el funcionamiento del catálogo.

**Prueba independiente**: Ejecutar la actualización del catálogo con datos simulados de WhoScored y comprobar que los jugadores publicados contienen los datos normalizados de esa fuente sin invocar FootballData.

**Escenarios de aceptación**:

1. **Dado** que WhoScored entrega jugadores válidos, **cuando** se actualiza el catálogo, **entonces** los jugadores quedan disponibles con sus identificadores, nombres, posiciones, equipos y estadísticas recibidas.
2. **Dado** que WhoScored no responde o entrega datos inválidos, **cuando** se actualiza el catálogo, **entonces** se informa el fallo y se conserva el último catálogo válido sin reemplazarlo por datos corruptos o vacíos.

---

### Historia de usuario 2 - Consultar jugadores y equipos enriquecidos (Prioridad: P1)

Como consumidor del catálogo, quiero consultar cada jugador relacionado con su equipo y sus nuevos atributos para contar con información más útil sin perder los datos existentes.

**Por qué esta prioridad**: El nuevo modelo de dominio y el enriquecimiento son el valor funcional que justifica la migración.

**Prueba independiente**: Crear un jugador a partir de un registro de WhoScored y verificar que su equipo y sus atributos extendidos se conservan en el listado y en el detalle.

**Escenarios de aceptación**:

1. **Dado** un registro de jugador con equipo, **cuando** se normaliza, **entonces** existe un Equipo asociado con su identidad y el jugador lo referencia de forma consistente.
2. **Dado** un registro con atributos adicionales de WhoScored, **cuando** se consulta el jugador, **entonces** se devuelven esos atributos junto con los campos existentes.
3. **Dado** un atributo opcional ausente, **cuando** se normaliza el jugador, **entonces** se representa como dato faltante según las reglas vigentes y no se inventa un valor.
4. **Dado** un jugador sin equipo identificable, **cuando** se procesa el registro, **entonces** se rechaza o se marca como inválido sin crear una relación ficticia.

---

### Historia de usuario 3 - Eliminar FootballData de forma segura (Prioridad: P2)

Como mantenedor, quiero eliminar todo el código y las pruebas de FootballData para completar la transición sin dejar referencias activas al proveedor anterior.

**Por qué esta prioridad**: La eliminación completa evita que el proveedor anterior vuelva a utilizarse accidentalmente.

**Prueba independiente**: Revisar el repositorio y comprobar que las clases, adaptadores, configuración y pruebas relacionadas con FootballData fueron eliminadas y que no quedan referencias activas.

**Escenarios de aceptación**:

1. **Dado** código específico de FootballData, **cuando** se completa la migración, **entonces** sus archivos y pruebas son eliminados.
2. **Dado** un arranque o actualización normal del catálogo, **cuando** se ejecuta el flujo, **entonces** no se requieren credenciales, solicitudes, fallback ni adaptadores de FootballData.
3. **Dado** que existen archivos heredados de FootballData, **cuando** se completa la migración, **entonces** se eliminan junto con sus referencias, configuración y pruebas.

### Casos límite

- WhoScored entrega registros duplicados o sin un identificador confiable.
- Un equipo cambia de nombre o no incluye alguno de sus atributos opcionales.
- El mismo equipo aparece asociado a varios jugadores y debe conservar una identidad coherente.
- WhoScored entrega estadísticas parciales, ceros válidos o un formato temporalmente inválido.
- La actualización de una competición falla mientras otras se actualizan correctamente.
- Queda una referencia heredada a FootballData en configuración, documentación o pruebas.

## Requisitos

### Requisitos funcionales

- **RF-001**: El servicio de catálogo DEBE utilizar WhoScored como única fuente externa activa de información de jugadores.
- **RF-002**: El sistema DEBE normalizar los registros de WhoScored conservando los atributos existentes del Jugador y agregando todos los atributos adicionales compatibles que la fuente entregue.
- **RF-003**: DEBE existir una entidad Equipo con identidad propia, datos descriptivos y relación con uno o más Jugadores.
- **RF-004**: Cada Jugador válido DEBE estar relacionado con su Equipo identificable y las consultas de listado y detalle DEBEN exponer esa relación.
- **RF-005**: El sistema DEBE distinguir valores numéricos válidos, incluido cero, de atributos ausentes, sin reemplazar datos faltantes por valores inventados.
- **RF-006**: El sistema DEBE conservar las consultas públicas y reglas de identificación existentes del catálogo, salvo los campos nuevos definidos para Jugador y Equipo.
- **RF-007**: Una respuesta vacía, inválida o fallida de WhoScored NO DEBE reemplazar el último catálogo válido; el fallo DEBE comunicarse mediante el mecanismo existente.
- **RF-008**: El fallo de una competición NO DEBE invalidar los datos válidos de otras competiciones actualizadas en la misma operación.
- **RF-009**: Los registros sin identificador de jugador o equipo confiable DEBEN rechazarse o excluirse sin crear entidades ficticias.
- **RF-010**: Todo código, clase, adaptador, configuración, dependencia y prueba específico de FootballData DEBE eliminarse, incluyendo `PlayerCatalogStartupRunner.java`, `PlayerCatalogRefreshService.java`, `FootballDataClient.java`, `League.java` y `dto/footballdata/`.
- **RF-011**: El flujo activo NO DEBE realizar solicitudes, extracción, fallback ni requerir configuración o credenciales de FootballData.
- **RF-012**: Las pruebas del catálogo DEBEN utilizar mocks o fixtures de WhoScored, y DEBEN existir pruebas unitarias para Equipo, la relación con Jugador y los atributos extendidos.
- **RF-013**: La funcionalidad no relacionada con el proveedor de jugadores DEBE conservar su comportamiento actual.

### Entidades principales

- **Jugador**: Persona del catálogo identificada por un identificador estable, nombre, posición, competición, Equipo, atributos existentes y estadísticas adicionales provistas por WhoScored.
- **Equipo**: Club asociado a jugadores, identificado de forma estable y con nombre y demás datos descriptivos disponibles desde WhoScored.
- **Fuente de jugadores**: Proveedor externo que entrega registros normalizables; WhoScored es la fuente activa y FootballData se elimina.
- **Catálogo válido**: Último conjunto de registros aceptados y consultables, conservado cuando una actualización falla.

## Criterios de éxito

### Resultados medibles

- **CS-001**: El 100% de las actualizaciones exitosas obtiene los jugadores desde WhoScored y no ejecuta solicitudes ni fallbacks de FootballData.
- **CS-002**: El 100% de los Jugadores válidos publicados conserva los campos existentes y expone los atributos adicionales recibidos que sean compatibles.
- **CS-003**: El 100% de los Jugadores válidos con equipo queda relacionado con una entidad Equipo identificable y consistente.
- **CS-004**: En el 100% de los fallos, respuestas vacías o respuestas inválidas de WhoScored, el último catálogo válido permanece consultable.
- **CS-005**: El 100% de las pruebas de dominio cubre Equipo, su relación con Jugador y al menos un atributo extendido con casos presentes, ausentes y cero válido.
- **CS-006**: Una revisión del repositorio no encuentra archivos, dependencias, configuración ni pruebas específicas de FootballData, ni usos activos como fuente o dependencia de ejecución.
- **CS-007**: El 100% de las pruebas existentes del catálogo actualizadas al nuevo proveedor y de las pruebas no relacionadas con él continúa pasando.
- **CS-008**: Al menos el 95% de las consultas de listado y detalle conserva su comportamiento de navegación, identificación y agrupación, incorporando los datos enriquecidos.

## Supuestos

- WhoScored proporciona identificadores estables y datos suficientes para los campos actuales del catálogo.
- Los nombres concretos de los atributos extendidos pueden variar entre respuestas; se conservan solo los atributos compatibles entregados, sin inventar valores.
- La relación entre Jugador y Equipo se mantiene durante la normalización y en las respuestas públicas sin agregar nuevas pantallas o filtros.
- La estrategia vigente de almacenamiento del último catálogo válido y los mecanismos existentes de error se reutilizan.
- La eliminación de FootballData incluye sus archivos y pruebas; cualquier archivo no relacionado que resulte innecesario podrá marcarse como deprecado.
- No se migran históricos originados exclusivamente en FootballData para presentarlos como datos nuevos de WhoScored.
