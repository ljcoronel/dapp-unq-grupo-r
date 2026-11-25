# Especificación de funcionalidad: Protección del recálculo con API Key

**Feature Branch**: `006-api-key-recalculate` (no creada)

**Created**: 2026-11-24

**Status**: Draft

**Input**: Proteger `POST /quotes/recalculate` con una API Key administrativa independiente de la autenticación JWT de usuarios.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Autorizar el recálculo administrativo (Priority: P1)

Como operador administrativo o tarea programada autorizada, quiero invocar el recálculo masivo con una credencial propia del sistema, para actualizar las cotizaciones sin depender de una sesión de inversor.

**Why this priority**: El recálculo cambia valuaciones del mercado y debe seguir disponible para clientes de sistema autorizados, pero no para solicitudes públicas o usuarios autenticados sin permiso administrativo.

**Independent Test**: Invocar el endpoint con una API Key configurada y válida, verificando el estado 200 y que el recálculo sigue la estrategia activa.

**Acceptance Scenarios**:

1. **Given** una API Key configurada y válida, **When** un cliente invoca `POST /quotes/recalculate`, **Then** se autoriza el intento, se ejecuta el recálculo según la estrategia activa y se responde 200 OK.
2. **Given** una API Key válida y ningún JWT, **When** el cliente invoca el endpoint, **Then** la solicitud puede completar el recálculo sin requerir autenticación de usuario.
3. **Given** una API Key válida junto con un encabezado JWT, **When** el cliente invoca el endpoint, **Then** la autorización del recálculo depende de la API Key y no de la identidad JWT.

---

### User Story 2 - Rechazar solicitudes no autorizadas o sin configuración (Priority: P1)

Como responsable del mercado, quiero que las solicitudes sin una credencial administrativa válida se detengan antes de cualquier operación de negocio, para impedir recálculos no autorizados o ejecutados en un entorno mal configurado.

**Why this priority**: Evitar una modificación no autorizada de cotizaciones y fallar de forma cerrada son los objetivos principales de seguridad de esta funcionalidad.

**Independent Test**: Probar el endpoint con la credencial ausente, vacía, incorrecta y con la configuración del servidor ausente; verificar respuestas y ausencia de cambios en cotizaciones.

**Acceptance Scenarios**:

1. **Given** una API Key configurada, **When** la solicitud no incluye `X-API-Key` o lo incluye vacío, **Then** el sistema responde 401 Unauthorized y no inicia consultas ni el recálculo.
2. **Given** una API Key configurada, **When** la solicitud incluye una clave incorrecta, **Then** el sistema responde 403 Forbidden, no inicia consultas ni el recálculo y registra el intento sin la clave recibida.
3. **Given** que el servidor no tiene una API Key configurada, **When** se invoca el endpoint, **Then** el sistema responde 500 Internal Server Error y no inicia el recálculo.
4. **Given** un JWT válido pero sin `X-API-Key`, **When** el usuario invoca el endpoint, **Then** el sistema responde 401 Unauthorized y no ejecuta el recálculo.
5. **Given** una clave configurada, **When** el valor recibido difiere de ella, aunque sea en un solo carácter, **Then** el sistema responde 403 Forbidden y no ejecuta el recálculo.
6. **Given** cualquier intento de invocación, **When** el sistema registra su resultado, **Then** el registro incluye marca de tiempo, ruta, resultado de autenticación y Correlation ID, y no contiene la API Key.

---

### User Story 3 - Comprender el acceso y los resultados de autenticación (Priority: P2)

Como consumidor administrativo de la API, quiero que la documentación identifique claramente la credencial requerida y los rechazos posibles, para enviar solicitudes válidas y diagnosticar errores sin confundir la API Key con el JWT de usuario.

**Why this priority**: Una documentación exacta reduce errores de integración y evita que los clientes interpreten un JWT de usuario como autorización para una operación administrativa.

**Independent Test**: Revisar la documentación OpenAPI de `POST /quotes/recalculate` y comprobar el esquema `ApiKeyAuth`, el encabezado requerido y las respuestas de autenticación.

**Acceptance Scenarios**:

1. **Given** la especificación OpenAPI publicada, **When** un consumidor revisa `POST /quotes/recalculate`, **Then** identifica `ApiKeyAuth` como esquema de seguridad basado en `X-API-Key`.
2. **Given** la documentación de ese endpoint, **When** un consumidor revisa sus respuestas, **Then** encuentra documentados 401 Unauthorized y 403 Forbidden.
3. **Given** un consumidor que solo dispone de un JWT de usuario, **When** consulta la documentación, **Then** puede identificar que el JWT no sustituye la API Key administrativa.
4. **Given** la documentación de ese endpoint, **When** un consumidor revisa el caso de configuración ausente, **Then** encuentra documentada la respuesta 500 Internal Server Error.

### Edge Cases

- Si el encabezado está ausente o su valor tiene longitud cero, el resultado es 401 cuando el servidor sí tiene una clave configurada.
- Un valor no vacío que solo difiere de la clave configurada por mayúsculas, minúsculas o espacios no se normaliza ni autoriza; se rechaza como clave incorrecta con 403.
- Una configuración ausente, vacía o compuesta solo por espacios se trata como API Key no configurada y produce 500, con independencia de las credenciales de la solicitud.
- La comprobación de configuración y credencial precede a toda consulta a la base de datos, validación de negocio o inicio del recálculo.
- Los intentos exitosos, rechazados y bloqueados por configuración deben ser trazables aunque la solicitud no proporcione un Correlation ID.
- Si una solicitud contiene valores ambiguos o malformados para `X-API-Key`, no se considera autorizada.
- Los logs y las respuestas de error no deben revelar la clave configurada ni la recibida.
- Una solicitud autorizada mantiene el comportamiento funcional existente de la estrategia activa de recálculo.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST exigir un valor no nulo y no vacío en `X-API-Key` para autorizar `POST /quotes/recalculate`.
- **FR-002**: Con una clave configurada, el sistema MUST responder 401 Unauthorized cuando el encabezado `X-API-Key` esté ausente o vacío, y MUST evitar cualquier consulta de negocio o modificación de cotizaciones.
- **FR-003**: El sistema MUST comparar la clave recibida con la configurada de forma exacta y en tiempo constante; no MUST normalizar valores para convertir una clave distinta en válida.
- **FR-004**: Con una clave configurada, el sistema MUST responder 403 Forbidden ante una clave no coincidente y MUST detener la solicitud antes de iniciar el recálculo.
- **FR-005**: El sistema MUST verificar la configuración de la credencial antes de procesar la solicitud. Si la clave del servidor no está configurada o está vacía, MUST responder 500 Internal Server Error y MUST impedir el recálculo.
- **FR-006**: El sistema MUST resolver la autorización administrativa exclusivamente con `X-API-Key`; un JWT de usuario no es requisito ni reemplazo de esa credencial.
- **FR-007**: El sistema MUST evaluar la configuración y autenticación antes de cualquier consulta a base de datos, lógica de negocio o inicio del recálculo.
- **FR-008**: Si la solicitud está autorizada, el sistema MUST ejecutar el recálculo según la estrategia activa y conservar su respuesta exitosa 200 OK.
- **FR-009**: El sistema MUST registrar estructuradamente cada intento, autorizado o rechazado, con marca de tiempo, ruta solicitada, resultado de autenticación y Correlation ID.
- **FR-010**: Cada intento MUST tener un Correlation ID trazable; si la solicitud no aporta uno, se utilizará uno disponible para esa solicitud o se generará uno.
- **FR-011**: Los registros, respuestas y documentación MUST omitir por completo los valores recibidos y configurados de la API Key.
- **FR-012**: La documentación OpenAPI MUST declarar `ApiKeyAuth` para `POST /quotes/recalculate`, como API Key en el encabezado `X-API-Key`, y MUST documentar las respuestas 401 Unauthorized y 403 Forbidden.
- **FR-013**: La documentación OpenAPI MUST documentar también 500 Internal Server Error para el caso de configuración ausente.
- **FR-014**: Solicitudes con valores ambiguos o malformados de `X-API-Key` MUST ser rechazadas sin iniciar el recálculo.

### Key Entities

- **Cliente administrativo autorizado**: Operador o tarea programada que puede solicitar el recálculo y cuya autorización se demuestra mediante la API Key.
- **Credencial administrativa**: Secreto de servidor contra el que se valida la solicitud; el valor no se expone en registros ni documentación.
- **Intento de recálculo**: Invocación que conserva ruta, resultado de autenticación, marca de tiempo y Correlation ID, pero no la credencial presentada.
- **Cotización**: Valuación del jugador afectada únicamente cuando el intento fue autorizado y el recálculo se ejecuta.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El 100% de las solicitudes con API Key válida y configuración presente alcanza la ejecución de la estrategia activa y recibe 200 OK.
- **SC-002**: El 100% de las solicitudes sin encabezado o con valor vacío, con configuración presente, recibe 401 y deja intactas las cotizaciones.
- **SC-003**: El 100% de las solicitudes con clave no coincidente recibe 403 y no inicia consultas de negocio ni recálculos.
- **SC-004**: El 100% de las solicitudes en un entorno sin clave configurada recibe 500 y no inicia el recálculo.
- **SC-005**: El 100% de los intentos genera un registro trazable con marca de tiempo, ruta, resultado de autenticación y Correlation ID; ningún registro contiene la clave enviada o configurada.
- **SC-006**: En una revisión guiada, el 100% de los consumidores administrativos puede localizar el encabezado requerido, distinguir la API Key del JWT de usuario e identificar los resultados 401 y 403 sin ayuda adicional.

## Assumptions

- La estrategia de recálculo y su resultado exitoso actual se mantienen sin cambios; esta funcionalidad solo restringe quién puede iniciarla.
- “Clave configurada” significa que existe un valor no vacío compuesto por caracteres distintos de espacios. Los valores recibidos se comparan exactamente y no se normalizan.
- Los resultados HTTP requeridos son 401 para credencial ausente o vacía, 403 para clave incorrecta y 500 para configuración del servidor ausente, como se define en los escenarios solicitados.
- El Correlation ID sigue el mecanismo de trazabilidad existente si está disponible; si no se recibe ni existe uno para la solicitud, se genera uno para registrar el intento.
- La auditoría requiere registrar metadatos de autenticación y no requiere persistir la API Key, el JWT ni el contenido secreto de la solicitud.
- El alcance se limita a `POST /quotes/recalculate` y su documentación OpenAPI; no cambia la autenticación JWT ni otros endpoints.
