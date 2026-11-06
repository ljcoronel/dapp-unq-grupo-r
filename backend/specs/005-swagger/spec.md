# Especificación de la funcionalidad: Documentación interactiva y contrato de API

**Rama de funcionalidad**: `005-swagger`

**Fecha de creación**: 2026-09-26

**Estado**: Borrador

**Entrada**: Descripción del usuario: "Add interactive documentation and the API contract for the backend. Developers and API clients must be able to explore, test, and understand endpoints, data models, and authentication methods directly from a web interface, while always maintaining the specification as the single source of truth."

## Escenarios de usuario y pruebas

### Historia de usuario 1 - Explorar el contrato de la API (Prioridad: P1)

Como desarrollador o responsable de integrar un cliente, quiero abrir una interfaz web con el contrato completo de la API para entender qué operaciones existen, qué datos aceptan y qué respuestas producen sin consultar documentación externa.

**Por qué esta prioridad**: El contrato visible y completo es la base para que cualquier consumidor pueda integrar el backend de forma autónoma.

**Prueba independiente**: Abrir la interfaz de documentación y comprobar que se muestran todas las operaciones públicas del backend, sus parámetros, cuerpos de solicitud, respuestas, códigos de estado y modelos de datos asociados.

**Escenarios de aceptación**:

1. **Dado** que el backend está disponible, **cuando** un usuario abre la ruta pública de documentación, **entonces** se muestra una interfaz web navegable con el contrato vigente de la API.
2. **Dado** que existe un endpoint documentado, **cuando** el usuario lo selecciona, **entonces** puede consultar su propósito, método, ruta, parámetros, cuerpo de solicitud, respuestas y errores esperados.
3. **Dado** que el contrato contiene modelos de datos, **cuando** el usuario consulta una operación o un modelo, **entonces** puede identificar los campos, tipos, obligatoriedad y significado de cada campo.
4. **Dado** que un endpoint no requiere autenticación, **cuando** el usuario consulta sus requisitos de seguridad, **entonces** la interfaz indica explícitamente que es público.

---

### Historia de usuario 2 - Probar operaciones desde la interfaz (Prioridad: P1)

Como desarrollador, quiero ejecutar solicitudes de prueba desde la interfaz de documentación para validar el comportamiento del backend y revisar respuestas reales sin construir primero un cliente separado.

**Por qué esta prioridad**: La posibilidad de probar el contrato reduce el tiempo de integración y permite detectar rápidamente diferencias entre lo documentado y lo que responde el servicio.

**Prueba independiente**: Seleccionar un endpoint, completar los datos requeridos, ejecutar una solicitud de prueba y comprobar que se muestran la solicitud enviada, el código de respuesta y el contenido recibido.

**Escenarios de aceptación**:

1. **Dado** que el usuario está consultando un endpoint, **cuando** habilita la ejecución de prueba y completa los datos válidos, **entonces** la interfaz envía la solicitud al backend y muestra el resultado real.
2. **Dado** que el endpoint requiere datos obligatorios, **cuando** el usuario intenta ejecutar la prueba sin completarlos, **entonces** la interfaz identifica los datos faltantes antes de enviar una solicitud inválida.
3. **Dado** que el backend responde con éxito o error, **cuando** termina la solicitud, **entonces** la interfaz muestra el código de estado, los encabezados relevantes y el cuerpo de respuesta conforme al contrato.
4. **Dado** que una solicitud de prueba modifica datos o produce efectos observables, **cuando** el usuario la ejecuta, **entonces** la interfaz advierte que se trata de una operación con efectos antes del envío.

---

### Historia de usuario 3 - Entender y usar la autenticación (Prioridad: P1)

Como desarrollador o cliente de la API, quiero saber qué autenticación requiere cada operación y cómo enviar sus credenciales para poder consumir endpoints protegidos de forma correcta.

**Por qué esta prioridad**: Una descripción ambigua de la seguridad impide integrar endpoints protegidos y puede provocar el envío inseguro o incorrecto de credenciales.

**Prueba independiente**: Consultar un endpoint protegido y otro público, verificar la diferencia de requisitos de seguridad y ejecutar una prueba autenticada con credenciales de prueba válidas.

**Escenarios de aceptación**:

1. **Dado** que un endpoint requiere autenticación, **cuando** el usuario consulta su documentación, **entonces** la interfaz identifica el mecanismo requerido, la ubicación de la credencial y el formato esperado.
2. **Dado** que el usuario configura una credencial en la interfaz, **cuando** ejecuta una solicitud autorizada, **entonces** la credencial se incorpora únicamente según el mecanismo documentado y el backend procesa la solicitud.
3. **Dado** que el usuario ejecuta un endpoint protegido sin credencial o con una credencial inválida, **entonces** la interfaz muestra la respuesta de autorización fallida documentada.
4. **Dado** que el contrato se actualiza, **cuando** se consulta la documentación, **entonces** los requisitos de autenticación visibles coinciden con los que aplica el backend.

---

### Historia de usuario 4 - Mantener el contrato como fuente única (Prioridad: P1)

Como responsable del backend, quiero que la documentación interactiva y el contrato publicado provengan de una única especificación versionada para evitar que la interfaz, los clientes y el comportamiento del backend diverjan.

**Por qué esta prioridad**: La confianza en la documentación depende de que no existan definiciones duplicadas o actualizadas de forma independiente.

**Prueba independiente**: Modificar una operación o modelo en la especificación, regenerar o actualizar la documentación y comprobar que el cambio aparece en la interfaz sin mantener una definición paralela incompatible.

**Escenarios de aceptación**:

1. **Dado** que se incorpora o modifica un endpoint, **cuando** se actualiza la especificación del contrato, **entonces** la interfaz refleja la ruta, operación, parámetros, respuestas y seguridad definidos allí.
2. **Dado** que existe una operación expuesta por el backend, **cuando** se revisa el contrato, **entonces** la operación tiene una única definición canónica y no depende de documentación manual duplicada.
3. **Dado** que una implementación contradice el contrato, **cuando** se ejecutan las comprobaciones de calidad del proyecto, **entonces** la discrepancia se identifica antes de publicar el cambio.
4. **Dado** que el contrato cambia, **cuando** se revisa la versión del proyecto, **entonces** el cambio queda trazable junto con la documentación que afecta.

### Casos límite

- Si el backend no puede cargar la especificación o la interfaz, se muestra un error comprensible y no una documentación vacía o parcialmente válida.
- Si un endpoint no tiene parámetros, la interfaz lo indica y permite ejecutar la solicitud sin exigir datos inexistentes.
- Si una respuesta puede ser vacía, la documentación describe explícitamente esa posibilidad y su código de estado.
- Si una operación requiere autenticación y el usuario no tiene credenciales, se puede explorar su contrato sin revelar ni inventar credenciales.
- Las credenciales introducidas para una prueba no se incorporan al contrato versionado ni se muestran como ejemplos persistentes.
- Los errores de validación, autenticación, autorización, recurso inexistente y error interno se documentan con sus códigos y estructura de respuesta cuando aplican.
- Si dos endpoints comparten un modelo, ambos referencian la misma definición conceptual para evitar esquemas incompatibles.
- Los cambios incompatibles del contrato no se publican como una actualización silenciosa de una versión existente.
- La interfaz debe seguir siendo usable en pantallas de escritorio y permitir navegación con teclado y tecnologías de asistencia.

## Requisitos

### Requisitos funcionales

- **RF-001**: El sistema DEBE publicar una interfaz web accesible desde una ruta conocida del backend para explorar el contrato de la API.
- **RF-002**: La interfaz DEBE mostrar todas las operaciones HTTP públicas expuestas por el backend y no DEBE omitir una operación existente del contrato vigente.
- **RF-003**: Para cada operación, el contrato DEBE describir como mínimo el método, la ruta, el propósito, los parámetros, el cuerpo de solicitud cuando corresponda, las respuestas exitosas y los errores esperados.
- **RF-004**: El contrato DEBE describir los modelos de datos utilizados por las solicitudes y respuestas, incluyendo nombre, tipo, obligatoriedad, relaciones y significado de cada campo relevante.
- **RF-005**: La interfaz DEBE permitir que un usuario explore las operaciones y sus modelos sin autenticarse cuando el acceso a la documentación sea público.
- **RF-006**: La interfaz DEBE permitir ejecutar solicitudes de prueba contra el backend para las operaciones habilitadas, mostrando la solicitud enviada, el código de estado, los encabezados relevantes y el cuerpo de respuesta.
- **RF-007**: Antes de ejecutar una solicitud de prueba, la interfaz DEBE validar la presencia y el formato de los datos obligatorios que el contrato declare.
- **RF-008**: La interfaz DEBE indicar de forma visible cuando una operación de prueba puede modificar datos o producir efectos observables, antes de enviar la solicitud.
- **RF-009**: El contrato DEBE declarar para cada operación si es pública o qué mecanismo de autenticación requiere.
- **RF-010**: Para cada mecanismo protegido, el contrato DEBE explicar la ubicación de la credencial, el formato esperado y las respuestas producidas por credenciales ausentes, inválidas o insuficientes.
- **RF-011**: La interfaz DEBE permitir configurar credenciales de prueba de forma temporal para ejecutar operaciones protegidas y NO DEBE persistirlas en la especificación ni incluirlas en el control de versiones.
- **RF-012**: La especificación del contrato DEBE ser la única fuente canónica de rutas, operaciones, modelos, respuestas y requisitos de seguridad que muestra la interfaz.
- **RF-013**: El sistema NO DEBE requerir definiciones manuales duplicadas que puedan divergir de la especificación canónica para publicar la interfaz.
- **RF-014**: Toda operación expuesta por el backend DEBE tener una definición correspondiente en la especificación antes de considerarse documentada.
- **RF-015**: Los cambios del backend que alteren rutas, datos, respuestas o seguridad DEBEN actualizar la especificación del contrato en el mismo cambio versionado.
- **RF-016**: Las comprobaciones de calidad DEBEN detectar al menos rutas implementadas sin contrato, operaciones del contrato no disponibles y diferencias en los modelos o requisitos de seguridad.
- **RF-017**: La interfaz DEBE representar de manera diferenciada respuestas exitosas y errores, incluyendo como mínimo validación, autenticación, autorización, recurso inexistente y error interno cuando cada caso sea aplicable.
- **RF-018**: La interfaz y las descripciones del contrato DEBEN estar redactadas en español, conservando en inglés los términos técnicos establecidos por el proyecto.
- **RF-019**: La interfaz DEBE permitir navegación con teclado, exponer nombres y estados comprensibles a tecnologías de asistencia y mantener una lectura clara de las operaciones y modelos.
- **RF-020**: La especificación DEBE declarar una versión del contrato y los cambios incompatibles DEBEN identificarse de forma explícita antes de publicar una nueva versión.
- **RF-021**: Si la especificación no puede cargarse o es inválida, el sistema DEBE mostrar un error comprensible y NO DEBE presentar una interfaz vacía como si fuera el contrato vigente.

### Entidades principales

- **Contrato de API**: Definición versionada y canónica de las operaciones, rutas, parámetros, cuerpos, respuestas, modelos y requisitos de seguridad del backend.
- **Operación de API**: Acción identificada por un método y una ruta, con su propósito, entradas, salidas, errores y mecanismo de autenticación.
- **Modelo de datos**: Estructura reutilizable que describe los datos intercambiados por una o más operaciones, incluidos sus campos, tipos y reglas de obligatoriedad.
- **Esquema de seguridad**: Descripción del mecanismo mediante el cual una operación identifica y valida las credenciales de un consumidor.
- **Solicitud de prueba**: Ejecución iniciada desde la interfaz con datos proporcionados por el usuario para observar el comportamiento real de una operación.
- **Versión del contrato**: Identificador que permite distinguir la definición vigente y rastrear cambios compatibles o incompatibles.

## Criterios de éxito

### Resultados medibles

- **CS-001**: El 100% de las operaciones HTTP públicas existentes en el backend aparece en la interfaz con método y ruta correctos.
- **CS-002**: El 100% de las operaciones documentadas incluye sus parámetros, entradas, respuestas exitosas y errores aplicables, verificado mediante una revisión automatizada del contrato.
- **CS-003**: Al menos el 90% de los desarrolladores que prueben la interfaz puede localizar un endpoint, identificar sus datos requeridos y ejecutar una solicitud válida sin consultar documentación externa.
- **CS-004**: El 100% de las solicitudes de prueba muestra al usuario el código de estado y el cuerpo de respuesta recibido, tanto en respuestas exitosas como fallidas.
- **CS-005**: El 100% de las operaciones protegidas declara su mecanismo de autenticación y el 100% de las operaciones públicas se identifica como no protegida.
- **CS-006**: El 100% de los cambios de rutas, modelos, respuestas o seguridad incluidos en una entrega actualiza el contrato en el mismo cambio versionado.
- **CS-007**: Las comprobaciones de calidad detectan el 100% de las rutas implementadas que no tienen definición en el contrato y el 100% de las definiciones del contrato que no tienen operación disponible.
- **CS-008**: Un usuario que navega únicamente con teclado puede abrir la interfaz, seleccionar una operación, completar sus datos y consultar el resultado de una prueba.
- **CS-009**: Ninguna credencial introducida durante una prueba aparece en el archivo de especificación, en los ejemplos versionados ni en la documentación publicada.
- **CS-010**: Ante una especificación inválida o no disponible, el 100% de las cargas de la interfaz muestra un mensaje de error comprensible en lugar de una documentación incompleta.

## Supuestos

- La interfaz se publica junto con el backend existente y su acceso de exploración es público, salvo que una decisión posterior del proyecto establezca lo contrario.
- El contrato seguirá un formato estándar de descripción de APIs compatible con la documentación interactiva solicitada; los detalles de implementación se decidirán durante la planificación.
- Las solicitudes de prueba se ejecutan contra el entorno seleccionado por el usuario y no constituyen un entorno aislado de producción.
- Las credenciales para pruebas serán proporcionadas por el usuario o por un entorno configurado de forma segura; esta funcionalidad no crea cuentas ni gestiona secretos.
- La primera versión documentará los endpoints existentes del backend, incluidos los endpoints públicos de jugadores y los endpoints protegidos que ya formen parte del servicio.
- Los endpoints que no puedan ejecutarse de forma segura desde la interfaz podrán marcarse como no ejecutables, pero seguirán siendo explorables y deberán describir por qué no se habilita su prueba.
- La especificación, sus ejemplos no sensibles y las comprobaciones de consistencia forman parte del control de versiones y del Definition of Done del proyecto.
- La documentación no sustituye las reglas de negocio ni modifica el comportamiento del backend; describe y permite verificar el contrato que el backend debe cumplir.
