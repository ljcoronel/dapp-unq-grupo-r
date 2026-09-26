# Investigacion: CI del backend

## Decision: Gestor de compilacion y wrapper

- **Decision**: Usar Gradle Wrapper 9.1.0, desde `backend/`, con `clean compileTestJava` seguido de `test`.
- **Rationale**: `backend/settings.gradle` declara el subproyecto `app`; `backend/app/build.gradle` configura Java 25 y `useJUnitPlatform()`. La suite ya contiene paquetes separados para pruebas unitarias, de integracion y E2E. Los comandos solicitados compilan antes de probar y conservan el fail-fast de Gradle.
- **Alternatives considered**: Maven no corresponde al backend real; instalar Gradle globalmente haria depender el resultado de una version ajena al wrapper. Invocar `bash gradlew` evita el bit ejecutable, pero se prefiere corregir el modo versionado del wrapper.
- **Hallazgo de preparacion**: Git registra `backend/gradlew` como `100644`; para que `./gradlew` funcione en Ubuntu, hay que cambiar el modo versionado a `100755`.

## Decision: Version y cache de Java

- **Decision**: Configurar `actions/setup-java@v4` con `distribution: temurin`, `java-version: '25'` y `cache: gradle`.
- **Rationale**: El build declara Java 25 como toolchain y `setup-java` gestiona JDK y cache Gradle. La version solicitada de action se conserva conforme a la especificacion.
- **Alternatives considered**: Maven cache no aplica. Usar Java preinstalado en el runner no garantiza la version requerida. La documentacion actual puede mostrar versiones mayores de la action, pero el alcance especificado pide v4.
- **Referencia**: [actions/setup-java README](https://github.com/actions/setup-java/blob/main/README.md).

## Decision: Triggers y filtrado por rutas

- **Decision**: Definir por separado `push` y `pull_request`, ambos limitados a `main`; en cada evento incluir `backend/**` y `.github/workflows/ci-backend.yml` en `paths`.
- **Rationale**: GitHub exige que las condiciones de rama y ruta se cumplan simultaneamente. Incluir el propio workflow permite validar cambios de CI que no toquen el backend, mientras que los cambios solo del frontend quedan excluidos.
- **Alternatives considered**: Ejecutar en todas las ramas o sin filtro de rutas desperdicia ejecuciones y contradice el alcance; omitir la ruta del workflow impediria ejecutar CI al modificar solo su configuracion.
- **Referencia**: [Sintaxis de workflows de GitHub Actions](https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-syntax).

## Decision: Docker para Testcontainers

- **Decision**: Usar el daemon Docker disponible en el runner Ubuntu y no declarar un servicio PostgreSQL en el workflow.
- **Rationale**: El proyecto incorpora Testcontainers y PostgreSQL como contenedor de prueba; el runner GitHub-hosted ejecuta cada job en una VM Ubuntu con herramientas del runner mantenidas por GitHub. La configuracion de pruebas ya crea contenedores `postgres:16-alpine`.
- **Alternatives considered**: Un servicio PostgreSQL fijo duplicaria la gestion de ciclo de vida existente en Testcontainers y no probaria el mismo camino que las pruebas configuradas.
- **Referencias**: [GitHub-hosted runners](https://docs.github.com/en/actions/concepts/runners/github-hosted-runners), `backend/app/src/test/java/com/dappunq/config/TestcontainersConfig.java`.

## Decision: Publicacion JUnit XML y preservacion de estado

- **Decision**: Usar `dorny/test-reporter@v1`, `reporter: java-junit`, ruta `backend/app/build/test-results/test/*.xml` y condicion `if: always()` con `fail-on-empty: false`. Cargar los XML con `actions/upload-artifact@v4`, tambien con `if: always()` y `if-no-files-found: ignore`. Omitir el Check Run de dorny en PR desde forks, donde el token no tiene permiso de escritura, sin omitir la carga de artefactos.
- **Rationale**: Gradle genera XML JUnit y la action presenta sus resultados en GitHub. La ejecucion incondicional permite intentar publicar resultados tras fallos de prueba y tambien cuando una etapa previa impide generarlos. El artefacto permite consultar los XML en ejecuciones desde forks, que no pueden escribir Check Runs. Un fallo de compilacion o de pruebas conserva el estado fallido del job.
- **Alternatives considered**: Publicar unicamente logs no ofrece un reporte procesable; no subir artefactos deja los PR desde forks sin los resultados XML; tratar la ausencia de informes como exito ocultaria resultados faltantes, mientras que hacer fallar exclusivamente al publicador puede convertir un error de infraestructura de reporte en causa primaria.
- **Referencia**: [dorny/test-reporter README v1](https://github.com/dorny/test-reporter/blob/v1/README.md) y [action.yml v1](https://github.com/dorny/test-reporter/blob/v1/action.yml).
- **Seguridad y limitacion**: Los PR desde forks reciben un token de solo lectura, que no permite a `dorny/test-reporter` crear Check Runs aunque el workflow declare `checks: write`. El plan omite el Check Run en ese contexto y conserva los XML como artefacto. No elevar privilegios con `pull_request_target`.
