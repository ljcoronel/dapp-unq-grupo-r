# Implementation Plan: Catálogo de jugadores por liga

**Branch**: `002-catalogo-jugadores` | **Date**: 2026-09-27 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/002-catalogo-jugadores/spec.md`

## Summary

Al entrar a Home, obtener los jugadores mediante `GET /players` usando el `apiClient` existente y su `baseURL` actual (`http://localhost:8080`), sin enviar un token JWT. Presentar las cinco listas de la respuesta como tablas independientes de diez jugadores, con encabezados en español, datos ausentes representados con `-`, y controles para expandir o contraer cada liga. Comunicar los estados de carga y error en español.

## Technical Context

**Language/Version**: JavaScript ES modules, React 19

**Primary Dependencies**: React, React Router 8, Axios, TailwindCSS 3

**Storage**: N/A; datos consultados al entrar a Home y mantenidos en estado de interfaz.

**Testing**: `npm run lint` y `npm run build`; no hay script de pruebas automatizadas configurado.

**Target Platform**: Aplicación web frontend

**Project Type**: Aplicación web frontend

**Performance Goals**: Mostrar los estados de carga y error durante la consulta y renderizar los datos disponibles tras recibir la respuesta; el alcance máximo es de 50 jugadores.

**Constraints**: Conservar la `baseURL` actual; usar Axios y un módulo de servicio dedicado; la petición de jugadores es pública y no debe incluir JWT; interfaz y mensajes en español; aplicar TailwindCSS e indentación de dos espacios.

**Scale/Scope**: Home, servicio para jugadores, presentación de cinco tablas y comportamiento expandir/contraer por liga.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio | Evaluación |
|-----------|------------|
| Responsabilidad Única por Componente | PASS: separar consulta de datos y presentación de tablas/secciones. |
| Modularización del Consumo de API | PASS: encapsular `GET /players` en un servicio dedicado; Home no construye requests. |
| Robustez de Requests y Manejo de Fallos | PASS: usar el cliente Axios existente y tratar errores, datos nulos y respuesta inválida. |
| Feedback de Estado para el Usuario | PASS: Home comunica carga, error y ausencia de datos en español. |
| Estándares de Código y Estilo | PASS: React, React Router, Axios, TailwindCSS, arrow functions, async/await y dos espacios. |

No se requieren excepciones a la constitución.

## Project Structure

### Documentation (this feature)

```text
specs/002-catalogo-jugadores/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── players-api.yaml
└── tasks.md
```

### Source Code (frontend)

```text
src/
├── components/
│   └── PlayerLeagueTable.jsx   # Sección expandible y tabla de una liga
├── pages/
│   └── HomePage.jsx            # Solicitud, estados de pantalla y composición
└── services/
    ├── apiClient.js            # Cliente Axios y baseURL existente, sin cambios
    └── playerService.js        # Función dedicada para GET /players
```

**Structure Decision**: Aplicación frontend existente en `frontend/src`. Reutilizar Home, componentes y servicios existentes; agregar solo el servicio de jugadores y, si resulta útil al separar responsabilidades, un componente para la tabla por liga. No introducir dependencias.

## Design Decisions

- Usar `apiClient.get('/players')`, preservando la `baseURL` configurada actualmente en `src/services/apiClient.js`.
- La ruta `/home` continúa protegida por el flujo de autenticación existente; la consulta del catálogo, sin embargo, es pública y no requiere ni adjunta el JWT.
- Tratar la respuesta como cinco arreglos internos en el orden establecido por el endpoint, que corresponde al orden de ligas solicitado: Premier League, Bundesliga, Primera División, Serie A y Ligue 1.
- Mostrar la lista recibida para cada liga sin inventar ni completar jugadores. El objetivo funcional es diez jugadores por liga; el entorno de datos debe proporcionar diez en cada arreglo. La definición del endpoint permite arreglos vacíos o de hasta diez, por lo que los datos de origen deben verificarse en integración.
- Tomar los datos del jugador de `nombre`, `seccion`, `equipo`, `partidosJugados`, `goles`, `asistencias` y `penaltis`. `seccion` se presenta como Posición; cada valor ausente o `null` se presenta como `-`. Los ceros se conservan como datos válidos.
- Iniciar las cinco secciones expandidas; cada control modifica solo su propia sección y comunica su estado expandido.
- Mantener explícitos los estados de carga, error de red/respuesta fallida y respuesta sin datos. Mostrar errores y estados al usuario en español.

### Constitution Check (post-design)

| Principio | Evaluación |
|-----------|------------|
| Responsabilidad Única por Componente | PASS: servicio de consulta separado de Home y de la tabla. |
| Modularización del Consumo de API | PASS: Home llama al servicio dedicado. |
| Robustez de Requests y Manejo de Fallos | PASS: Axios, estados explícitos y valores `null` representados. |
| Feedback de Estado para el Usuario | PASS: carga, error y respuesta vacía tienen presentación en español. |
| Estándares de Código y Estilo | PASS: no se agrega stack ni dependencia; se conserva el estilo existente. |

## Complexity Tracking

No hay violaciones que justificar.
