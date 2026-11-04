# Implementation Plan: Mercado de órdenes de tokens

**Branch**: `014-token-market` | **Date**: 2026-11-04 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/014-token-market/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

Agregar un mercado de tokens con órdenes de compra y venta, bloqueo de saldos,
coincidencia y liquidación atómica, ejecución prioritaria contra el Superusuario
y consultas separadas para órdenes pendientes e historial ejecutado. La solución
incorporará un modelo de dominio de órdenes, entidades JPA separadas, servicios
transaccionales y cuatro endpoints REST públicos. Strategy y Holding son
capacidades existentes que se reutilizan: Strategy habilita la operación y
determina la valuación de tokens nuevos; Holding recibe las transferencias.

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Spring Boot 4, Spring Web, Spring Data JPA, Jakarta Validation; Strategy y Holding existentes

**Storage**: PostgreSQL mediante Hibernate/JPA; Testcontainers para integración

**Testing**: JUnit 5, Mockito, Testcontainers PostgreSQL y MockMvc

**Target Platform**: Servicio REST backend ejecutado sobre Linux/Java

**Project Type**: Web service REST dentro de un monorepo backend/frontend

**Performance Goals**: No se fija un throughput nuevo; las operaciones de creación y coincidencia deben ser transacciones cortas y serializables por activo/orden

**Constraints**: Importes con máximo dos decimales, cantidades enteras, órdenes completas sin ejecuciones parciales, bloqueo consistente y liquidación atómica. No se agregará JWT ni API key a estos endpoints.

**Scale/Scope**: Usuarios, jugadores, tenencias y saldos existentes; nuevas órdenes de compra/venta, historial y órdenes pendientes

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio | Evaluación |
|-----------|-----------|
| **I. Monorepo** | ✅ PASS — El cambio queda dentro de `backend/app` y sus artefactos en `backend/specs`. |
| **II. Layered Architecture** | ✅ PASS — Controller mapea DTOs, Service coordina casos de uso, Model contiene invariantes y Repository mapea entidades JPA; Strategy/Holding se consumen mediante sus servicios/repositorios existentes. |
| **III. Rich Model** | ✅ PASS — `Orden` y objetos de dinero/bloqueo serán POJOs sin anotaciones de persistencia; no se duplican Strategy ni Holding. |
| **IV. Layered Validation** | ✅ PASS — DTO valida formato, Service resuelve usuarios/jugadores y disponibilidad, Model valida estados y rangos. |
| **V. Testing and Quality** | ✅ PASS — Se prevén unit tests de dominio, integración PostgreSQL con Testcontainers, E2E MockMvc sin autenticación y actualización de Postman. |
| **Idioma y DoD** | ✅ PASS — Documentación/mensajes en español, identificadores sin tildes/ñ; se exige compilación, tests, startup y colección Postman. |

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
backend/
├── app/src/main/java/com/dappunq/
│   ├── model/order/                  # Orden, estados y reglas de coincidencia
│   ├── persistence/                  # entidades/repositorios/mappers JPA
│   ├── service/                      # creación, matching, liquidación y consultas
│   ├── dto/order/                    # requests y responses REST
│   └── controller/                   # endpoints /orders y /users/{id}
├── app/src/test/java/com/dappunq/
│   ├── unit/                         # invariantes y transición de Orden
│   ├── integration/                  # PostgreSQL/Testcontainers
│   └── e2e/                          # MockMvc, exclusivamente endpoints
├── postman/token-market.postman_collection.json
└── specs/014-token-market/
```

**Structure Decision**: Se mantiene el módulo backend existente y sus paquetes
por capa. Las entidades de persistencia serán distintas de los modelos de
dominio. Strategy y Holding no se recrean: el servicio de órdenes integra sus
APIs existentes. Como no hay autenticación en este alcance, el request de
creación incluye `usuarioId` y el Service lo valida como propietario de la
orden; los GET usan el `id` de la ruta para filtrar resultados propios.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | No hay violaciones de la Constitución. |

## Re-evaluación post-diseño

✅ PASS — El diseño conserva las capas, reutiliza Strategy y Holding, centraliza
la lógica de estado en el modelo, usa transacciones para evitar liquidaciones
parciales, mantiene los tests E2E en `e2e`, no añade JWT/API key y agrega la
colección Postman requerida. No quedan aclaraciones técnicas pendientes.
