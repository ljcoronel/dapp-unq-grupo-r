# Implementation Plan: Protección JWT de órdenes

**Branch**: `015-jwt-order-protection` | **Date**: 2026-11-04 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `backend/specs/015-jwt-order-protection/spec.md`

## Summary

Proteger `POST /orders/buy` y `POST /orders/sell` con la autenticación JWT stateless
existente, derivando la identidad de la orden desde el usuario autenticado y
conservando las validaciones y reglas actuales. El contrato OpenAPI canónico se
ampliará con el esquema Bearer JWT, la seguridad de ambos endpoints y sus respuestas
401; el recurso estático se regenerará mediante la tarea Gradle existente.

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Spring Boot 4.0.0, Spring Security, JJWT 0.12.7,
Spring Web MVC, Jakarta Validation

**Storage**: PostgreSQL mediante Spring Data JPA; las órdenes relacionan su propietario
con `UserEntity`.

**Testing**: JUnit 5, MockMvc con `springSecurity()`, Mockito y Testcontainers
PostgreSQL. Las pruebas end-to-end permanecen en `com.dappunq.e2e`.

**Target Platform**: Servicio web Spring Boot ejecutado en JVM con PostgreSQL.

**Project Type**: Backend web service dentro de un monorepo con frontend React/Vite.

**Performance Goals**: Mantener el coste del filtro JWT y la carga del usuario dentro
del flujo actual; no se introduce una consulta adicional de orden ni una operación de
persistencia para solicitudes rechazadas.

**Constraints**: La autenticación debe ejecutarse antes de la lógica de órdenes; toda
solicitud sin JWT, con JWT inválido/vencido o usuario inexistente responde 401 sin
efectos. El campo `usuarioId` del body se conserva por compatibilidad, pero no es una
fuente confiable de identidad.

**Scale/Scope**: Dos operaciones de creación de órdenes, su contrato OpenAPI y las
pruebas E2E asociadas; los demás endpoints no cambian de política.

## Constitution Check

*GATE: PASS*. La solución mantiene el monorepo y las capas existentes: el filtro y
`SecurityConfig` autentican, el controller obtiene la identidad y transforma la entrada,
los servicios siguen coordinando la creación y el modelo conserva las reglas de dominio.
No se agregan entidades de persistencia ni lógica de negocio al modelo. Las pruebas se
ubican en el paquete E2E existente y usan MockMvc con PostgreSQL Testcontainers. El
contrato y las descripciones permanecen en español y no se requieren excepciones a la
constitución.

## Project Structure

```text
backend/
├── app/
│   ├── src/main/java/com/dappunq/
│   │   ├── controller/                 # autenticación de buy/sell e identidad
│   │   ├── security/                   # filtro, principal y configuración JWT
│   │   ├── service/order/              # reglas existentes de creación
│   │   └── dto/order/                  # request/response existentes
│   ├── src/main/resources/static/      # openapi.yaml generado
│   └── src/test/java/com/dappunq/e2e/  # escenarios MockMvc con JWT
├── specs/005-swagger/contracts/        # fuente canónica OpenAPI
└── specs/015-jwt-order-protection/     # artefactos de esta feature
frontend/
└── src/
```

**Structure Decision**: Se selecciona la estructura de aplicación web del monorepo.
La implementación se limita a `backend/app` y al contrato fuente en
`backend/specs/005-swagger`; `processResources` mantiene sincronizado el
`app/src/main/resources/static/openapi.yaml`. Los artefactos de diseño viven junto a
la especificación `015`.

## Phase 0: Research

Las decisiones y alternativas están documentadas en [research.md](./research.md).
No quedan incógnitas técnicas abiertas.

## Phase 1: Design

- [data-model.md](./data-model.md) define la identidad autenticada, la orden y las
  invariantes de atribución.
- [contracts/orders-openapi.yaml](./contracts/orders-openapi.yaml) define el delta
  contractual de los dos endpoints protegidos.
- [quickstart.md](./quickstart.md) define la validación reproducible de seguridad,
  atribución y contrato.

## Implementation sequencing

1. Exponer el identificador del usuario en `UserPrincipal` y actualizar el controller
   para recibir la autenticación validada, verificar la coherencia del `usuarioId`
   recibido y pasar a los servicios una solicitud atribuida al principal.
2. Cambiar las reglas de `SecurityConfig` para exigir autenticación únicamente en
   `POST /orders/buy` y `POST /orders/sell`, manteniendo el filtro JWT y el handler
   401 uniforme sin revelar datos de negocio.
3. Actualizar las pruebas E2E existentes: ausencia, formato inválido, token vencido,
   usuario inexistente, JWT válido, mismatch de identidad y conservación de errores
   funcionales; comprobar que no se persisten órdenes ni reservas en rechazos.
4. Añadir ambos paths y sus esquemas/respuestas al contrato canónico OpenAPI y
   ejecutar la tarea que copia el contrato al recurso estático.
5. Ejecutar validación focalizada y luego `:app:test`, verificando compilación,
   pruebas y sincronización del contrato.

## Post-design Constitution Check

*GATE: PASS*. El diseño no mezcla capas: la autenticación pertenece a Security,
la extracción/adaptación de identidad al Controller y la creación permanece en los
Services/Model existentes. Se preservan validaciones de DTO, validaciones de existencia
en Service y reglas de dominio. Los escenarios E2E usan MockMvc en su paquete dedicado,
la persistencia de pruebas usa PostgreSQL Testcontainers y el contrato fuente se
actualiza junto con el recurso servido.

## Complexity Tracking

No hay violaciones de la constitución ni complejidad adicional que justificar.
