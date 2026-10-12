# Implementation Plan: Documentacion interactiva y contrato de API

**Branch**: `005-swagger` | **Date**: 2026-09-26 | **Spec**: `/specs/005-swagger/spec.md`

**Input**: Feature specification from `/specs/005-swagger/spec.md`

## Summary

Se publicara una interfaz Swagger UI en espanol para explorar y probar las cinco operaciones REST existentes. El contrato OpenAPI 3.0.3, versionado como `1.0.0`, sera la unica fuente de definiciones publicadas: vivira en `contracts/openapi.yaml` y Gradle lo empaquetara como `/openapi.yaml`, que Swagger UI cargara en `/swagger-ui.html`. Se usara `springdoc-openapi-starter-webmvc-ui:3.1.1`, compatible con Spring Boot 4; el contrato no se duplicara en anotaciones de operacion ni en un bean OpenAPI.

La interfaz incluira una personalizacion localizada y accesible de Swagger UI que avise antes de ejecutar `POST /register`. Se documentara el esquema bearer JWT para facilitar exploracion, pero las operaciones actuales se declararan publicas: `SecurityConfig` permite las cinco rutas y el alcance de esta funcionalidad no cambia la autorizacion del backend.

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Spring Boot 4.0.0, Spring MVC, Spring Security; `springdoc-openapi-starter-webmvc-ui:3.1.1`; Gradle Wrapper 9.1.0

**Storage**: PostgreSQL existente; la documentacion y el contrato son recursos estaticos versionados y no requieren almacenamiento nuevo

**Testing**: JUnit 5, Spring Boot Test, MockMvc y Testcontainers para pruebas del comportamiento API; prueba de contrato para validar el YAML, comparar rutas documentadas con mappings MVC y verificar seguridad; lint OpenAPI en CI

**Target Platform**: Servicio web JVM local y CI; interfaz servida desde el backend

**Project Type**: Backend REST dentro del monorepo existente (`../../app`)

**Performance Goals**: UI y contrato disponibles sin consultas a base de datos; ninguna regresion medible en latencia de las operaciones API actuales

**Constraints**: Mantener la arquitectura por capas y la seguridad existente; descripciones del contrato y textos personalizados en espanol; no incluir credenciales persistentes; Java 25 requiere Gradle 9.1.0, ya configurado; la UI debe cargar el contrato canonico y no uno autogenerado divergente

**Scale/Scope**: Cinco operaciones actuales: `POST /login`, `POST /register`, `GET /users/{id}/`, `GET /players`, `GET /players/{id}`; los contratos historicos `001`, `002` y `003` se reconciliaran con los mappings, DTO y respuestas actuales

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Pass: El cambio permanece en el backend del monorepo; no mezcla responsabilidades con el frontend de producto.
- Pass: OpenAPI y la interfaz documentan el comportamiento existente sin mover reglas de negocio a controllers/services ni modificar autorizacion.
- Pass: No se agregan entidades ni persistencia para la documentacion.
- Pass: Las validaciones de contrato y API se integran a las pruebas existentes con JUnit 5/MockMvc; las pruebas de persistencia existentes mantienen Testcontainers.
- Pass: Gradle y Java 25 siguen el stack vigente. Se fija Springdoc 3.x, ya que la linea 2.x corresponde a Spring Boot 3.
- Pass with scoped customization: Swagger UI no ofrece configuracion soportada para localizar todo su chrome ni confirmacion por operacion. Se mantendra la UI base de Springdoc, con plugin/recursos de inicializacion versionados y pruebas de teclado/lector de pantalla para esos dos requisitos. La API de plugins de Swagger UI no es estable; se fijara y probara la version de UI asociada al starter.
- Pass: Se evitan definiciones de OpenAPI duplicadas en Java. Los tags, respuestas, esquemas y seguridad publicados se mantienen en el YAML canonico.
- Pass: Las cinco operaciones hoy son publicas segun `SecurityConfig`; no se asigna `bearerAuth` como requisito de operacion ni se marca `GET /users/{id}/` como protegido. Cambiar esa politica requeriria una especificacion y cambio de backend separados.

## Design Decisions

- **Contrato canonico**: `contracts/openapi.yaml`; OpenAPI 3.0.3 y version `1.0.0`. La tarea Gradle de recursos lo empaquetara en `static/openapi.yaml`, sin mantener una segunda copia fuente.
- **UI y rutas**: Springdoc WebMVC UI en `/swagger-ui.html`, apuntando explicitamente a `/openapi.yaml`. Se configurara la ruta de API Docs solo si es necesaria para la configuracion de Swagger UI; el documento autogenerado no sera la fuente que la interfaz presente.
- **Metadatos y seguridad**: titulo `SDD Project API`, version `1.0.0`, descripcion en espanol y esquema HTTP bearer con formato `JWT`. El esquema estara disponible en el boton Authorize; ninguna operacion actual lo requiere.
- **Operaciones y grupos**: el YAML sera propietario de las etiquetas `Autenticacion`, `Usuarios` y `Jugadores`, de las cinco rutas y de sus schemas. Los controladores permanecen como fuente de mappings de Spring MVC y no recibiran descripciones OpenAPI paralelas.
- **Contrato observable**: documentar las formas distintas de error que ya existen (`message` para autenticacion/usuarios y `mensaje` para jugadores), los tipos/nullabilidad de DTO, el `Authorization` de respuesta de login y codigos aplicables. No normalizar ni cambiar respuestas como parte de esta funcionalidad.
- **Pruebas desde UI**: `POST /register` se marcara como operación con efectos y exigira confirmacion en espanol antes de enviar. La confirmacion sera accesible por teclado y tecnologias de asistencia; no reemplaza controles de servidor.
- **Calidad y versionado**: CI validara sintaxis/reglas del contrato y paridad de rutas y seguridad con la aplicacion. Al ser el primer contrato consolidado de la API vigente, `1.0.0` establece la linea base; futuras roturas deben incrementar explicitamente la version mayor.
- **Seguridad de publicacion**: permitir acceso anonimo a Swagger UI, sus recursos necesarios y `/openapi.yaml`, sin ampliar acceso a rutas de negocio ni publicar tokens de ejemplo.

## Project Structure

### Documentation (this feature)

```text
specs/005-swagger/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── openapi.yaml
└── tasks.md              # Se genera en la fase de tareas, no en esta planificacion
```

### Source Code (repository root)

```text
backend/
├── app/
│   ├── build.gradle                     # Springdoc, recursos del contrato y pruebas de contrato
│   └── src/
│       ├── main/
│       │   ├── java/com/dappunq/
│       │   │   ├── controller/          # Mappings actuales; sin contrato duplicado por anotaciones
│       │   │   └── security/            # Permisos para UI/recursos de documentacion
│       │   └── resources/
│       │       ├── application.properties
│       │       └── static/              # OpenAPI empaquetado y personalizacion UI localizada
│       └── test/java/com/dappunq/
│           └── e2e/                     # Pruebas UI/contrato y endpoints con MockMvc
└── gradlew

.github/workflows/ci-backend.yml         # Lint y ejecucion por cambios en contrato
```

**Structure Decision**: La funcionalidad se sirve desde el backend Spring existente. El YAML versionado dentro de los artefactos de la feature se copia al classpath durante `processResources`; la copia empaquetada es un artefacto generado, no una definicion mantenida aparte. La personalizacion del UI se limita a recursos estaticos y plugin compatible con el Swagger UI fijado.

## Complexity Tracking

No se identifican violaciones constitucionales que requieran excepcion. La personalizacion UI es necesaria para cumplir localizacion y confirmacion previas a operaciones con efectos, capacidades que Swagger UI no proporciona mediante configuracion declarativa.
