# Implementation Plan: Protección del scraping con API key

**Branch**: `011-scraping-api-key` | **Date**: 2026-10-12 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `backend/specs/011-scraping-api-key/spec.md`

## Summary

Se protegerá `POST /scrape` mediante una API key compartida transportada en
`X-API-Key`, validada por su permiso `scraping` antes de invocar el servicio de
scraping. La credencial se persistirá como hash BCrypt en una fila singleton y
un comando de aplicación ejecutable por un wrapper PowerShell la rotará de forma
transaccional e imprimirá únicamente el nuevo valor cuando la persistencia haya
confirmado la operación.

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Spring Boot 4, Spring Security, Spring Data JPA,
PostgreSQL, BCrypt, Gradle

**Storage**: PostgreSQL mediante entidad/repository JPA; restricción única para
el permiso `scraping`

**Testing**: JUnit 5, Mockito, MockMvc y Testcontainers con PostgreSQL real

**Target Platform**: Backend Spring Boot ejecutado localmente o como JAR en un
entorno con PostgreSQL

**Project Type**: Servicio web con comando operativo de administración

**Performance Goals**: La validación debe ocurrir antes del scraping, con una
consulta de credencial por solicitud; no se introduce un objetivo de throughput
nuevo para esta feature

**Constraints**: No almacenar la clave en texto plano; no registrar su valor;
rechazar sin ejecutar scraping; mantener como máximo una credencial activa;
rotar dentro de una transacción sin dejar inutilizada la credencial anterior si
la nueva no puede persistirse; mantener separadas las capas de modelo,
persistencia, servicio y controlador

**Scale/Scope**: Una credencial compartida para todos los consumidores
autorizados; un endpoint protegido, un permiso (`scraping`) y un comando de
rotación

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Monorepo**: PASS. Solo se modifica el backend dentro del monorepo.
- **Layered Architecture**: PASS. La entidad/repository encapsulan persistencia,
  el servicio coordina rotación y validación, y el filtro/controlador manejan
  HTTP sin mover reglas de persistencia al modelo.
- **Rich Model**: PASS. Las invariantes de la credencial (permiso y formato)
  quedan en objetos de dominio/servicio sin anotaciones de persistencia.
- **Layered Validation**: PASS. El filtro valida transporte y autorización; el
  servicio valida existencia, unicidad y rotación; la persistencia aplica
  restricciones.
- **Testing and Quality**: PASS. Se planifican unit tests, integración con
  Testcontainers y E2E MockMvc, además de actualizar el contrato OpenAPI.
- **Idioma y nombres**: PASS. Documentación y errores en español; identificadores
  sin acentos ni `ñ`.

## Project Structure

### Documentation (this feature)

```text
backend/specs/011-scraping-api-key/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── scrape.md
│   └── rotate-scraping-api-key.md
└── tasks.md              # generado por /speckit-tasks
```

### Source Code

```text
backend/
├── app/
│   ├── src/main/java/com/dappunq/
│   │   ├── model/                    # invariantes de la API key
│   │   ├── persistence/              # entidad y repository JPA
│   │   ├── service/                  # validación y rotación
│   │   ├── security/                 # filtro X-API-Key y entry point
│   │   └── controller/               # POST /scrape
│   ├── src/main/resources/
│   └── src/test/java/com/dappunq/
│       ├── unit/
│       ├── integration/
│       └── e2e/
└── scripts/
    └── rotate-scraping-api-key.ps1
```

**Structure Decision**: Se conserva la estructura Gradle existente de
`backend/app`. La lógica se agrega a los paquetes Java ya utilizados y el
script operativo se ubica en `backend/scripts`; no se crea un segundo servicio
ni un endpoint HTTP administrativo para rotar secretos.

## Complexity Tracking

No hay violaciones de la constitución que requieran justificación.
