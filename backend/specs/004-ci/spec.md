# Especificación de la funcionalidad: CI del backend

**Rama de funcionalidad**: `004-ci`

**Fecha de creación**: 2026-09-26

**Estado**: Borrador

**Entrada**: Descripción del usuario: "Configurar un pipeline de integración continua aislado para el backend en GitHub Actions, con activación por cambios del backend o del propio workflow en main, compilación y ejecución de pruebas unitarias, de integración y E2E con Testcontainers, y publicación de resultados incluso cuando haya fallos."

## Escenarios de usuario y pruebas

### Historia de usuario 1 - Validar cambios del backend (Prioridad: P1)

Como colaborador del proyecto, quiero que los cambios del backend se compilen y prueben automáticamente para detectar regresiones antes de integrarlos en `main`.

**Por qué esta prioridad**: La compilación y las pruebas son las comprobaciones esenciales para impedir que cambios defectuosos del backend se integren.

**Prueba independiente**: Crear un cambio que afecte a `../..` dirigido a `main` y comprobar que el workflow ejecuta la compilación y la suite de pruebas en un runner compatible con Docker.

**Escenarios de aceptación**:

1. **Dado** un push a `main` que modifica archivos de `../..`, **cuando** se procesa el cambio, **entonces** se inicia la validación del backend.
2. **Dado** un pull request dirigido a `main` que modifica archivos de `../..`, **cuando** se procesa el pull request, **entonces** se inicia la misma validación.
3. **Dado** un cambio que modifica solamente archivos del frontend, **cuando** se procesa el cambio, **entonces** el workflow del backend no se inicia.
4. **Dado** un cambio en el archivo del workflow de CI del backend, **cuando** se procesa en `main` o en un pull request dirigido a `main`, **entonces** se inicia la validación del backend.

### Historia de usuario 2 - Bloquear cambios que no superan las comprobaciones (Prioridad: P1)

Como responsable de mantenimiento, quiero que los errores de compilación o las pruebas fallidas produzcan un resultado fallido para evitar integrar cambios que no cumplen el nivel de calidad acordado.

**Por qué esta prioridad**: Un resultado exitoso solo es útil si refleja que tanto la compilación como todas las pruebas requeridas finalizaron correctamente.

**Prueba independiente**: Introducir por separado un error de compilación y una prueba fallida, y comprobar que cada caso produce un resultado fallido en la ejecución.

**Escenarios de aceptación**:

1. **Dado** que falla la compilación, **cuando** se ejecuta el pipeline, **entonces** la ejecución falla y no continúa a la etapa de pruebas.
2. **Dado** que la compilación termina correctamente y falla al menos una prueba, **cuando** se ejecuta la suite, **entonces** el pipeline termina con resultado fallido.
3. **Dado** que la compilación y todas las pruebas terminan correctamente, **cuando** finaliza el pipeline, **entonces** el resultado es exitoso.

### Historia de usuario 3 - Consultar los resultados de las pruebas (Prioridad: P2)

Como colaborador, quiero consultar los resultados detallados de las pruebas aunque alguna comprobación falle para identificar con rapidez qué necesita corrección.

**Por qué esta prioridad**: Los resultados disponibles en ejecuciones fallidas reducen el tiempo necesario para diagnosticar problemas.

**Prueba independiente**: Provocar una prueba fallida y comprobar que el paso de publicación de resultados se ejecuta igualmente y muestra los informes disponibles.

**Escenarios de aceptación**:

1. **Dado** que las pruebas producen informes y terminan correctamente, **cuando** finaliza la ejecución, **entonces** se publican los resultados.
2. **Dado** que las pruebas producen informes y al menos una falla, **cuando** finaliza la ejecución, **entonces** el paso de publicación se ejecuta igualmente y presenta los resultados.
3. **Dado** que falla una etapa anterior a las pruebas y no existen informes de pruebas, **cuando** termina la ejecución, **entonces** el paso de publicación se ejecuta igualmente y el resultado fallido de la etapa anterior se conserva.

### Casos límite

- Un cambio que incluye archivos del backend y del frontend inicia la CI del backend por el cambio en `../..`.
- Un cambio exclusivo del frontend no inicia la CI del backend.
- Un cambio al propio archivo del workflow inicia la CI aunque no cambien archivos del backend.
- La falta de informes por una falla de compilación no debe cambiar el resultado fallido de la ejecución ni impedir que el paso de publicación se ejecute.
- Las pruebas de integración que requieran PostgreSQL deben poder iniciar su base de datos de prueba sin servicios de base de datos declarados en el workflow.

## Requisitos

### Requisitos funcionales

- **RF-001**: El sistema DEBE definir el workflow `CI Backend` en `../../../.github/workflows/ci-backend.yml`.
- **RF-002**: El workflow DEBE ejecutarse ante pushes a la rama `main` cuando el cambio incluya archivos bajo `../..` o el propio archivo `.github/workflows/ci-backend.yml`.
- **RF-003**: El workflow DEBE ejecutarse ante pull requests dirigidos a `main` cuando el cambio incluya archivos bajo `../..` o el propio archivo `.github/workflows/ci-backend.yml`.
- **RF-004**: Los cambios exclusivos del frontend NO DEBEN iniciar este workflow.
- **RF-005**: El job de validación DEBE ejecutarse en `ubuntu-latest`, con Docker disponible para las pruebas que utilizan Testcontainers.
- **RF-006**: Todos los comandos de ejecución (`run`) del job DEBEN ejecutarse desde el directorio `./backend`.
- **RF-007**: El pipeline DEBE obtener el código mediante `actions/checkout@v4` y configurar Temurin Java 25 mediante `actions/setup-java@v4`.
- **RF-008**: La configuración del entorno DEBE habilitar la caché de Gradle, de acuerdo con el gestor de compilación que utiliza el backend.
- **RF-009**: Antes de ejecutar las pruebas, el pipeline DEBE compilar el código de producción y de pruebas con una operación equivalente a `./gradlew clean compileTestJava`.
- **RF-010**: Si falla la compilación, el pipeline DEBE fallar y detener la secuencia antes de ejecutar las pruebas.
- **RF-011**: Si la compilación termina correctamente, el pipeline DEBE ejecutar la suite completa mediante `./gradlew test`, incluyendo las pruebas unitarias, de integración y E2E configuradas por el backend.
- **RF-012**: Las pruebas que utilicen Testcontainers DEBEN poder iniciar PostgreSQL usando el daemon Docker del runner; el workflow NO DEBE requerir un servicio PostgreSQL declarado para este propósito.
- **RF-013**: El pipeline DEBE fallar cuando una o más pruebas fallen.
- **RF-014**: El pipeline DEBE publicar los informes XML de pruebas generados por Gradle, ubicados bajo `backend/app/build/test-results/test/*.xml`.
- **RF-015**: El paso de publicación de resultados DEBE ejecutarse siempre, aunque falle una etapa previa o una o más pruebas.
- **RF-016**: El paso de publicación NO DEBE ocultar ni convertir en exitoso un fallo de compilación o de pruebas.
- **RF-017**: El job DEBE identificarse como `build-and-test` y seguir este orden: checkout, configuración de Java, compilación, pruebas y publicación de informes.

### Entidades principales

- **Ejecución de CI**: Validación asociada a un push o pull request que registra su resultado y las etapas ejecutadas.
- **Informe de pruebas**: Resultado legible de las pruebas ejecutadas, incluyendo los casos aprobados y fallidos disponibles para la ejecución.
- **Cambio del backend**: Modificación del repositorio que, al cumplir las condiciones de rama y rutas, inicia la validación.

## Criterios de éxito

### Resultados medibles

- **CS-001**: El 100% de los pushes a `main` y pull requests dirigidos a `main` que modifican `../..` o el workflow del backend inician la validación.
- **CS-002**: El 100% de los cambios exclusivos del frontend dejan sin iniciar el workflow del backend.
- **CS-003**: El 100% de las ejecuciones con errores de compilación o pruebas fallidas terminan con un resultado fallido.
- **CS-004**: En el 100% de las ejecuciones, el paso de publicación se intenta incluso si una etapa previa falla.
- **CS-005**: En el 100% de las ejecuciones con informes XML disponibles, estos resultados quedan publicados para su consulta.
- **CS-006**: Las pruebas que requieren PostgreSQL pueden ejecutarse en el runner sin configuración manual de servicios de base de datos en el workflow.

## Supuestos

- El backend utiliza Gradle con wrapper y el subproyecto `app`; por ello, se usan los comandos Gradle equivalentes a los objetivos Maven indicados en la descripción y el directorio de informes de Gradle.
- La suite `test` del backend reúne las pruebas unitarias, de integración y E2E que deben formar parte de CI.
- Testcontainers y la configuración existente del proyecto son responsables de iniciar y detener PostgreSQL durante las pruebas.
- La publicación de resultados usa un reportero compatible con informes JUnit XML de Gradle; `dorny/test-reporter@v1` es una opción recomendada.
- La ejecución debe conservar el resultado de fallo de compilación o pruebas incluso si la publicación de informes también encuentra problemas.
