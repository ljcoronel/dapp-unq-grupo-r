# Data Model: Autenticación de usuario

## Credenciales

Representa los datos ingresados en login o registro.

| Campo | Tipo | Reglas |
|---|---|---|
| `nombre` | string | Obligatorio; longitud entre 4 y 16 caracteres inclusive; no puede contener espacios |
| `password` | string | Obligatorio; longitud entre 4 y 16 caracteres inclusive; acepta espacios y cualquier tipo de carácter |

La validación se ejecuta antes de cada request y se comparte entre ambas pantallas.

## Usuario autenticado

Representa la respuesta de autenticación recibida del backend.

| Campo | Tipo | Origen |
|---|---|---|
| `nombre` | string | `UserResponseDTO` del body de login o registro |
| `token` | string | Header `Authorization`, sólo en login |

## Sesión

Estado global mínimo de la aplicación:

| Campo | Tipo | Persistencia |
|---|---|---|
| `token` | string o null | `localStorage.authToken` |
| `isAuthenticated` | boolean derivado | En memoria |
| `isLoading` | boolean | En memoria durante login/registro |
| `error` | string o null | En memoria para feedback de la vista |

## Transiciones

1. `sin sesión` → `autenticando`: el usuario envía login válido.
2. `autenticando` → `sesión activa`: respuesta 200 con header Bearer; se guarda `authToken` y se navega a Home.
3. `autenticando` → `sin sesión`: error HTTP, error de red o token ausente; se muestra mensaje.
4. `sin sesión` → `registrando`: el usuario envía registro válido.
5. `registrando` → `sin sesión`: registro 200; se navega a `/login` sin crear sesión automáticamente.
6. `sesión activa` → `sin sesión`: logout; se elimina `authToken` y se navega a `/login`.
