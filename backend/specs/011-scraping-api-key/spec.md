# Feature Specification: Protección del scraping con API key

**Feature Branch**: `011-scraping-api-key`

**Created**: 2026-10-12

**Status**: Draft

**Input**: User description: "Como desarrollador, quiero implementar un sistema de API key para proteger al endpoint POST /scrape. La API key a crear tendrá el permiso "scraping", y solo necesito una. Por lo tanto: una sola API key debe crearse y varios usuarios podrán usarla; el endpoint POST /scrape solo podrá ser utilizado si se brinda la API key; se necesita un script que genere e imprima la API key para poder enviarla al usuario correspondiente. Cada ejecución del script debe revocar la API key vigente, si existe, y reemplazarla por una nueva; si no existe una vigente, debe crearla. En ambos casos debe imprimir su valor."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Ejecutar scraping con una credencial autorizada (Priority: P1)

Como usuario autorizado, quiero enviar la API key con permiso de scraping para utilizar el endpoint `POST /scrape` y obtener el resultado solicitado.

**Why this priority**: Es el objetivo principal de la funcionalidad: proteger el acceso sin impedir que los usuarios autorizados compartan una única credencial operativa.

**Independent Test**: Puede probarse enviando una solicitud válida a `POST /scrape` con la API key generada y verificando que el scraping se ejecuta y devuelve su respuesta normal.

**Acceptance Scenarios**:

1. **Given** que existe la única API key con permiso `scraping`, **When** un usuario envía una solicitud válida a `POST /scrape` junto con esa API key, **Then** el sistema autoriza la solicitud y procesa el scraping.
2. **Given** que varios usuarios disponen de la misma API key válida, **When** cada uno envía una solicitud válida a `POST /scrape` con ella, **Then** todos pueden utilizar el endpoint sin que se cree una API key adicional por usuario.

---

### User Story 2 - Rechazar solicitudes no autorizadas (Priority: P1)

Como responsable del sistema, quiero que las solicitudes sin una API key válida no puedan ejecutar scraping.

**Why this priority**: La protección no aporta valor si una solicitud puede omitir la credencial o utilizar un valor inválido.

**Independent Test**: Puede probarse con solicitudes sin API key y con API keys inválidas, verificando que ambas son rechazadas y que el scraping no se ejecuta.

**Acceptance Scenarios**:

1. **Given** una solicitud válida a `POST /scrape` sin API key, **When** el sistema recibe la solicitud, **Then** la rechaza e informa que se requiere autorización.
2. **Given** una solicitud válida a `POST /scrape` con una API key inexistente, alterada o sin permiso `scraping`, **When** el sistema recibe la solicitud, **Then** la rechaza e informa que la credencial no está autorizada.
3. **Given** una solicitud no autorizada a `POST /scrape`, **When** el sistema la rechaza, **Then** no inicia ninguna operación de scraping.

---

### User Story 3 - Generar y distribuir la credencial (Priority: P2)

Como responsable del despliegue, quiero ejecutar un script que genere la única API key e imprima su valor para entregarlo a los usuarios autorizados.

**Why this priority**: Los usuarios necesitan una forma operativa y reproducible de obtener la credencial inicial sin exponerla en el código fuente.

**Independent Test**: Puede probarse ejecutando el script en un entorno configurado, verificando que muestra una API key utilizable, que esta tiene el permiso `scraping`, y ejecutándolo nuevamente para comprobar que la credencial anterior queda revocada y es reemplazada por otra.

**Acceptance Scenarios**:

1. **Given** un entorno sin una API key vigente, **When** el responsable ejecuta el script de generación, **Then** se crea una nueva API key con permiso `scraping` y se imprime su valor en la salida del script.
2. **Given** que ya existe una API key vigente, **When** el responsable ejecuta el script, **Then** la API key vigente se revoca, se crea una nueva API key con permiso `scraping` y se imprime el valor de la nueva credencial.
3. **Given** que el script reemplazó una API key vigente, **When** se intenta usar la credencial anterior en `POST /scrape`, **Then** la solicitud es rechazada y solo la nueva API key permite acceder al endpoint.

### Edge Cases

- Si se envía una API key vacía, con espacios o con formato inválido, la solicitud debe rechazarse como no autorizada y no debe ejecutarse el scraping.
- Si se intenta generar o reemplazar la credencial cuando la persistencia no está disponible, el script debe informar el error y no imprimir una credencial que no pueda utilizarse.
- La sustitución debe dejar como máximo una API key activa con permiso `scraping`; si la creación de la nueva credencial falla, la API key vigente no debe quedar revocada sin una nueva credencial utilizable.
- Si la API key existente no tiene el permiso `scraping`, `POST /scrape` debe continuar rechazando la solicitud; no se debe ampliar el permiso automáticamente durante una petición.
- La API key no debe incluirse en mensajes de error ni en registros operativos destinados a usuarios.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST mantener una única API key activa para el propósito de scraping.
- **FR-002**: La API key única MUST tener el permiso `scraping`.
- **FR-003**: El sistema MUST permitir que múltiples usuarios utilicen la misma API key autorizada para `POST /scrape`.
- **FR-004**: `POST /scrape` MUST requerir una API key válida asociada al permiso `scraping` antes de iniciar cualquier operación de scraping.
- **FR-005**: El sistema MUST rechazar las solicitudes a `POST /scrape` que no incluyan una API key, incluyan una API key inválida o utilicen una API key sin el permiso requerido.
- **FR-006**: Ante una solicitud rechazada, el sistema MUST devolver una respuesta de autorización comprensible sin revelar la API key ni ejecutar el scraping.
- **FR-007**: Debe existir un script operable por el responsable del despliegue que cree una API key con permiso `scraping` cuando no exista una credencial vigente y muestre su valor para su distribución segura.
- **FR-008**: Cada ejecución exitosa del script MUST revocar la API key vigente para scraping, si existe, y reemplazarla por una nueva API key con permiso `scraping`.
- **FR-009**: Después de una ejecución exitosa del script MUST existir exactamente una API key activa para scraping, y la credencial anterior no debe seguir autorizando `POST /scrape`.
- **FR-010**: El script MUST imprimir el valor de la nueva API key después de crearla, tanto cuando no existía una credencial vigente como cuando reemplaza una existente.
- **FR-011**: El script MUST informar de forma explícita si no puede crear o reemplazar la credencial debido a un error del entorno, y no debe presentar como exitosa una generación fallida.
- **FR-012**: El sistema MUST conservar el permiso `scraping` asociado a la API key y validarlo en cada solicitud protegida.
- **FR-013**: El valor de la API key MUST tratarse como secreto: no debe aparecer en mensajes de error ni en registros normales de solicitudes.

### Key Entities

- **API key de scraping**: Credencial compartida que identifica el acceso autorizado al scraping; posee un valor secreto, un permiso `scraping`, un estado activo y una única vigencia operativa.
- **Permiso**: Autorización que define qué operación puede realizar una API key; para esta feature solo se contempla `scraping`.
- **Solicitud de scraping**: Petición al endpoint `POST /scrape` que solo puede procesarse después de validar la API key y su permiso.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El 100% de las solicitudes a `POST /scrape` sin API key, con API key inválida o sin permiso `scraping` son rechazadas sin iniciar scraping.
- **SC-002**: El 100% de las solicitudes válidas a `POST /scrape` que presentan la única API key autorizada superan la verificación de acceso y conservan el comportamiento normal del endpoint.
- **SC-003**: Cada ejecución exitosa del script deja exactamente una API key activa con permiso `scraping`, revoca la credencial activa anterior cuando existía y genera una nueva cuyo valor es imprimido.
- **SC-004**: Un responsable puede obtener la credencial ejecutando el script y localizar su valor en la salida en menos de 1 minuto.
- **SC-005**: En las pruebas de aceptación, ningún mensaje de error o registro normal expone el valor completo de la API key.

## Assumptions

- La API key se comparte deliberadamente entre varios usuarios autorizados; no se requiere una credencial individual por usuario en esta versión.
- La API key se transportará junto con cada solicitud mediante el mecanismo estándar de credenciales de la API; el detalle exacto de transporte se definirá durante la planificación y el contrato del endpoint.
- La generación de la API key se ejecutará en un entorno con acceso a la persistencia y permisos suficientes para crear o consultar la credencial.
- La administración simultánea de varias API keys queda fuera del alcance de esta versión; la rotación mediante ejecuciones sucesivas del script sí forma parte del alcance.
- El endpoint `POST /scrape` y su validación de datos existente deben conservarse; esta feature agrega la autorización previa.
- La entrega de la API key a los usuarios se realiza fuera del sistema y de forma segura por el responsable correspondiente.
