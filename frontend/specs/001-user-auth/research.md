# Research: Autenticación de usuario en frontend

## Decisión: Usar la API REST real del backend

- **Decisión**: Consumir `http://localhost:8080/login` y `http://localhost:8080/register` con Axios.
- **Rationale**: `AuthControllerRest` es la fuente de verdad disponible y recibe `UserRequestDTO` con `nombre` y `password`. No se implementa mock ni autenticación simulada.
- **Alternativas consideradas**: Mock local o almacenamiento frontend-only; descartadas porque no cumplen la integración requerida con el backend.

## Decisión: Usar POST para autenticación

- **Decisión**: Enviar `POST /login` y `POST /register`.
- **Rationale**: El controller inspeccionado tiene `@PostMapping` para ambos endpoints y el requerimiento fue confirmado con esa corrección.
- **Alternativas consideradas**: GET con credenciales en query string; descartada por incompatibilidad con el controller y por exponer credenciales en URLs.

## Decisión: Extraer el JWT del header Authorization

- **Decisión**: En login, leer `response.headers.authorization`, quitar el prefijo `Bearer ` y guardar el token bajo `authToken` en `localStorage`.
- **Rationale**: `AuthControllerRest` construye explícitamente `Authorization: Bearer <token>`. El body sólo contiene `UserResponseDTO`, por lo que no debe buscarse el token en JSON.
- **Alternativas consideradas**: Guardar el header completo o un token devuelto en el body; descartadas para evitar formatos inconsistentes con el contrato actual.

## Decisión: Sesión global y rutas protegidas

- **Decisión**: Un `AuthContext` leerá `authToken` al iniciar, expondrá login/logout y un `ProtectedRoute` redirigirá a `/login` si no hay token. Logout hará `localStorage.removeItem('authToken')` y navegará a `/login`.
- **Rationale**: Centraliza el estado de autenticación y evita duplicar acceso a storage en las páginas.
- **Alternativas consideradas**: Consultar `localStorage` directamente desde cada página; descartada por duplicación y mayor riesgo de estados inconsistentes.

## Decisión: Validación compartida en cliente

- **Decisión**: Una función pura validará nombre y contraseña antes de llamar a la API: longitud inclusiva de 4 a 16; nombre sin espacios; contraseña sin restricción de caracteres.
- **Rationale**: Login y registro deben mostrar exactamente las mismas reglas y bloquear requests inválidos.
- **Alternativas consideradas**: Validar sólo con atributos HTML o sólo en backend; descartadas porque no garantizan feedback consistente ni cumplen la validación previa visible.

## Decisión: Manejo de errores y carga

- **Decisión**: El servicio propagará errores Axios; las vistas mostrarán carga, errores de validación, errores HTTP y ausencia de token de forma visible en español.
- **Rationale**: La constitución exige manejo explícito de fallos y feedback accionable.
- **Alternativas consideradas**: Capturas silenciosas o mensajes técnicos crudos; descartadas por incumplir robustez y experiencia de usuario.
