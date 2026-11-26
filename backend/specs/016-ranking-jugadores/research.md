# Research

## Decision: consulta unica PostgreSQL

- **Decision**: usar un Adapter JDBC con CTEs y `ROW_NUMBER()` para filtros, consolidacion, orden y paginacion.
- **Rationale**: evita cargar catalogos y cotizaciones completas en memoria, elimina duplicados en la consulta y conserva el mismo criterio en conteo y pagina.
- **Alternatives considered**: consultas JPA por entidad y consolidacion en Java (correcta pero mas ineficiente); varias repositorios JPA (mas viajes a base y riesgo de divergencia).

## Decision: elegibilidad de jugadores

- **Decision**: consultar exclusivamente filas de jugadores ya persistidas en el catalogo y pertenecientes a las cinco ligas `{2,3,4,5,22}` utilizadas por el scraping. El ranking no insertara ni dara de alta jugadores.
- **Rationale**: el catalogo existente se alimenta por `WhoScoredScrapingService`; `PlayerEntity` no tiene un campo de estado activo. No se agregara un estado nuevo en esta feature sin definicion adicional de ciclo de vida.
- **Alternatives considered**: crear jugadores desde el ranking (prohibido); agregar una nueva semantica de activo sin soporte en el modelo (fuera del alcance aclarado).

## Decision: scraping, contrato y E2E

- **Decision**: poblar o actualizar jugadores llamando `POST /quotes/scrape` definido en `docs/openapi.yaml`. El endpoint tiene `operationId: scrape`, `security: []`, referencia `#/components/parameters/NumberOfPlayers` (query `numberOfPlayers`, default 50, minimo 1) y respuestas 200/400. No requiere autenticacion.
- **Rationale**: reutiliza la ruta de carga existente y evita que el modulo ranking se acople a la creacion de jugadores.
- **Alternatives considered**: sembrar directamente jugadores en el flujo E2E (no prueba la integracion requerida); duplicar el contrato en un YAML propio de la feature (diverge de la fuente central).
- **E2E**: MockMvc + Testcontainers llamara la ruta HTTP real `/quotes/scrape?numberOfPlayers=N`. Un doble determinista de `ScrapingService` persistira fixtures en su callback y evitara red/WhoScored; el test configurara estrategias para los jugadores resultantes y consultara el ranking.
- **Actualizacion**: la consulta del ranking no cacheara catalogo ni cotizaciones; cada request observara las filas persistidas despues de completar scraping.

## Decision: cotizacion base y tenencias

- **Decision**: la cotizacion base sera la tenencia vigente del usuario superusuario para el jugador; si no existe o es no positiva, se usara `1.00`. El desempate sumara `tenencias.tokens` de usuarios inversores distintos del superusuario.
- **Rationale**: el superusuario ya posee una tenencia por jugador y su `valor_unitario` representa una cotizacion base calculada.
- **Alternatives considered**: columna base en `PlayerEntity` (duplica fuente); compras historicas (contradice FR-015).

## Decision: cotizaciones elegibles

- **Decision**: una cotizacion personalizada sera elegible si `valuaciones.is_current=true` y existe una configuracion `ACTUAL` coincidente para el mismo usuario y estrategia. El modelo `UserEntity` actual no contiene un atributo `activo`; no se inventa uno como parte de esta aclaracion.
- **Rationale**: expresa vigencia con los estados persistidos existentes y evita filtrar por una columna inexistente.
- **Alternatives considered**: agregar estado activo de usuario sin requerimiento o migracion definida; considerar valuaciones vigentes sin configuracion ACTUAL.

## Decision: filtros y paginacion

- **Decision**: pagina por defecto `1`, limite por defecto `20`, limite maximo `100`; filtros no vacios, sin caracteres de control y con longitud maxima 100.
- **Rationale**: rango acotado protege la consulta y permite una pagina vacia sin ampliar el conjunto.
- **Alternatives considered**: limite ilimitado (riesgo de timeout); ausencia de limites (contrato ambiguo).

## Decision: origen ante empates

- **Decision**: ante empates de valor entre estrategias, mostrar una estrategia deterministica sin multiplicar filas ni agregarla al desempate.
- **Rationale**: la especificacion permite informar una estrategia que haya producido el maximo.
- **Alternatives considered**: desempatar por estrategia (criterio adicional no solicitado).
