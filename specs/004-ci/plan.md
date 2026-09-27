# Plan de implementacion: CI del backend

**Rama**: `004-ci` | **Fecha**: 2026-09-26 | **Especificacion**: [spec.md](spec.md)

## Resumen

Crear un workflow de GitHub Actions que valide el backend Gradle con Java 25 en pushes a `main` y pull requests dirigidos a `main`. Los filtros incluyen cambios en `backend/**` y en el propio workflow, para ignorar cambios exclusivos del frontend. El job usa el Gradle Wrapper, compila produccion y pruebas antes de ejecutar la suite completa con Testcontainers y publica los resultados JUnit XML incluso tras fallos.

## Contexto tecnico

**Lenguaje/Version**: Java 25 (toolchain configurado en `backend/app/build.gradle`).

**Dependencias principales**: Gradle Wrapper 9.1.0, Spring Boot 4.0.0, `actions/checkout@v4`, `actions/setup-java@v4` (Temurin, cache Gradle), `dorny/test-reporter@v1` y `actions/upload-artifact@v4`.

**Persistencia**: PostgreSQL efimero levantado por Testcontainers sobre Docker; no se requiere un servicio PostgreSQL en el workflow.

**Pruebas**: JUnit Platform mediante `./gradlew test`. El subproyecto `app` contiene pruebas unitarias, de integracion y E2E, y genera resultados JUnit XML en `backend/app/build/test-results/test/`.

**Plataforma objetivo**: `ubuntu-latest` de GitHub-hosted runners, con daemon Docker para Testcontainers.

**Tipo de proyecto**: Monorepo web con backend Java y frontend independiente; el cambio se limita a CI y no altera codigo de producto.

**Objetivos de rendimiento**: No aplica un SLA de ejecucion para este workflow. La cache Gradle reduce descargas repetidas sin afectar la validez de la compilacion.

**Restricciones**: Ejecutar comandos desde `./backend`; usar los eventos `push` y `pull_request` restringidos a `main`; activar el workflow para cambios bajo `backend/` o en `.github/workflows/ci-backend.yml`; fallar ante errores de compilacion o pruebas. El wrapper esta actualmente registrado como modo Git `100644` y debe quedar ejecutable (`100755`) para invocar `./gradlew` en Linux.

**Alcance**: Un workflow `CI Backend` y un job `build-and-test`, con pasos ordenados de checkout, configuracion Java, compilacion, pruebas y publicacion de resultados.

## Verificacion de la constitucion

**Antes del diseno**:

- **I. Monorepo**: Cumple. El workflow es especifico del backend y no acopla el frontend.
- **II. Arquitectura en capas / III. Modelo rico / IV. Validacion por niveles**: Cumple. No se modifica codigo de aplicacion ni reglas del dominio.
- **V. Testing y calidad**: Cumple. Ejecuta las suites existentes, conserva Testcontainers contra PostgreSQL real y no elimina ni modifica pruebas.
- **Entregables y Definition of Done**: Cumple para esta funcionalidad de CI, cuyo objetivo es automatizar la compilacion y las pruebas ya configuradas.

**Despues del diseno**: Sin cambios. El workflow conserva la suite completa del subproyecto `app`; no agrega servicios de base de datos ni modifica la estructura de capas. La ejecucion de CI no sustituye la validacion local ni la actualizacion de Postman requerida al incorporar endpoints.

**Resultado de los gates**: Aprobado; no hay violaciones que justificar.

## Estructura del proyecto

### Documentacion de esta funcionalidad

```text
specs/004-ci/
├── plan.md
├── research.md
├── data-model.md
└── quickstart.md
```

No se crea `contracts/`: este cambio es infraestructura interna y no expone una API, CLI ni otro contrato externo.

### Codigo y configuracion relevantes

```text
.github/
└── workflows/
    └── ci-backend.yml

backend/
├── gradlew                    # Debe registrarse con modo ejecutable 100755
├── gradle/wrapper/
├── build.gradle
├── settings.gradle            # Incluye el subproyecto app
└── app/
    ├── build.gradle           # Java 25 y JUnit Platform
    └── src/test/java/com/dappunq/
        ├── unit/
        ├── integration/
        └── e2e/
```

**Decision estructural**: Mantener el workflow en `.github/workflows/` en la raiz del monorepo; establecer `defaults.run.working-directory: ./backend` para todos los comandos; mantener rutas de informes relativas al workspace del runner. Usar los comandos `./gradlew clean compileTestJava` y `./gradlew test` en pasos separados, sin `continue-on-error`, de modo que un fallo de compilacion detenga la ejecucion de pruebas y un fallo de pruebas determine el resultado del job. Ejecutar el publicador con `if: always()` y `fail-on-empty: false`; conservar XML disponibles con `actions/upload-artifact@v4` tambien con `if: always()` y `if-no-files-found: ignore`. Ante un PR desde un fork, omitir la creacion del Check Run (token de solo lectura) pero publicar los XML como artefacto. La ausencia de XML no cambia un fallo anterior ni genera un resultado exitoso.

**Permisos de informes**: `dorny/test-reporter` crea un Check Run y requiere `checks: write` ademas de permisos de lectura. En pull requests desde forks, GitHub limita el token a solo lectura; condicionar el Check Run a ejecuciones con permisos de escritura y cargar siempre los XML como artefacto evita ampliar el contexto del token o usar `pull_request_target` para ejecutar codigo no confiable.

## Registro de complejidad

No se identifican desviaciones de la constitucion ni componentes adicionales que requieran justificacion.
