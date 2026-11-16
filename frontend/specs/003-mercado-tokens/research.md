# Research: Mercado de Tokens de Jugadores

**Feature**: 003-mercado-tokens | **Phase**: 0 — Research | **Date**: 2026-11-16

## 1. ID de Usuario desde el JWT

**Decision**: Extraer el `userId` numérico del payload JWT decodificado en Base64.

**Rationale**: El endpoint `POST /login` devuelve un `UserResponse` que solo contiene `{ nombre: string }`, sin exponer `id`. Sin embargo, múltiples endpoints del backend requieren el `userId` como parámetro de ruta o query (`/users/{usuarioId}/orders`, `/users/{usuarioId}/transactions`, `/users/{usuarioId}/holdings`, `/strategy/{usuarioId}`, `/strategy/{usuarioId}/configure`). La práctica estándar para JWT es embeber el subject (`sub`) en el payload con el identificador del usuario. Se decodificará el payload (segunda sección del token, Base64url) en `AuthContext` al momento del login para extraer el campo `sub` (o equivalente) como `userId`.

**Alternatives considered**:
- Llamar a `GET /users/{id}` al iniciar sesión — descartado porque requiere conocer `id` antes de hacer la llamada (problema circular).
- Almacenar solo `nombre` y usarlo como identificador en URLs — descartado porque los endpoints esperan ID numérico entero.
- Extender `UserResponse` en el backend para incluir `id` — fuera del alcance del frontend (cambio de backend).

**Implementation note**: `AuthContext` expondrá tanto `authToken` como `userId` (number | null). El parsing se realiza en `parseJwtPayload(token)` extraído a `src/utils/jwt.js`.

---

## 2. Biblioteca de gráficos (PortfolioPieChart)

**Decision**: Usar **Recharts** (`recharts` ≥ 2.x).

**Rationale**: Recharts es la biblioteca de gráficos más popular en el ecosistema React (>10M descargas semanales), con soporte nativo para React 18/19 mediante componentes declarativos, PieChart incluido, y compatibilidad probada con TailwindCSS. Es ligera (~200 KB min+gzip), bien mantenida y no requiere configuración adicional de Vite.

**Alternatives considered**:
- **Chart.js + react-chartjs-2**: Funciona pero requiere registro manual de componentes y una API imperativa menos idiomática para React.
- **Victory**: Más pesada y menos popular en proyectos React modernos.
- **D3.js directo**: Demasiado bajo nivel para un gráfico de torta simple; requiere manejo manual del DOM.

**Action required**: Instalar `recharts` como dependencia de producción (`pnpm add recharts`).

---

## 3. Routing con react-router v8 (layout sidebar)

**Decision**: Usar **Outlet** de react-router v8 con rutas anidadas para el layout con sidebar.

**Rationale**: React Router v8 (que es el que usa el proyecto — `"react-router": "^8.4.0"`) soporta rutas anidadas mediante `<Outlet />`. El patrón correcto es crear una ruta padre `/app` (o `/`) protegida que renderiza `AppLayout` (sidebar + área principal) y rutas hijas (`/app/catalogo`, `/app/comerciar`, `/app/portfolio`, `/app/estrategia`, `/app/perfil`) que se inyectan en el `<Outlet />` del layout. Esto evita re-renders del sidebar al navegar y mantiene el sidebar siempre visible.

**Alternatives considered**:
- Renderizar el sidebar manualmente en cada página — descartado porque duplica código y causa re-renders innecesarios.
- Usar estado global para controlar la sección visible (single-page sin routing) — descartado porque rompe la navegación por URL y el botón Back del navegador.

**Route structure**:
```
/login          → LoginPage
/register       → RegisterPage
/app            → ProtectedRoute → AppLayout (sidebar)
  /app/catalogo   → CatalogoPage
  /app/comerciar  → ComerciarPage
  /app/portfolio  → PortfolioPage
  /app/estrategia → EstrategiaPage
  /app/perfil     → ProfilePage
/               → redirect → /app/catalogo (si autenticado) o /login
```

---

## 4. Interceptor Axios para JWT

**Decision**: Añadir un interceptor de request en `apiClient.js` que inyecte el header `Authorization: Bearer <token>` para todos los requests que lo requieran.

**Rationale**: Actualmente `apiClient.js` no adjunta el token JWT. Los endpoints de órdenes (`POST /orders/buy`, `POST /orders/sell`) requieren `bearerAuth`. El patrón estándar en proyectos Axios es un interceptor de request que lee el token del `localStorage` (o del contexto) y lo adjunta automáticamente. Esto centraliza la lógica de autenticación sin modificar cada llamada individual.

**Alternatives considered**:
- Pasar el token como parámetro a cada función de servicio — descartado porque viola el principio DRY y dispersa la lógica de autenticación.
- Crear un segundo `apiClient` autenticado — descartado por innecesario; el interceptor puede omitir el header si el token es null.

**Implementation**: El interceptor leerá `localStorage.getItem('authToken')` en cada request (no en la inicialización del cliente) para capturar cambios de sesión.

---

## 5. Filtros y ordenamiento del catálogo (client-side vs server-side)

**Decision**: Implementar filtros y ordenamiento **client-side** sobre el array de jugadores ya cargado.

**Rationale**: El endpoint `GET /players` devuelve todos los jugadores en una única respuesta. No existe un endpoint de búsqueda/filtrado en el backend (el OpenAPI no lo expone). Por lo tanto, los filtros por liga, equipo y nacionalidad, y el ordenamiento por métricas, se aplican directamente sobre el array en memoria usando `Array.prototype.filter` y `Array.prototype.sort`.

**Alternatives considered**:
- Agregar parámetros de query al endpoint `/players` — requiere cambio de backend, fuera de alcance.
- Paginación server-side — no existe soporte en el backend actual.

**Performance note**: Con el volumen esperado de jugadores (≤ 1000 registros), las operaciones client-side son < 10 ms y no requieren virtualización de listas.

---

## 6. Cálculo de precio total en órdenes

**Decision**: El precio unitario proviene del endpoint `GET /players/{playerId}/quotes/{userId}/current` (valuación actual según la estrategia del usuario). Los cálculos de precio total y tolerancia se realizan en el frontend:

- **Compra**: `totalSinTolerancia = cantidad × precioUnitario`; `totalConTolerancia = totalSinTolerancia + tolerancia`
- **Venta**: `totalSinTolerancia = cantidad × precioUnitario`; `totalConTolerancia = totalSinTolerancia − (cantidad × toleranciaPorToken)`

**Rationale**: El spec define explícitamente las fórmulas (FR-018, FR-024) y el backend devuelve el `valorUnitario` en `ValuationResponse`. El frontend solo muestra los cálculos; el backend valida la orden al momento de procesarla.

**Note**: El campo `price` del `CrearOrdenRequest` enviado al backend corresponde al `valorUnitario` de la `ValuationResponse`; `tolerance` es el valor absoluto ingresado por el usuario.

---

## 7. Bloqueo de tokens en órdenes de venta

**Decision**: La cantidad de tokens "disponibles" para vender se calcula como: `tokensDisponibles = holding.tokens − tokensEnOrdenesVentaActivas`. Los `tokensEnOrdenesVentaActivas` se obtienen sumando `quantity` de las `OrdenPendienteResponse` donde `operation === 'SELL'`.

**Rationale**: El backend no expone directamente "tokens bloqueados", pero `GET /users/{usuarioId}/orders` devuelve las órdenes pendientes incluyendo su `operation` y `quantity`. El frontend puede calcular el disponible localmente con estos datos.

**Data sources**:
- `GET /users/{usuarioId}/holdings` → `HoldingResponse[]` (campo `tokens`)
- `GET /users/{usuarioId}/orders` → `OrdenPendienteResponse[]` (campo `quantity`, `operation`, `playerId`)

---

## 8. Estrategia de valuación — estado "sin estrategia"

**Decision**: Cuando `GET /strategy/{usuarioId}` devuelve `{ estrategia: null, estado: ... }` (o similar), el frontend considera que el usuario no tiene estrategia configurada y bloquea los botones de Comprar/Vender con un mensaje descriptivo.

**Rationale**: El `StrategyStateResponse` define `estrategia` como nullable. Cuando es `null`, el spec requiere mostrar un mensaje claro (FR-014, FR-033).

---

## 9. Agrupación "Otros" en el gráfico de torta

**Decision**: Si el usuario tiene tokens de más de 10 jugadores, se muestran los 10 de mayor `valorTotal` individualmente y el resto se agrupan en un segmento "Otros" cuyo valor es la suma de los `valorTotal` restantes.

**Rationale**: FR-031 requiere la agrupación. `HoldingResponse.valorTotal` es el valor actual de todos los tokens de ese jugador, que es exactamente el valor a usar para el gráfico.

---

## 10. Formato de créditos y cantidades

**Decision**:
- Tokens: `<input type="number" step="1" min="1">` con validación de número entero.
- Créditos/tolerancia: `<input type="number" step="0.01" min="0">` con redondeo a 2 decimales en display.
- El campo `créditos disponibles del usuario` no está expuesto directamente en el `UserResponse` ni en el `HoldingResponse`. **NEEDS CLARIFICATION → se asume que los créditos del usuario se obtienen de `GET /users/{id}` si el backend los expone, o del contexto de autenticación**. Revisar si `UserResponse` incluye créditos en la implementación real del backend (el OpenAPI solo declara `nombre`).

**Action required**: Verificar si el backend devuelve créditos en algún endpoint. Si no, el modal de compra no podrá mostrar "créditos insuficientes" basado en datos reales y deberá depender del error 400 del backend.
