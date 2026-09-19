# Implementation Plan: Autenticación de usuario en frontend

**Branch**: `001-user-auth` | **Date**: 2026-09-18 | **Spec**: `/frontend/specs/001-user-auth/spec.md`

**Input**: Especificación de interfaces de registro, inicio de sesión, Home y persistencia del token JWT mediante la API REST existente, con endpoints de autenticación confirmados como `POST`.

## Summary

Implementar el flujo frontend completo de autenticación con React + Vite: pantallas `/login` y `/register`, navegación protegida hacia Home, validación compartida de credenciales, consumo modular de `http://localhost:8080/login` y `http://localhost:8080/register` mediante Axios, almacenamiento del JWT recibido en `localStorage` y eliminación del token al cerrar sesión. Ambos endpoints se consumirán mediante `POST`.

## Technical Context

**Language/Version**: JavaScript ES modules, React 19, Vite 8

**Primary Dependencies**: React, `react-router` (no `react-router-dom`), Axios, TailwindCSS

**Storage**: `localStorage` del navegador, con una clave única documentada para el token JWT

**Testing**: Comandos existentes de npm (`npm run lint`, `npm run build`); pruebas manuales end-to-end contra el backend local porque el proyecto no tiene runner de tests frontend configurado

**Target Platform**: Navegadores modernos en desarrollo local, con frontend Vite y backend en `http://localhost:8080`

**Project Type**: Aplicación web frontend SPA dentro de un monorepo

**Performance Goals**: Render inicial y navegación entre rutas sin recargas completas; feedback visible de carga y error durante cada request

**Constraints**: No realizar requests desde componentes de presentación; no usar `react-router-dom`; respetar validaciones de 4-16 caracteres, sin espacios para usuario, y aceptar cualquier carácter para contraseña; no asumir mocks ni backend frontend-only

**Scale/Scope**: Tres vistas funcionales (login, registro y Home), un servicio HTTP de autenticación, una capa mínima de sesión y componentes reutilizables de formulario/navbar

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Pass: La solución mantiene la separación del monorepo y se limita al frontend.
- Pass: Cada vista y componente tendrá una responsabilidad única; la lógica de validación y sesión será reutilizable.
- Pass: Todos los llamados HTTP estarán en módulos de servicios Axios, no en componentes.
- Pass: Login, registro, carga, respuestas inválidas y fallos de red tendrán feedback visible y mensajes accionables en español.
- Pass: El stack usa React, `react-router`, Axios y TailwindCSS como exige la constitución.
- Pass: La persistencia del JWT se limita a `localStorage`; logout elimina la misma clave y las rutas protegidas no exponen Home sin sesión.

## Research Summary

- Se usará el contrato real de `AuthControllerRest`: `POST /login` y `POST /register`, JSON `{nombre, password}`.
- Login devuelve el usuario en el body y el JWT en el header `Authorization: Bearer <token>`; el servicio deberá extraer y persistir el valor del header.
- Registro devuelve el usuario en el body sin token; luego de éxito se navega a `/login`.
- La API base será `http://localhost:8080`, centralizada en un cliente Axios.
- La clave de almacenamiento será `authToken`; se leerá para proteger Home y se eliminará al cerrar sesión.

## Project Structure

### Documentation (this feature)

```text
frontend/specs/001-user-auth/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── auth-api.yaml
└── tasks.md
```

### Source Code (frontend)

```text
frontend/
├── src/
│   ├── components/
│   │   ├── AuthForm.jsx
│   │   ├── ProfileMenu.jsx
│   │   └── ProtectedRoute.jsx
│   ├── context/
│   │   └── AuthContext.jsx
│   ├── pages/
│   │   ├── LoginPage.jsx
│   │   ├── RegisterPage.jsx
│   │   └── HomePage.jsx
│   ├── services/
│   │   ├── apiClient.js
│   │   └── authService.js
│   ├── utils/
│   │   └── validation.js
│   ├── App.jsx
│   └── main.jsx
├── package.json
└── vite.config.js
```

**Structure Decision**: SPA React organizada por responsabilidades: pages para composición de vistas, components para UI reutilizable, services para Axios, context para sesión global y utils para reglas puras de validación. El router se configura en `App.jsx` usando exclusivamente `react-router`.

## Implementation Phases

### Phase 0: Research

Documentar el contrato del controller backend, la extracción del JWT desde `Authorization`, las decisiones de navegación/protección de rutas y las reglas de validación compartidas en `research.md`.

### Phase 1: Design & Contracts

Definir entidades de sesión y credenciales en `data-model.md`, documentar los endpoints y respuestas en `contracts/auth-api.yaml`, y describir el flujo de validación manual en `quickstart.md`.

### Phase 2: Implementation

Agregar dependencias, construir el cliente Axios y servicio de autenticación, implementar validación y estado de sesión, montar rutas `/login`, `/register` y Home protegido, y configurar estilos TailwindCSS sin alterar el contrato backend.

## Post-Design Constitution Check

- Pass: `research.md` fija la API real y evita mocks o requests desde la presentación.
- Pass: `data-model.md` separa credenciales, usuario autenticado y sesión, manteniendo responsabilidades acotadas.
- Pass: `contracts/auth-api.yaml` refleja los métodos `POST`, el payload `{nombre, password}` y el JWT en `Authorization`.
- Pass: `quickstart.md` cubre validación, feedback de errores, persistencia, protección de Home y logout.
- Pass: No se requiere excepción constitucional.

## Complexity Tracking

No hay violaciones constitucionales ni complejidad excepcional que requiera justificación.
