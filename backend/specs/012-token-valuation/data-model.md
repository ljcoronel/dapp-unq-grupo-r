j# Modelo de datos: Valuacion de tokens

## Jugador existente

`PlayerEntity` (`whoscored_players`) aporta `goals`, `assists`,
`passSuccessPercentage`, `aerialsWon`, `timesManOfTheMatch`, `minutesPlayed`,
`appearances` y `position`. Las metricas faltantes se interpretan como 0 antes de
normalizar y no impiden valuar ese jugador.

## Usuario existente

`UserEntity` (`usuarios`) es el propietario de configuraciones y tenencias. El
Superusuario es el Usuario unico designado por la configuracion de administracion y se
inicializa con Estrategia A.

## ConfiguracionEstrategia

Entidad persistente recomendada: `configuraciones_estrategia`.

| Campo | Regla |
|---|---|
| `id` | PK autogenerada |
| `usuario_id` | FK obligatoria a `usuarios` |
| `estrategia` | `ESTRATEGIA_A` o `ESTRATEGIA_B` |
| `pesos` | JSON/estructura persistida con las metricas configurables; B conserva 5% fijo para `position` |
| `estado` | `ACTUAL` o `PENDIENTE` |
| `vigente_desde` | fecha/hora de aplicacion |

Restriccion: como maximo una fila `PENDIENTE` por Usuario. Primera configuracion es
`ACTUAL` y produce valuacion inmediata; las siguientes reemplazan la pendiente.

## Tenencia

Entidad `TenenciaEntity` en tabla `tenencias`, con `TenenciaRepository`.

| Campo | Tipo/regla |
|---|---|
| `id` | PK |
| `usuario_id` | FK a `usuarios`, NOT NULL |
| `jugador_id` | FK a `whoscored_players`, NOT NULL |
| `tokens` | Integer NOT NULL; inventario inicial 100 por Jugador |
| `valorTotal` | Float NOT NULL |
| `valorUnitario` | Float NOT NULL |

Restriccion unica `(usuario_id, jugador_id)`. El valor total debe ser `tokens *
valorUnitario`, y todos los tokens de la fila comparten `valorUnitario`.

## Historial de valuacion

Entidad `HistorialValuacionEntity` en `historial_valuaciones`, con
`HistorialValuacionRepository`. Cada fila es un snapshot inmutable de una tenencia en
una ejecucion:

| Campo | Tipo/regla |
|---|---|
| `id` | PK |
| `usuario_id` | FK a `usuarios`, NOT NULL |
| `jugador_id` | FK a `whoscored_players`, NOT NULL |
| `tokens` | Integer NOT NULL; cantidad poseida durante la ejecucion |
| `valor_unitario` | Float NOT NULL; valor individual de cada Token |
| `valor_total` | Float NOT NULL; `tokens * valor_unitario` |
| `estrategia` | Estrategia aplicada |
| `ejecutado_en` | Timestamp NOT NULL |
| `ventana` | Identificador de ventana dominical o ejecucion manual |

Cada ejecucion agrega filas nuevas y nunca modifica filas historicas. La clave unica
`(usuario_id, jugador_id, ventana, tipo_ejecucion)` evita duplicar el mismo snapshot al
reintentar una ejecucion.

## Valuacion de ventana

Entidad auxiliar recomendada `ValuacionEntity` en `valuaciones`, para representar el
resultado vigente e idempotencia: `id`, `usuario_id`, `jugador_id`,
`ventana_dominical`, `estrategia`, `estado`, `valor_unitario`, `creado_en`.

Unica `(usuario_id, jugador_id, ventana_dominical)`. `estado` puede ser `VALUADA` o
`ERROR`; un error de ventana no reemplaza la ultima valuacion `VALUADA`.

## Reglas de dominio

1. Los pesos configurables son no negativos, contienen exactamente las metricas
   configurables de la estrategia y suman 100; en B los pesos configurables suman 95%
   porque `position` aporta un 5% fijo.
2. A usa exclusivamente `goals`, `assists`, `passSuccessPercentage`, `aerialsWon` y
   `timesManOfTheMatch`.
3. B usa exclusivamente `passSuccessPercentage`, `aerialsWon`, `minutesPlayed`,
   `appearances` y `position`; `position` tiene un peso fijo de 5% y vale 1 solo si
   es exactamente `Defender`, o 0 en cualquier otro caso.
4. La normalizacion produce valores en `[0,1]`; una metrica faltante usa directamente
   el valor normalizado 0 antes del calculo.
5. El precio se redondea a dos decimales antes de persistirse.
6. Una ventana dominical aplica la pendiente antes de recalcular.
7. La valuacion no crea ni elimina tokens.
8. El historial captura la cantidad poseida y el valor unitario de cada Token en el
   instante de la ejecucion, sin sobrescribir snapshots anteriores.
