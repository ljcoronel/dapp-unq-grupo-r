# Feature Specification: Autenticacion y perfil de usuario

**Feature Branch**: `001-user-auth`

**Created**: 2026-09-08

**Status**: Draft

**Input**: User description: "Crear un proyecto de 0 con arquitectura en capas. Se necesita una API REST para usuarios y autenticacion con PostgreSQL local y Testcontainers para tests. Debe existir un endpoint GET /users/:id/, POST /login y POST /register. Los controllers Usuarios/Auth, GlobalExceptionHandler, DTOs UserRequestDTO/UserResponseDTO y local credentials postgres/root."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consultar perfil por id (Priority: P1)
El usuario necesita consultar el nombre asociado a su identificador para poder recuperar su perfil en la API.

**Why this priority**: Es la consulta base del usuario y el punto de entrada para obtener datos del perfil sin depender de una sesión activa.

**Independent Test**: Se valida con una solicitud GET a /users/{id}/ usando un usuario existente.

**Acceptance Scenarios**:
1. **Given** un usuario registrado en la base de datos, **When** se hace GET /users/{id}/, **Then** la API responde 200 y devuelve `{"nombre":"string"}`.
2. **Given** un identificador inexistente, **When** se hace GET /users/{id}/, **Then** la API responde 404 con el mensaje "Usuario no encontrado".

---

### User Story 2 - Iniciar sesión con credenciales válidas (Priority: P1)
El usuario quiere autenticarse para obtener un token de acceso que se utilizará en requests protegidas.

**Why this priority**: Es el flujo principal de autenticación y debe devolver un token Bearer en la respuesta.

**Independent Test**: Se puede probar con una petición POST /login con nombre y password correctos.

**Acceptance Scenarios**:
1. **Given** un usuario ya registrado con nombre y password válidos, **When** se hace POST /login, **Then** la API responde 200 y devuelve `{"nombre":"string"}` con el header `Authorization: Bearer <token>`.
2. **Given** credenciales inválidas, **When** se hace POST /login, **Then** la API responde 400 con "Credenciales inválidas".

---

### User Story 3 - Crear una cuenta nueva (Priority: P1)
El usuario necesita registrarse para poder iniciar sesión en la aplicación.

**Why this priority**: Es el primer paso para permitir acceso al sistema y la creación de cuentas en la base de datos.

**Independent Test**: Se prueba con una request POST /register con un nombre no repetido.

**Acceptance Scenarios**:
1. **Given** un nombre nuevo y una password válida, **When** se hace POST /register, **Then** la API responde 200 y devuelve `{"nombre":"string"}`.
2. **Given** un nombre ya existente, **When** se hace POST /register, **Then** la API responde 400 con "Usuario existente".

---

### Edge Cases
- Si el nombre o la password se envían vacíos, la request debe ser rechazada con 400 y un mensaje de validación / error.
- Si el usuario no existe, GET /users/{id}/ debe responder 404.
- Si se intenta registrar una cuenta duplicada, la API debe devolver 400 y el mensaje "Usuario existente".
- Si la login falla por credenciales incorrectas, la API debe responder 400 y "Credenciales inválidas".

## Requirements *(mandatory)*

### Functional Requirements
- **FR-001**: The system MUST expose `GET /users/{id}/` and return the user name for an existing record as `{"nombre":"string"}`.
- **FR-002**: The system MUST return HTTP 404 with the message "Usuario no encontrado" when the user id does not exist.
- **FR-003**: The system MUST expose `POST /login` accepting a JSON body with `nombre` and `password`.
- **FR-004**: The system MUST validate credentials and, on success, return HTTP 200, the user name payload, and the `Authorization` header with a Bearer token.
- **FR-005**: The system MUST reject invalid credentials with HTTP 400 and the message "Credenciales inválidas".
- **FR-006**: The system MUST expose `POST /register` accepting JSON with `nombre` and `password`.
- **FR-007**: The system MUST reject duplicate registrations with HTTP 400 and the message "Usuario existente".
- **FR-008**: The system MUST use `UserControllerRest` as the user-oriented controller and `AuthControllerRest` as the authentication controller.
- **FR-009**: The system MUST provide a `GlobalExceptionHandler` with dedicated handlers for controller exceptions.
- **FR-010**: The system MUST use DTO records named `UserRequestDTO` and `UserResponseDTO` for request/response payloads.
- **FR-011**: The system MUST persist users in PostgreSQL and run tests with Testcontainers, not against the local user profile.
- **FR-012**: The system MUST support a local profile with credentials `postgres` / `root` against `jdbc:postgresql://localhost:5432/dappfc`.

### Key Entities *(include if feature involves data)*
- **Usuario**: Representa la cuenta del usuario. Incluye identificador, nombre visible y password almacenada como hash.
- **Sesion**: Token Bearer emitido tras login exitoso y utilizado por endpoints protegidos.
- **UserRequestDTO**: DTO de request para nombre y password en login o registro.
- **UserResponseDTO**: DTO de response para devolver el nombre del usuario.

## Success Criteria *(mandatory)*

### Measurable Outcomes
- **SC-001**: Un usuario existente puede consultar su perfil por id y recibe 200 con `{"nombre":"string"}`.
- **SC-002**: Un identificador inexistente devuelve 404 con "Usuario no encontrado".
- **SC-003**: Login con credenciales correctas devuelve 200 y un header `Authorization` con token Bearer.
- **SC-004**: Login con credenciales incorrectas devuelve 400 con "Credenciales inválidas".
- **SC-005**: Registro con usuario nuevo devuelve 200 y con nombre repetido devuelve 400 con "Usuario existente".
- **SC-006**: La suite de tests de integración usa PostgreSQL real levantado con Testcontainers y no depende del perfil local de ejecución.

## Assumptions
- La base de datos local ya existe como `dappfc` y corre en PostgreSQL en localhost:5432.
- El perfil local de la app usa `postgres` / `root` sólo para entorno de desarrollo.
- El idioma de mensajes y respuestas será español.
- El proyecto se desarrollará como monorepo con backend y frontend, aunque la entrega funcional base es un backend REST.
- La contraseña se almacenará en hash y no como texto plano.
