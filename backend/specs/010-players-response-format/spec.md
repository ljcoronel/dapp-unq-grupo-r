# Especificación de funcionalidad: Modificación de la respuesta de jugadores

**Rama de funcionalidad**: `010-players-response-format`

**Fecha de creación**: 2026-10-10

**Estado**: Borrador

**Entrada**: Descripción del usuario: "Como desarrollador, quiero modificar la forma en la que se devuelven jugadores a través del endpoint GET /players"

## Escenarios de usuario y pruebas

### Historia de usuario 1 - Consultar jugadores con el nuevo contrato (Prioridad: P1)

Como consumidor de la API, quiero obtener los jugadores mediante una respuesta con la nueva forma acordada para poder procesarlos sin transformar manualmente una estructura inesperada.

**Por qué esta prioridad**: Cambiar el contrato de listado es el objetivo principal de la funcionalidad y afecta a todos los consumidores de `GET /players`.

**Prueba independiente**: Ejecutar una solicitud exitosa a `GET /players` con datos de jugadores disponibles y comprobar que el cuerpo cumple exactamente la estructura definida para el nuevo contrato.

**Escenarios de aceptación**:

1. **Dado** que existen jugadores almacenados, **cuando** un consumidor consulta `GET /players`, **entonces** recibe una respuesta HTTP 200 con la estructura de listado definida para la nueva versión.
2. **Dado** que un jugador contiene datos opcionales ausentes, **cuando** se devuelve el listado, **entonces** la respuesta conserva esos campos con valor ausente sin desplazar ni mezclar los campos de otros jugadores.
3. **Dado** que existe más de una liga con jugadores, **cuando** se consulta el listado, **entonces** cada jugador queda asociado a la liga correcta según el contrato.

---

### Historia de usuario 2 - Mantener resultados consistentes (Prioridad: P1)

Como consumidor de la API, quiero que todos los elementos de la respuesta sigan reglas uniformes para poder recorrerlos, identificarlos y mostrar sus datos de forma predecible.

**Por qué esta prioridad**: La consistencia evita que el cambio de formato rompa clientes que procesan varios jugadores o ligas.

**Prueba independiente**: Consultar el endpoint con conjuntos de datos completos, parciales y vacíos, y validar que la forma de la respuesta se mantiene estable en cada caso.

**Escenarios de aceptación**:

1. **Dado** un conjunto de jugadores, **cuando** se devuelve la respuesta, **entonces** todos los jugadores usan los mismos nombres, tipos y niveles de anidamiento definidos.
2. **Dado** que no hay jugadores disponibles, **cuando** se consulta `GET /players`, **entonces** la respuesta representa explícitamente el resultado vacío según el nuevo contrato, sin crear jugadores ficticios.
3. **Dado** que el catálogo no está disponible, **cuando** se consulta el endpoint, **entonces** se conserva el código de error y el formato de error acordados por la API.

---

### Historia de usuario 3 - Consultar detalles sin afectar el listado (Prioridad: P2)

Como consumidor de la API, quiero seguir consultando un jugador individual para distinguir el cambio del listado de cualquier cambio en la consulta por identificador.

**Por qué esta prioridad**: `GET /players/{id}` es una operación relacionada, pero no debe cambiar accidentalmente por una modificación exclusiva de `GET /players`.

**Prueba independiente**: Consultar un jugador existente y uno inexistente por identificador después de modificar el listado y verificar que ambos resultados mantienen su comportamiento vigente.

**Escenarios de aceptación**:

1. **Dado** un identificador existente, **cuando** se consulta `GET /players/{id}`, **entonces** se devuelve el jugador correspondiente sin aplicar la envoltura o agrupación exclusiva del listado.
2. **Dado** un identificador inexistente, **cuando** se consulta `GET /players/{id}`, **entonces** se responde con el error de recurso no encontrado vigente.

### Casos límite

- La respuesta no debe incluir jugadores duplicados por efecto de la transformación del formato.
- Un jugador con valores nulos, vacíos o cero debe conservar la distinción entre dato ausente y valor cero según las reglas actuales del catálogo.
- Una liga sin jugadores debe respetar la representación vacía elegida para el nuevo contrato.
- El orden actual de jugadores y ligas solo debe conservarse si forma parte del contrato seleccionado; de lo contrario, debe definirse un orden determinista.
- Un cliente que continúe enviando solicitudes válidas no debe recibir un error de autenticación por este cambio.
- Los consumidores que dependan del contrato anterior requieren una estrategia explícita de compatibilidad o una migración coordinada.

## Requisitos

### Requisitos funcionales

- **RF-001**: El endpoint público `GET /players` DEBE devolver los jugadores usando una única estructura de respuesta consistente con el nuevo contrato.
- **RF-002**: `GET /players` DEBE devolver una lista plana de jugadores, sin agruparlos en listas internas por liga.
- **RF-003**: Cada elemento DEBE devolver todos los campos públicos actuales del jugador, excepto su identificador interno: `externalId`, `name`, `team`, `league`, `age`, `heightCM`, `weightKG`, `position`, `appearances`, `minutesPlayed`, `goals`, `assists`, `yellowCards`, `redCards`, `avgShotsPerGame`, `passSuccessPercentage`, `aerialsWon` y `timesManOfTheMatch`.
- **RF-004**: Los nombres, tipos, nulabilidad y nivel de anidamiento de los campos definidos para cada jugador DEBEN ser iguales para todos los elementos de una misma respuesta; `team` y `league` DEBEN conservar su relación y representación pública vigente.
- **RF-005**: La respuesta DEBE representar un catálogo vacío sin inventar registros y de acuerdo con la estructura seleccionada.
- **RF-006**: La respuesta DEBE conservar los valores numéricos válidos, incluido cero, y distinguirlos de los datos no disponibles.
- **RF-007**: El endpoint DEBE mantener el acceso público vigente y no DEBE requerir autenticación adicional para consultar el listado.
- **RF-008**: Los errores de catálogo no disponible DEBEN conservar un código HTTP y un cuerpo de error coherentes con el contrato vigente.
- **RF-009**: `GET /players/{id}` DEBE conservar su comportamiento actual, salvo que se defina expresamente lo contrario como parte de este cambio.
- **RF-010**: La nueva respuesta DEBE reemplazar de forma incompatible el contrato de listado anterior; los consumidores y la documentación DEBEN actualizarse al formato plano y a los campos definidos en esta especificación.
- **RF-011**: La documentación pública del endpoint DEBE reflejar la estructura final, los campos, los casos vacíos y los errores de la nueva respuesta.
- **RF-012**: Las pruebas del endpoint DEBEN cubrir al menos una respuesta con jugadores, una respuesta vacía, datos opcionales ausentes, valores cero y errores de disponibilidad.

### Entidades principales

- **Respuesta de listado de jugadores**: Lista plana de objetos de jugador devueltos por `GET /players`.
- **Jugador**: Registro del catálogo identificado de forma única y asociado a una liga, con los campos públicos actuales excepto su identificador interno.
- **Liga**: Competencia a la que pertenece cada jugador cuando la nueva respuesta conserva o expone esa relación.
- **Error de catálogo**: Resultado que informa que el listado no pudo ser entregado, conservando el código y la estructura acordados.

## Criterios de éxito

### Resultados medibles

- **CS-001**: El 100% de las respuestas exitosas de `GET /players` es una lista plana y cumple la estructura final documentada, sin listas internas por liga.
- **CS-002**: El 100% de los jugadores devueltos contiene exactamente los campos públicos definidos, excluyendo el identificador interno, y conserva su asociación correcta con la liga.
- **CS-003**: El 100% de las respuestas vacías usa la representación vacía definida, sin registros ficticios ni estructuras inconsistentes.
- **CS-004**: El 100% de los casos de datos opcionales ausentes distingue valores faltantes de valores numéricos iguales a cero según las reglas documentadas.
- **CS-005**: El 100% de las consultas válidas a `GET /players/{id}` mantiene su comportamiento vigente después del cambio del listado.
- **CS-006**: Una revisión de la documentación y de las pruebas confirma que el contrato publicado coincide con las respuestas observables en todos los casos definidos.
- **CS-007**: Al menos el 95% de los consumidores representativos puede procesar la nueva respuesta siguiendo únicamente la documentación actualizada, sin depender de campos no documentados.

## Supuestos

- La modificación se limita al contrato de respuesta de `GET /players`; la carga, persistencia y actualización del catálogo no cambian salvo que sean necesarias para producir la nueva forma.
- Los nombres de dominio existentes se mantienen en español y las reglas actuales de identificación de jugadores continúan vigentes.
- El endpoint sigue siendo público y de solo lectura.
- La respuesta de `GET /players/{id}` no cambia por defecto, porque la solicitud menciona exclusivamente `GET /players`.
- El consumidor de la API dispone de documentación actualizada antes de adoptar el nuevo contrato.
- Los códigos de error existentes se conservan cuando no contradicen la decisión de compatibilidad seleccionada.
- La sustitución del contrato es intencionalmente incompatible: no se mantiene una respuesta alternativa ni un período de compatibilidad temporal.
