# Modelo de datos: CI del backend

El workflow no introduce almacenamiento de aplicacion. Las entidades siguientes describen la informacion temporal que GitHub Actions procesa durante cada ejecucion.

## Ejecucion de CI

Representa una validacion iniciada por un evento de GitHub.

| Campo | Tipo conceptual | Regla |
|---|---|---|
| evento | `push` o `pull_request` | Solo estos eventos inician este workflow. |
| rama | referencia Git | `main` para push; destino `main` para pull request. |
| archivos modificados | conjunto de rutas | Debe incluir `backend/**` o `.github/workflows/ci-backend.yml`. |
| estado | `queued`, `in_progress`, `success` o `failure` | Un fallo de compilacion o prueba conduce a `failure`; un reporte no puede convertirlo en `success`. |
| etapas | lista ordenada | Checkout, Java, compilacion, pruebas y publicacion de resultados. |

### Transiciones

`queued` -> `in_progress` -> `success` o `failure`.

Un error en checkout o configuracion tambien produce `failure`. Si falla la compilacion, las pruebas se omiten y se intenta la publicacion; si falla la suite, se intenta la publicacion y la ejecucion queda en fallo.

## Informe de pruebas

Resultado XML JUnit generado por Gradle para la tarea `test`.

| Campo | Tipo conceptual | Regla |
|---|---|---|
| ruta | ruta relativa al workspace | `backend/app/build/test-results/test/*.xml`. |
| formato | identificador | JUnit XML (`java-junit` para el reportero). |
| casos | conjunto de resultados | Puede incluir pruebas aprobadas, fallidas y omitidas. |
| disponibilidad | booleano | Puede ser falso si falla la compilacion antes de ejecutar las pruebas. |

La publicacion se intenta aunque el informe no exista. La ausencia de informe no elimina ni reemplaza el estado fallido que haya producido una etapa anterior.

## Cambio del backend

Conjunto de archivos modificados que se evalua para decidir si se inicia el workflow.

| Campo | Tipo conceptual | Regla |
|---|---|---|
| evento | evento GitHub | `push` o `pull_request`. |
| destino | rama Git | Solo se considera `main`. |
| rutas | conjunto de rutas | Backend o archivo del workflow para activar; cambios solo de frontend no activan. |

Las condiciones de rama y rutas se aplican conjuntamente para cada tipo de evento.
