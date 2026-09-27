# Research: Autenticacion y perfil de usuario

## Decision

- Se implementará un monorepo con backend y frontend separados, manteniendo el backend como pieza central para la funcionalidad del MVP.
- El backend será una API REST en Java 25 con Spring Boot 4.
- La persistencia se hará en PostgreSQL local (`jdbc:postgresql://localhost:5432/dappfc`) usando la cuenta `postgres` / `root` para desarrollo.
- Las pruebas de integración y E2E se ejecutarán contra PostgreSQL real levantado por Testcontainers.
- La autenticación usará password hashing con BCrypt y tokens Bearer tipo JWT.
- Los DTOs se definirán como records Java: `UserRequestDTO` y `UserResponseDTO`.
- Los controllers se dividirán en `UserControllerRest` y `AuthControllerRest` y la traducción de errores se hará en `GlobalExceptionHandler`.

## Rationale

La constitución del proyecto exige una arquitectura en capas, monorepo, validación por niveles y tests con Testcontainers. Un backend REST con Spring Boot 4 satisface esas condiciones y permite una separación clara entre controlador, servicio, dominio y persistencia. El uso de DTOs records evita acoplar la respuesta de la API a entidades JPA. La elección de JWT/Bearer permite una autenticación stateless y un flujo compatible con la cabecera `Authorization` requerida por la especificación.

## Alternatives considered

- Session cookies: descartado porque la especificación exige claramente un token Bearer en el header `Authorization`, y JWT es más adecuado para APIs REST.
- Exponer entidades JPA directamente: descartado porque rompe la separación entre capa de persistencia y capa de API; los DTOs son obligatorios por la especificación.
- Ejecutar tests contra la base local: descartado por la constitución y por la necesidad de contar con PostgreSQL real levantado por Testcontainers en pruebas automáticas.
- Auth local sin token: descartado porque la especificación requiere una respuesta con header `Authorization` en la login.

## Open questions and assumptions resolved

- El perfil local de desarrollo usa `postgres` / `root`; los tests no usan esa configuración.
- El usuario se identifica por un id numérico o UUID interno y la API devuelve únicamente el nombre en el payload.
- La contraseña se guarda en hash y no como texto plano.
- Si el usuario no existe, la API devuelve 404 con el mensaje "Usuario no encontrado".
- Si el registro falla por nombre duplicado, la API devuelve 400 con "Usuario existente".
- Si el login falla, la API devuelve 400 con "Credenciales inválidas".
