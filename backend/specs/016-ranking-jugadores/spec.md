# Especificación de Funcionalidad: Ranking Global de Jugadores

**Rama de funcionalidad**: `016-ranking-jugadores` (no creada; especificación solamente)

**Creada**: 2026-11-25

**Estado**: Borrador

**Entrada**: Ranking global de jugadores de fútbol para el mercado de tokens, consolidando sus cotizaciones vigentes según estrategias y pesos personalizados.

## Escenarios de usuario y pruebas

### Historia de usuario 1 - Consultar el ranking global (Prioridad: P1)

Como usuario inversor, quiero consultar a todos los jugadores activos ordenados por el valor vigente más alto de sus tokens, para comparar su costo y reconocer al jugador más costoso.

**Por qué tiene esta prioridad**: La consolidación y el orden correcto son el valor central del ranking; sin ellos, los valores simultáneos de un jugador distorsionarían la comparación.

**Prueba independiente**: Consultar el ranking completo con jugadores que tengan varias cotizaciones vigentes y verificar cobertura, unicidad, valor máximo, origen y orden.

**Escenarios de aceptación**:

1. **Dado** el Jugador X con cotizaciones vigentes de 110 créditos por Estrategia A y 135 créditos por Estrategia B, **cuando** se consulta el ranking, **entonces** X aparece una sola vez con 135 créditos e indica Estrategia B como origen.
2. **Dado** un catálogo con jugadores activos de distintas ligas, **cuando** se consulta el ranking sin filtros, **entonces** cada jugador activo aparece exactamente una vez, sin omisiones ni duplicados.
3. **Dado** jugadores con valores máximos diferentes, **cuando** se consulta el ranking, **entonces** aparecen de mayor a menor valor y el jugador más costoso ocupa el puesto #1.
4. **Dado** dos jugadores con el mismo valor máximo, **cuando** se consulta el ranking, **entonces** se aplica sucesivamente el desempate por mayor cotización base estándar, mayor cantidad actual de tokens en manos de inversores y nombre alfabético ascendente.
5. **Dado** un jugador sin cotizaciones personalizadas vigentes, **cuando** se consulta el ranking, **entonces** aparece con su cotización base vigente o con su valor inicial de 1 crédito si aún no tuvo recálculos, e identifica el origen como “Cotización base”.

### Historia de usuario 2 - Explorar por filtros y páginas (Prioridad: P2)

Como usuario inversor, quiero filtrar el ranking por liga, equipo o posición en cancha y recorrerlo por páginas, para encontrar jugadores relevantes sin perder el orden ni ver duplicados.

**Por qué tiene esta prioridad**: Los filtros y la paginación facilitan explorar un catálogo completo sin cambiar cómo se calcula el lugar de cada jugador.

**Prueba independiente**: Aplicar cada filtro por separado y en combinación, recorrer las páginas resultantes y comprobar que la lista equivale al ranking filtrado, ordenado y sin registros repetidos.

**Escenarios de aceptación**:

1. **Dado** uno o más filtros por liga, equipo y/o posición, **cuando** se consulta una página, **entonces** solo se incluyen jugadores que cumplen todos los filtros seleccionados, ordenados por las mismas reglas del ranking global.
2. **Dado** un resultado filtrado que ocupa varias páginas, **cuando** se recorren todas las páginas, **entonces** cada coincidencia aparece exactamente una vez y las posiciones continúan en el orden del conjunto filtrado, sin reiniciarse en cada página.
3. **Dado** filtros válidos que no coinciden con ningún jugador, **cuando** se consulta el ranking, **entonces** se muestra un resultado vacío y un total filtrado de cero.

### Historia de usuario 3 - Ver cambios vigentes en el ranking (Prioridad: P2)

Como usuario inversor, quiero que el ranking refleje las cotizaciones actuales después de un cambio de estrategia, pesos o recálculo, para tomar decisiones con información vigente.

**Por qué tiene esta prioridad**: Un ranking basado en valores anteriores podría mostrar un liderazgo que ya no representa las cotizaciones vigentes.

**Prueba independiente**: Cambiar una configuración para que un jugador supere el valor del líder y consultar nuevamente el ranking; verificar también que los valores históricos no alteran el resultado.

**Escenarios de aceptación**:

1. **Dado** un cambio confirmado de estrategia o pesos que eleva la cotización vigente de un jugador por encima del líder, **cuando** se vuelve a consultar el ranking, **entonces** ese jugador ocupa el puesto #1 con el nuevo valor y el origen correspondiente.
2. **Dado** un recálculo vigente de cotizaciones, **cuando** se consulta el ranking después de completarse, **entonces** se ordena según esos valores actuales y no según cotizaciones históricas.

### Casos borde

- Si un jugador está activo en el catálogo, pero no tiene cotizaciones personalizadas vigentes, se incluye con su cotización base estándar actual; si nunca se recalculó, se usa el valor inicial de 1 crédito.
- Si existen varias cotizaciones vigentes iguales y máximas para un jugador, se muestra una sola fila y se informa como origen una estrategia que haya producido ese máximo; si más de una estrategia lo produjo, se informa la estrategia correspondiente al valor mostrado sin duplicar al jugador.
- Si dos jugadores coinciden en valor máximo, cotización base estándar y cantidad de tokens en manos de inversores, el nombre ascendente determina el orden. Si también comparten nombre, se usa el identificador estable del jugador en orden ascendente para mantener un orden reproducible.
- Si los filtros no producen coincidencias, el resultado es vacío; no se agregan jugadores fuera del filtro para completar una página.
- Las cotizaciones pasadas, reemplazadas o que ya no están vigentes no participan en el máximo ni en el orden actual.
- Al paginar resultados con valores empatados, se conserva el orden de desempate entre páginas para que una consulta no duplique ni omita jugadores.
- Si se modifica una estrategia, los pesos o las cotizaciones mientras se consulta el ranking, la siguiente consulta completada debe reflejar la información vigente en ese momento; no se exige que una consulta ya iniciada cambie en curso.

## Requisitos

### Requisitos funcionales

- **FR-001**: El ranking sin filtros MUST incluir a todos los jugadores activos registrados en el catálogo de las cinco ligas principales.
- **FR-002**: El ranking MUST mostrar a cada jugador activo como máximo una vez, sin importar cuántas cotizaciones vigentes tenga.
- **FR-003**: Para cada jugador con una o más cotizaciones personalizadas vigentes asociadas a estrategias y pesos de usuarios activos, el ranking MUST elegir el mayor valor vigente como valor representativo.
- **FR-004**: El sistema MUST excluir del cálculo las cotizaciones históricas, reemplazadas o no vigentes.
- **FR-005**: Para cada jugador sin cotización personalizada vigente, el sistema MUST usar su cotización base estándar vigente o, si todavía no fue recalculada, el valor inicial de emisión de 1 crédito.
- **FR-006**: El ranking MUST ordenar los jugadores por valor representativo descendente.
- **FR-007**: Si dos jugadores empatan en valor representativo, el sistema MUST desempatar en este orden: cotización base estándar descendente, cantidad actual de tokens en manos de usuarios inversores descendente y nombre ascendente. Si el nombre también empata, MUST ordenar por el identificador estable del jugador ascendente.
- **FR-008**: Cada fila MUST informar la posición, el nombre del jugador, su equipo, liga y posición en cancha, el valor representativo actual y el origen de ese valor.
- **FR-009**: Cuando el valor representativo provenga de una cotización personalizada, el origen informado MUST indicar Estrategia A o Estrategia B, según corresponda. Si proviene del fallback, MUST indicar “Cotización base”.
- **FR-010**: Los usuarios MUST poder solicitar páginas del ranking y aplicar opcionalmente filtros por liga, equipo y posición, de manera individual o combinada.
- **FR-011**: Los filtros MUST aplicarse antes de determinar el orden y la página. Los resultados filtrados MUST conservar las reglas de orden y unicidad del ranking.
- **FR-012**: La posición mostrada MUST numerarse dentro del conjunto filtrado y ordenado, y continuar entre páginas sin reiniciarse.
- **FR-013**: Los metadatos de paginación MUST indicar el total de jugadores que coinciden con los filtros y la página solicitada; una consulta sin coincidencias MUST devolver cero resultados y total cero.
- **FR-014**: Una consulta completada después de un cambio vigente de estrategia, pesos o recálculo MUST reflejar las nuevas cotizaciones y el orden resultante.
- **FR-015**: La cantidad usada para desempatar MUST corresponder a los tokens que actualmente permanecen en manos de usuarios inversores, no al total histórico de compras.

### Entidades clave

- **Jugador activo**: Jugador registrado en el catálogo y habilitado para aparecer en el ranking; incluye nombre, equipo, liga, posición en cancha e identificador estable.
- **Cotización vigente**: Valor actual de un token de jugador. Puede corresponder a una estrategia y pesos configurados por un usuario activo o ser la cotización base estándar del jugador.
- **Configuración de valuación**: Estrategia A o B y pesos personalizados que determinan una cotización vigente para un jugador.
- **Tenencia de tokens**: Cantidad actual de tokens de un jugador en manos de usuarios inversores; se utiliza como segundo criterio de desempate.
- **Resultado del ranking**: Lista paginada de jugadores con su posición, datos básicos, valor máximo vigente y origen, más el total que coincide con los filtros.

## Criterios de éxito

### Resultados medibles

- **SC-001**: En el ranking completo, el 100% de los jugadores activos del catálogo aparece exactamente una vez al recorrer todas las páginas.
- **SC-002**: En el 100% de los casos de prueba con múltiples cotizaciones vigentes, el valor y la estrategia mostrados corresponden al máximo vigente; el orden coincide con los criterios de orden y desempate definidos.
- **SC-003**: En el 100% de las combinaciones de filtros y páginas verificadas, no se muestran jugadores fuera del filtro ni se duplican u omiten coincidencias entre páginas.
- **SC-004**: En al menos el 95% de las consultas con el catálogo activo completo, los usuarios ven la página solicitada en menos de 2 segundos.
- **SC-005**: En pruebas de uso, al menos 9 de cada 10 usuarios pueden identificar correctamente al líder, su valor y el origen de su cotización, y completar una búsqueda usando los filtros disponibles.
- **SC-006**: En el 100% de las consultas realizadas tras un cambio confirmado o recálculo completado, el ranking refleja los valores vigentes y no usa cotizaciones históricas para el orden.

## Supuestos

- “Jugador activo” significa que su estado actual en el catálogo permite incluirlo en el mercado; los jugadores inactivos quedan fuera.
- Se consideran elegibles las cotizaciones vigentes derivadas de configuraciones actualmente activas de usuarios inversores activos; una configuración deshabilitada o una cotización histórica no participa.
- Si varias estrategias producen el mismo máximo para el mismo jugador, se muestra una sola fila y se informa una estrategia que haya producido exactamente ese valor; no se crea un orden adicional entre estrategias.
- La cotización base se utiliza como fallback cuando no existe cotización personalizada vigente para el jugador; no compite como una cotización adicional contra las personalizadas.
- Los filtros combinados se interpretan conjuntamente: un jugador debe satisfacer todos los filtros proporcionados.
- El ranking no modifica las reglas de valuación ni las tenencias; únicamente presenta los datos vigentes según los criterios definidos.
- Los valores se comparan con la precisión vigente de las cotizaciones del mercado, sin introducir una regla de redondeo nueva.
- En el desempate final se emplea el identificador estable del jugador solo cuando los tres criterios solicitados y el nombre también coinciden.
- La meta de 2 segundos y la validación con usuarios son objetivos de experiencia observables para el catálogo activo completo, independientes de la tecnología usada.
