# Research

## Decision: consulta unica PostgreSQL

- **Decision**: usar un Adapter JDBC con CTEs y `ROW_NUMBER()` para filtros, consolidacion, orden y paginacion.
- **Rationale**: evita cargar catalogos y cotizaciones completas en memoria, elimina duplicados en la consulta y conserva el mismo criterio en conteo y pagina.
- **Alternatives considered**: consultas JPA por entidad y consolidacion en Java (correcta pero mas ineficiente); varias repositorios JPA (mas viajes a base y riesgo de divergencia).

## Decision: elegibilidad de jugadores

- **Decision**: incluir jugadores cuyo campo `activo=true` y cuya liga tenga uno de los external ID `{2,3,4,5,22}` usados por el scraper.
- **Rationale**: es el conjunto materializado en `WhoScoredScrapingService` y `PlayerEntity` no contiene actualmente estado activo.
- **Alternatives considered**: usar todas las ligas persistidas (podria incluir ligas fuera de las cinco); tabla configurable de ligas (mayor alcance que el requisito inmediato).

## Decision: cotizacion base y tenencias

- **Decision**: la cotizacion base sera la tenencia vigente del usuario superusuario para el jugador; si no existe o es no positiva, se usara `1.00`. El desempate sumara `tenencias.tokens` de usuarios inversores distintos del superusuario.
- **Rationale**: el superusuario ya posee una tenencia por jugador y su `valor_unitario` representa una cotizacion base calculada.
- **Alternatives considered**: columna base en `PlayerEntity` (duplica fuente); compras historicas (contradice FR-015).

## Decision: usuarios y cotizaciones activas

- **Decision**: una cotizacion personalizada sera elegible si `valuaciones.is_current=true`, el usuario tiene `activo=true` y existe una configuracion `ACTUAL` para el mismo usuario y estrategia.
- **Rationale**: separa vigencia historica, estado del usuario y estado de estrategia.
- **Alternatives considered**: considerar toda valuacion con `is_current=true` aunque su usuario no tenga configuracion ACTUAL (permitiria valores huerfanos).

## Decision: filtros y paginacion

- **Decision**: pagina por defecto `1`, limite por defecto `20`, limite maximo `100`; filtros no vacios, sin caracteres de control y con longitud maxima 100.
- **Rationale**: rango acotado protege la consulta y permite una pagina vacia sin ampliar el conjunto.
- **Alternatives considered**: limite ilimitado (riesgo de timeout); ausencia de limites (contrato ambiguo).

## Decision: origen ante empates

- **Decision**: ante empates de valor entre estrategias, mostrar una estrategia deterministica sin multiplicar filas ni agregarla al desempate.
- **Rationale**: la especificacion permite informar una estrategia que haya producido el maximo.
- **Alternatives considered**: desempatar por estrategia (criterio adicional no solicitado).
