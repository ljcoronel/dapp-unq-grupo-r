# Implementation Plan: Mercado de Tokens de Jugadores

**Branch**: `003-mercado-tokens` | **Date**: 2026-11-16 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/frontend/specs/003-mercado-tokens/spec.md`

## Summary

Rediseño completo de la interfaz del frontend: se introduce un sidebar de navegación permanente con cuatro secciones (Catálogo, Comerciar, Portfolio y Estrategia), un catálogo unificado de jugadores con filtros y ordenamiento, compra y venta de tokens mediante órdenes con tolerancia de precio, portfolio con historial, estrategia de valuación configurable y visualización de cotizaciones en gráfico de torta. El flujo de autenticación existente (login/registro/JWT en localStorage) se mantiene sin cambios. La solución reutiliza la infraestructura de React 19 + react-router 8 + Axios + TailwindCSS ya presente en el proyecto.

## Technical Context

**Language/Version**: JavaScript (ES2022+), React 19, Node.js runtime (Vite 8)

**Primary Dependencies**:
- `react` ^19.2.8 — UI framework
- `react-router` ^8.4.0 — routing (NO react-router-dom)
- `axios` ^1.20.0 — HTTP client
- `tailwindcss` ^3.4.17 — styling
- Charting: NEEDS CLARIFICATION (ver research.md — se resolverá en recharts o chart.js)

**Storage**: `localStorage` para JWT (`authToken`); sin base de datos en frontend.

**Testing**: Sin framework de testing declarado en package.json (NEEDS CLARIFICATION — ver research.md).

**Target Platform**: SPA web (desktop y móvil, viewport 320px–∞).

**Project Type**: Single-Page Application (React SPA, Vite bundler).

**Performance Goals**: Carga inicial < 3 s en conexión estándar; interacciones del catálogo (filtros/ordenamiento) < 100 ms (client-side).

**Constraints**:
- API base: `http://localhost:8080`
- JWT almacenado en `localStorage` bajo la clave `authToken`; se adjunta como `Authorization: Bearer <token>` en endpoints protegidos.
- `UserResponse` del backend solo expone `nombre`; el `userId` numérico necesario para los endpoints `/users/{usuarioId}/*` y `/strategy/{usuarioId}/*` debe extraerse del payload JWT (campo `sub` o similar) — NEEDS CLARIFICATION (ver research.md § ID de usuario).
- Rutas con react-router (no react-router-dom).
- TailwindCSS para todo el styling; sin frameworks CSS adicionales.

**Scale/Scope**: ~10 vistas/secciones, ~15 componentes nuevos, ~6 módulos de servicio API nuevos.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| Responsive Web Application | ✅ PASS | Se usarán utilidades responsive de TailwindCSS (breakpoint prefixes); sidebar colapsable en mobile. |
| Single Responsibility per Component | ✅ PASS | Cada componente (Sidebar, PlayerTable, BuyModal, SellModal, etc.) tiene una única responsabilidad declarada en data-model.md. |
| Modular API Consumption | ✅ PASS | Todos los nuevos endpoints se encapsulan en módulos de servicio dedicados (`orderService`, `strategyService`, `holdingService`, `userService`). |
| Request Robustness and Failure Handling | ✅ PASS | Todos los requests usan Axios con manejo explícito de errores de red, respuestas fallidas y datos nulos. |
| User State Feedback | ✅ PASS | Cada vista expone estados de carga, error y vacío mediante componentes reutilizables. |
| Code and Style Standards | ✅ PASS | TailwindCSS, indentación 2 espacios, arrow functions, desestructuración de objetos, async/await. |
| Technology Stack | ✅ PASS | React, react-router (no react-router-dom), Axios, TailwindCSS — stack confirmado en package.json y constitution. |

**Post-design re-check**: Ver sección al final de Phase 1.

## Project Structure

### Documentation (this feature)

```text
frontend/specs/003-mercado-tokens/
├── plan.md              # Este archivo
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   ├── api-services.md
│   └── component-contracts.md
└── tasks.md             # Phase 2 output (/speckit-tasks — NO creado por /speckit-plan)
```

### Source Code (repository root)

```text
frontend/
├── src/
│   ├── App.jsx                        # Actualizado: rutas sidebar + perfil
│   ├── context/
│   │   └── AuthContext.jsx            # Existente; exponer userId parseado de JWT
│   ├── components/
│   │   ├── Sidebar.jsx                # NUEVO: navegación lateral permanente
│   │   ├── AppLayout.jsx              # NUEVO: layout base (sidebar + main)
│   │   ├── PlayerTable.jsx            # NUEVO: tabla unificada con filtros/sort
│   │   ├── PlayerFilters.jsx          # NUEVO: controles de filtro (liga/equipo/nac)
│   │   ├── BuyModal.jsx               # NUEVO: modal de compra de tokens
│   │   ├── SellModal.jsx              # NUEVO: modal de venta de tokens
│   │   ├── OrderList.jsx              # NUEVO: lista de órdenes pendientes
│   │   ├── HoldingList.jsx            # NUEVO: lista de tenencias
│   │   ├── TransactionHistory.jsx     # NUEVO: historial de transacciones
│   │   ├── PortfolioPieChart.jsx      # NUEVO: gráfico de torta del portfolio
│   │   ├── StrategyForm.jsx           # NUEVO: formulario de configuración de estrategia
│   │   ├── StrategyDisplay.jsx        # NUEVO: visualización de estrategia actual
│   │   ├── ProfileMenu.jsx            # Existente; corregir botón "Ir a mi perfil"
│   │   ├── AuthForm.jsx               # Existente; sin cambios
│   │   └── ProtectedRoute.jsx         # Existente; sin cambios
│   ├── pages/
│   │   ├── CatalogoPage.jsx           # NUEVO: catálogo unificado
│   │   ├── ComerciarPage.jsx          # NUEVO: sección comerciar
│   │   ├── PortfolioPage.jsx          # NUEVO: portfolio + historial
│   │   ├── EstrategiaPage.jsx         # NUEVO: estrategia de valuación
│   │   ├── ProfilePage.jsx            # NUEVO: perfil de usuario
│   │   ├── HomePage.jsx               # Reemplazado por CatalogoPage
│   │   ├── LoginPage.jsx              # Existente; sin cambios
│   │   └── RegisterPage.jsx           # Existente; sin cambios
│   └── services/
│       ├── apiClient.js               # Existente; añadir interceptor JWT
│       ├── authService.js             # Existente; sin cambios
│       ├── playerService.js           # Existente + nuevos métodos de cotización
│       ├── orderService.js            # NUEVO: órdenes (buy, sell, cancel, list)
│       ├── strategyService.js         # NUEVO: estrategia (get, configure)
│       ├── holdingService.js          # NUEVO: tenencias
│       └── userService.js             # NUEVO: perfil de usuario
└── specs/003-mercado-tokens/          # Documentación de la feature
```

**Structure Decision**: Single project (frontend SPA). No se introduce ningún proyecto adicional. La estructura sigue el patrón `components/pages/services` existente, expandido con los nuevos módulos requeridos.

## Complexity Tracking

> No hay violaciones de Constitution Check que justificar.
