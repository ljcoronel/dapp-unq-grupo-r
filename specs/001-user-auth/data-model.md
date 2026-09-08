# Data Model: Autenticacion y perfil de usuario

## Entidades principales

### Usuario

Representa la cuenta del usuario dentro del dominio.

Campos principales:
- id: identificador único interno de la entidad
- nombre: nombre de usuario visible para la API y la autenticación
- passwordHash: valor hash de la contraseña, nunca almacenado como texto plano
- createdAt: fecha de creación (opcional, recomendado para auditoría)

Reglas de validación:
- nombre no puede ser nulo ni vacío tras sanitización
- nombre debe ser único en la base de datos
- passwordHash debe existir tras el registro
- el dominio debe rechazar intentos de crear un usuario con un nombre duplicado

Relaciones:
- Un usuario tiene una sola cuenta asociada a una identidad de login
- La relación con la sesión es temporal y no requiere persistencia del token como entidad de negocio principal

### Sesion

Representa el token de acceso emitido luego de una autenticación exitosa.

Campos principales:
- token: valor JWT/Bearer emitido por la aplicación
- usuario: referencia al usuario autenticado
- expiracion: timestamp de expiración del token

Reglas de validación:
- el token debe ser emitido únicamente tras validación exitosa de nombre y password
- la expiración debe ser verificada antes de autorizar requests protegidas

## DTOs

### UserRequestDTO

Record Java para encapsular la entrada de request:
- nombre: String
- password: String

Valida formato y limpieza básica antes de pasar la información al servicio.

### UserResponseDTO

Record Java para encapsular la salida de la API:
- nombre: String

Se usa para responder 200 en login, register y consulta de perfil.

## Excepciones del dominio y de la capa API

- UserNotFoundException: cuando el id no existe
- InvalidCredentialsException: cuando nombre/password no coinciden
- UserAlreadyExistsException: cuando el usuario intenta registrarse con un nombre duplicado
- ValidationException o BadRequestException: para errores de request mal formados o inputs inválidos

Estas excepciones deben ser capturadas por `GlobalExceptionHandler` con un método dedicado para cada caso para devolver mensajes y códigos HTTP consistentes.

## Contract mapping

- GET /users/{id}/ -> `UserResponseDTO`
- POST /login -> `UserResponseDTO` + header `Authorization: Bearer <token>`
- POST /register -> `UserResponseDTO`

## Implementation notes

La capa de modelo debe validar invariantes del dominio; la capa de servicio debe resolver la entidad y decidir si la operación es válida; la capa de persistence debe persistir la entidad y recuperar la información del usuario; los controllers quedan restringidos a serializar/deserialize los DTOs y delegar la lógica al servicio.
