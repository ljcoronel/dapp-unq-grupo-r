# Implementation Plan: Mercado de Tokens de Jugadores

**Branch**: `003-mercado-tokens` | **Date**: 2026-11-16 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/frontend/specs/003-mercado-tokens/spec.md`

## Summary

Rediseño completo de la interfaz del frontend: se introduce un sidebar de navegación permanente con cuatro secciones (Catálogo, Comerciar, Portfolio y Estrategia), un catálogo unificado de jugadores con filtros y ordenamiento, compra y venta de tokens mediante órdenes con tolerancia de precio, portfolio con historial, estrategia de valuación configurable y visualización de cotizaciones en gráfico de torta. El flujo de autenticación existente (login/registro/JWT en localStorage) se mantiene sin cambios. La solución reutiliza React 19, react-router 8, TypeScript, el cliente HTTP tipado `openapi-fetch` y TailwindCSS.

## Technical Context

**Language/Version**: TypeScript 5.9 (strict mode), React 19, ES2022, Node.js runtime (Vite 8)

**Primary Dependencies**:
- `react` ^19.2.8 — UI framework
- `react-router` ^8.4.0 — routing (NO react-router-dom)
- `openapi-fetch` ^0.17.0 — HTTP client tipado a partir del schema OpenAPI
- `openapi-typescript` ^7.13.0 — generación de tipos del schema OpenAPI
- `typescript` ^5.9.3 — lenguaje y verificación estática
- `tailwindcss` ^3.4.17 — styling
- Charting: Recharts (ver research.md)

**Storage**: `localStorage` para JWT (`authToken`); sin base de datos en frontend.

**Testing**: `npm run typecheck` (TypeScript) y `npm run build`; no hay framework de pruebas automatizadas declarado en `package.json`.

**Target Platform**: SPA web (desktop y móvil, viewport 320px–∞).

**Project Type**: Single-Page Application (React SPA, Vite bundler).

**Performance Goals**: Carga inicial < 3 s en conexión estándar; interacciones del catálogo (filtros/ordenamiento) < 100 ms (client-side).

**Constraints**:
- API base: `http://localhost:8080`
- Todos los servicios consumen la API mediante el cliente compartido `openapi-fetch` y sus tipos generados desde OpenAPI; todo código nuevo se escribe en TypeScript.
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
| Request Robustness and Failure Handling | ✅ PASS | Los requests usan el cliente compartido `openapi-fetch` y manejo explícito de errores de red, respuestas fallidas y datos nulos. |
| User State Feedback | ✅ PASS | Cada vista expone estados de carga, error y vacío mediante componentes reutilizables. |
| Code and Style Standards | ✅ PASS | TypeScript en modo estricto, TailwindCSS, indentación 2 espacios, arrow functions, desestructuración de objetos y async/await. |
| Technology Stack | ✅ PASS | React, react-router (no react-router-dom), TypeScript, `openapi-fetch` y TailwindCSS — stack confirmado en `package.json` y constitution. |

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
│   ├── App.tsx                        # Actualizado: rutas sidebar + perfil
│   ├── context/
│   │   └── AuthContext.tsx            # Existente; exponer userId parseado de JWT
│   ├── components/
│   │   ├── Sidebar.tsx                # NUEVO: navegación lateral permanente
│   │   ├── AppLayout.tsx              # NUEVO: layout base (sidebar + main)
│   │   ├── PlayerTable.tsx            # NUEVO: tabla unificada con filtros/sort
│   │   ├── PlayerFilters.tsx          # NUEVO: controles de filtro (liga/equipo/nac)
│   │   ├── BuyModal.tsx               # NUEVO: modal de compra de tokens
│   │   ├── SellModal.tsx              # NUEVO: modal de venta de tokens
│   │   ├── OrderList.tsx              # NUEVO: lista de órdenes pendientes
│   │   ├── HoldingList.tsx            # NUEVO: lista de tenencias
│   │   ├── TransactionHistory.tsx     # NUEVO: historial de transacciones
│   │   ├── PortfolioPieChart.tsx      # NUEVO: gráfico de torta del portfolio
│   │   ├── StrategyForm.tsx           # NUEVO: formulario de configuración de estrategia
│   │   ├── StrategyDisplay.tsx        # NUEVO: visualización de estrategia actual
│   │   ├── ProfileMenu.tsx            # Existente; corregir botón "Ir a mi perfil"
│   │   ├── AuthForm.tsx               # Existente; sin cambios
│   │   └── ProtectedRoute.tsx         # Existente; sin cambios
│   ├── pages/
│   │   ├── CatalogoPage.tsx           # NUEVO: catálogo unificado
│   │   ├── ComerciarPage.tsx          # NUEVO: sección comerciar
│   │   ├── PortfolioPage.tsx          # NUEVO: portfolio + historial
│   │   ├── EstrategiaPage.tsx         # NUEVO: estrategia de valuación
│   │   ├── ProfilePage.tsx            # NUEVO: perfil de usuario
│   │   ├── HomePage.tsx               # Reemplazado por CatalogoPage
│   │   ├── LoginPage.tsx              # Existente; sin cambios
│   │   └── RegisterPage.tsx           # Existente; sin cambios
│   ├── api/
│   │   ├── client.ts                 # Cliente openapi-fetch con middleware JWT
│   │   └── schema.d.ts               # Tipos generados con openapi-typescript
│   ├── services/
│   │   ├── authService.ts             # Existente; sin cambios
│   │   ├── playerService.ts           # Existente + nuevos métodos de cotización
│   │   ├── orderService.ts            # NUEVO: órdenes (buy, sell, cancel, list)
│   │   ├── strategyService.ts         # NUEVO: estrategia (get, configure)
│   │   ├── holdingService.ts          # NUEVO: tenencias
│   │   └── userService.ts             # NUEVO: perfil de usuario
│   └── utils/
│       └── jwt.ts                     # Parseo del payload JWT
└── specs/003-mercado-tokens/          # Documentación de la feature
```

**Structure Decision**: Single project (frontend SPA). No se introduce ningún proyecto adicional. La estructura sigue el patrón `components/pages/services` existente, con el cliente tipado en `src/api/client.ts`, el schema generado en `src/api/schema.d.ts` y código React en archivos `.tsx` y `.ts`.

## Complexity Tracking

> No hay violaciones de Constitution Check que justificar.
