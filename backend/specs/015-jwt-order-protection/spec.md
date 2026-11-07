# Feature Specification: Protección JWT de órdenes

**Feature Branch**: `015-jwt-order-protection`

**Created**: 2026-11-04

**Status**: Draft

**Input**: User description: "Como desarrollador, quiero proteger endpoints. Los endpoints /orders/buy y /orders/sell deben ser protegidos con JWT. Debe actualizarse el archivo backend/app/src/main/resources/static/openapi.yaml para reflejar la nueva información de los dos endpoints."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Proteger la creación de órdenes (Priority: P1)

Como desarrollador, quiero que la creación de órdenes de compra y venta solo pueda realizarse con una identidad autenticada, para impedir que clientes no identificados operen en nombre de un usuario.

**Why this priority**: Los endpoints crean operaciones y modifican saldos o tenencias; protegerlos evita operaciones no autorizadas.

**Independent Test**: Invocar cada endpoint con un JWT válido, sin credenciales, con un token inválido y con un token vencido, verificando que solo la solicitud autenticada pueda continuar al comportamiento funcional existente.

**Acceptance Scenarios**:

1. **Given** un JWT válido y vigente asociado a un usuario, **When** el usuario invoca `POST /orders/buy` con una solicitud válida, **Then** la solicitud supera la autenticación y se procesa según las reglas existentes de órdenes de compra.
2. **Given** un JWT válido y vigente asociado a un usuario, **When** el usuario invoca `POST /orders/sell` con una solicitud válida, **Then** la solicitud supera la autenticación y se procesa según las reglas existentes de órdenes de venta.
3. **Given** una solicitud a cualquiera de los dos endpoints sin credenciales, **When** se recibe la solicitud, **Then** el sistema la rechaza con estado HTTP 401 y no crea ni modifica ninguna orden.
4. **Given** una solicitud con un JWT inválido o vencido, **When** se recibe la solicitud, **Then** el sistema la rechaza con estado HTTP 401 y no crea ni modifica ninguna orden.

---

### User Story 2 - Documentar el requisito de autenticación (Priority: P2)

Como desarrollador consumidor de la API, quiero que la documentación de los endpoints indique que requieren JWT, para poder construir solicitudes válidas y distinguir los errores de autenticación de los errores funcionales.

**Why this priority**: La documentación es el contrato visible de la API y debe mantenerse alineada con el comportamiento protegido.

**Independent Test**: Consultar la definición OpenAPI y verificar que ambos endpoints declaran el esquema de seguridad JWT y una respuesta 401 para solicitudes no autenticadas.

**Acceptance Scenarios**:

1. **Given** la especificación OpenAPI publicada, **When** se consulta `POST /orders/buy`, **Then** el endpoint declara que requiere el esquema de seguridad JWT y documenta la respuesta HTTP 401.
2. **Given** la especificación OpenAPI publicada, **When** se consulta `POST /orders/sell`, **Then** el endpoint declara que requiere el esquema de seguridad JWT y documenta la respuesta HTTP 401.
3. **Given** la definición del esquema de seguridad, **When** un consumidor revisa la documentación, **Then** puede identificar el uso de un token portador JWT en el encabezado de autorización.

### Edge Cases

- El encabezado de autorización ausente, vacío o con un formato distinto de token portador debe rechazarse como solicitud no autenticada.
- Un JWT correctamente firmado pero vencido debe rechazarse y no debe alcanzar la lógica de creación de órdenes.
- Un JWT válido cuyo usuario ya no existe o no puede identificarse debe rechazarse sin crear una orden.
- Un JWT válido no debe alterar las validaciones funcionales existentes: una solicitud autenticada pero inválida debe recibir el error funcional correspondiente, no un 401.
- Los errores de autenticación no deben revelar si el usuario, el saldo, la tenencia o los datos de la orden existen.
- La protección se limita a `/orders/buy` y `/orders/sell`; los demás endpoints conservan su comportamiento actual salvo que una regla existente disponga lo contrario.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST exigir autenticación mediante un JWT válido y vigente para `POST /orders/buy`.
- **FR-002**: El sistema MUST exigir autenticación mediante un JWT válido y vigente para `POST /orders/sell`.
- **FR-003**: El sistema MUST obtener la identidad del usuario autenticado a partir del JWT validado antes de ejecutar la lógica de creación de órdenes.
- **FR-004**: El sistema MUST rechazar con HTTP 401 las solicitudes a cualquiera de los dos endpoints que no incluyan credenciales válidas.
- **FR-005**: El sistema MUST impedir que una solicitud rechazada por autenticación cree, cancele o modifique órdenes, saldos o tenencias.
- **FR-006**: El sistema MUST rechazar tokens ausentes, malformados, inválidos, vencidos o asociados a una identidad no disponible con el mismo resultado de autenticación no válida.
- **FR-007**: Una solicitud autenticada correctamente MUST conservar las validaciones, respuestas y reglas de negocio existentes de la compra o venta correspondiente.
- **FR-008**: La documentación OpenAPI MUST declarar el requisito de autenticación JWT para `POST /orders/buy` y `POST /orders/sell`.
- **FR-009**: La documentación OpenAPI MUST definir el esquema de seguridad como autenticación mediante token portador JWT en el encabezado de autorización.
- **FR-010**: La documentación OpenAPI MUST documentar la respuesta HTTP 401 para ambos endpoints cuando la autenticación sea ausente o inválida.

### Key Entities

- **JWT**: Credencial firmada y temporal que prueba la identidad del usuario que solicita crear una orden.
- **Usuario autenticado**: Identidad extraída y validada desde el JWT, utilizada para autorizar la operación de compra o venta.
- **Orden de compra**: Operación creada por `POST /orders/buy`, cuyo propietario debe corresponder al usuario autenticado.
- **Orden de venta**: Operación creada por `POST /orders/sell`, cuyo propietario debe corresponder al usuario autenticado.
- **Contrato OpenAPI**: Descripción pública de los endpoints, sus requisitos de seguridad y sus respuestas de autenticación.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El 100% de las solicitudes sin JWT, con JWT inválido o con JWT vencido a `/orders/buy` y `/orders/sell` recibe HTTP 401.
- **SC-002**: El 100% de las solicitudes rechazadas por autenticación deja sin cambios las órdenes, saldos y tenencias.
- **SC-003**: El 100% de las solicitudes con JWT válido llega a las validaciones funcionales existentes de su endpoint y conserva sus resultados esperados.
- **SC-004**: La documentación OpenAPI de ambos endpoints declara el esquema JWT y la respuesta 401 en el 100% de las verificaciones automatizadas del contrato.
- **SC-005**: Ninguna solicitud autenticada puede crear una orden atribuida a un usuario distinto del identificado por su JWT.
- **SC-006**: En pruebas de uso normal, al menos el 95% de los consumidores puede identificar en la documentación cómo enviar el JWT y cómo interpretar una respuesta 401.

## Assumptions

- El sistema ya dispone de un mecanismo de validación de JWT y de una forma establecida de identificar al usuario autenticado; esta funcionalidad se reutiliza y no se redefine aquí.
- El JWT se transporta como token portador en el encabezado `Authorization`, que es el formato estándar para el contrato de la API.
- La autenticación se verifica antes de cualquier validación o efecto de negocio de los endpoints protegidos.
- La autorización específica sobre saldos, tenencias, órdenes y permisos existentes permanece vigente y fuera del alcance de esta especificación.
- La actualización del contrato OpenAPI se limita a la información de seguridad y a la respuesta 401 de `/orders/buy` y `/orders/sell`; no se modifican sus cuerpos ni respuestas funcionales existentes.
- Los demás endpoints no se protegen como parte de esta feature.
