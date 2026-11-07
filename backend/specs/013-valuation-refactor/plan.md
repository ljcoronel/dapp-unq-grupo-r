# Implementation Plan: Refactor de valuación — fórmula compuesta y superusuario

**Branch**: `013-valuation-refactor` | **Date**: 2026-10-30 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/013-valuation-refactor/spec.md`

## Summary

Refactor del motor de valuación para introducir la fórmula acumulativa `valorToken = valorBase + (result * 2)` en todas las ejecuciones de valuación (dominical, manual y al configurar la primera estrategia), y corrección del flujo de inicialización del Superusuario para que al arrancar el sistema las tenencias sean creadas primero, luego se configure la estrategia ACTUAL y finalmente los tokens sean valuados como consecuencia directa de la configuración de estrategia.

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Spring Boot 4, Spring Data JPA, Spring Security, SLF4J

**Storage**: PostgreSQL (accedido vía Hibernate/JPA con Testcontainers en tests)

**Testing**: JUnit 5, Mockito, Testcontainers (PostgreSQL), MockMvc

**Target Platform**: Linux server (backend REST API)

**Project Type**: Web service (monorepo backend/frontend)

**Performance Goals**: N/A para esta feature (cambio de fórmula puntual, sin impacto de escala)

**Constraints**: Valores monetarios almacenados con exactamente 2 decimales; redondeo HALF_UP. La constante `factorEscala = 2` está definida en código, no es configurable.

**Scale/Scope**: Afecta a `PlayerValuationCalculator`, `HoldingsValuationService`, `WeeklyValuationService` y `SuperuserInitializationService` / `InitialHoldingsService`. No hay cambios de esquema de base de datos.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio | Evaluación |
|-----------|-----------|
| **I. Monorepo** | ✅ PASS — Los cambios son exclusivamente en el módulo `backend/app`. No se crean nuevos módulos. |
| **II. Layered Architecture** | ✅ PASS — La nueva fórmula reside en `PlayerValuationCalculator` (Model). El `HoldingEntity.valuate()` recibe el valor ya calculado; el servicio no expone DTOs. El Repositorio sigue mapeando entidades. Sin violaciones de capa. |
| **III. Rich Model** | ✅ PASS — `PlayerValuationCalculator` encapsula la fórmula; `HoldingEntity.valuate()` aplica la invariante. Sin anotaciones de persistencia en modelos. La constante `FACTOR_ESCALA` vive en el Model. |
| **IV. Layered Validation** | ✅ PASS — `valorBase` se lee del estado persistido antes de aplicar la fórmula (responsabilidad del Service/Repository). La validación de no-negatividad de `valorUnitario` permanece en `HoldingEntity`. |
| **V. Testing and Quality** | ✅ PASS — Se agregan unit tests en `PlayerValuationCalculatorTest`, service tests en `SuperuserInitializationServiceTest`/`HoldingsValuationServiceTest`, integration tests con Testcontainers y E2E con MockMvc. Tests existentes NO se modifican excepto si rompen por contrato de fórmula — en ese caso se adaptan con permiso implícito en la spec. |
| **Idioma** | ✅ PASS — Documentación y mensajes en español. Identificadores de código en inglés/español sin tildes ni ñ. |
| **DoD** | 🔲 PENDING — Compilación exitosa, tests pasando, startup exitoso, Postman collection actualizado. |

**Post-Phase 1 re-check**: Sin violaciones adicionales detectadas en el diseño.

## Project Structure

### Documentation (this feature)

```text
specs/013-valuation-refactor/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
│   └── valuation-api.md
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
backend/
├── app/src/main/java/com/dappunq/
│   ├── model/valuation/
│   │   └── PlayerValuationCalculator.java   ← MODIFICAR: nueva fórmula
│   ├── service/
│   │   ├── HoldingsValuationService.java    ← MODIFICAR: leer valorBase antes de valuar
│   │   ├── WeeklyValuationService.java      ← MODIFICAR: leer valorBase antes de valuar
│   │   ├── SuperuserInitializationService.java ← MODIFICAR: orquestar flujo 1→2→3
│   │   └── InitialHoldingsService.java      ← LEER (sin cambios esperados)
│   └── config/
│       └── (startup hook — verificar cómo se llama initializeSuperuserStrategy al arrancar)
└── app/src/test/java/com/dappunq/
    ├── unit/
    │   └── PlayerValuationCalculatorTest.java  ← MODIFICAR: tests de nueva fórmula
    ├── service/
    │   ├── HoldingsValuationServiceTest.java   ← NUEVO o MODIFICAR
    │   └── SuperuserInitializationServiceTest.java ← MODIFICAR: flujo completo
    ├── integration/
    │   ├── ValuationPersistenceIntegrationTest.java ← MODIFICAR: verificar nueva fórmula
    │   └── InitialHoldingsIntegrationTest.java ← MODIFICAR: verificar flujo superusuario
    └── e2e/
        └── ValuationConfigurationE2ETest.java  ← MODIFICAR: verificar acumulación
```

**Structure Decision**: Single-project backend (Option 1 ajustado al monorepo). Los cambios son internos al módulo `app`; no se agregan módulos ni proyectos.

## Complexity Tracking

> No hay violaciones de Constitution. Sección omitida.
