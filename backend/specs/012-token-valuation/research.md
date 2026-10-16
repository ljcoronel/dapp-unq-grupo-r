# Research: Valuacion de tokens de jugadores

## Decision: Separar configuracion, tenencia, valuacion vigente e historial

La configuracion de estrategia se almacenara por Usuario con una configuracion actual y
como maximo una pendiente. `TenenciaEntity` almacenara la cantidad agregada por
Usuario/Jugador, incluyendo `tokens`, `valorTotal` y `valorUnitario` como exige la
especificacion. La valuacion se calculara en el dominio y se persistira en la misma
tenencia para el valor vigente; el scheduler actualizara esas columnas atomicamente.

**Rationale:** Evita crear 100 filas fisicas por jugador, preserva la unicidad del valor
por Usuario/Jugador y respeta el alcance que pide una tabla Tenencia. Cada ejecucion
dominical o manual agregara un snapshot inmutable de la cantidad poseida y del valor
unitario de cada Token por Usuario/Jugador. La cantidad de inventario se valida para que
el Superusuario tenga exactamente 100 por Jugador.

## Decision: Estrategias como catalogo cerrado

Se modelaran `ESTRATEGIA_A` y `ESTRATEGIA_B` como enum/catalogo inmutable, no como filas
creables por API. A permite `goals`, `assists`, `passSuccessPercentage`, `aerialsWon` y
`timesManOfTheMatch`. B permite `passSuccessPercentage`, `aerialsWon`, `minutesPlayed`,
`appearances` y la metrica derivada `isDefender`; esta ultima tendra el peso de 5% fijado
por la estrategia.

Los pesos configurables se expresan en porcentajes y deben sumar exactamente 100. La
configuracion del Superusuario se inicializa con A y los pesos 30/25/15/15/15.

## Decision: Normalizacion determinista y dinero redondeado

El calculador obtendra minimos y maximos del snapshot de jugadores para cada metrica.
Cuando todos los valores sean iguales, la metrica normalizada sera 0 para evitar una
division por cero. La posicion Defender se convertira directamente a 1 o 0. El calculo
se hara con `BigDecimal`, y el valor persistido/expuesto se redondeara a dos decimales
con `HALF_UP`; los campos JPA conservaran el tipo `Float` requerido por el contrato de
la tabla, con conversion centralizada.

## Decision: Ventana semanal atomica e idempotente

Se habilitara scheduling con cron configurable `0 0 3 * * SUN` y zona
`America/Argentina/Buenos_Aires`. El scheduler delegara en un servicio que primero
ejecuta `scrape("50")`, valida que la actualizacion sea completa y luego, en una
transaccion corta, aplica pendientes y calcula tenencias. Una clave de ventana
dominical y una restriccion unica impediran doble aplicacion por reintento.

Si scrape o el calculo falla, la excepcion se propaga al limite transaccional, se
conserva la ultima valuacion y se registra un estado operativo explicito. No se
capturaran errores para continuar con datos parciales.

## Decision: Contratos autenticados y administracion interna

Se expondran endpoints autenticados para configurar/consultar estrategia y consultar
tenencias. La inicializacion de los 100 tokens y la operacion del scheduler seran
servicios internos/administrativos, no endpoints de transferencia. Como excepción,
`POST /quotes/recalculate` sera publico y sin autenticacion para permitir una ejecucion
manual urgente; reutilizara exactamente el flujo del scheduler y no tendra una variante
de calculo. El contrato OpenAPI documentara estados `actual`, `pendiente`,
`sin_estrategia`; las metricas faltantes no generan un estado separado porque se
reemplazan por 0.

## Alternatives considered

- Persistir 100 entidades `Token`: rechazado porque el requerimiento define explicitamente
  una tenencia agregada y la valuacion es unica por jugador.
- Usar `double` para el calculo: rechazado por errores de redondeo monetario.
- Ejecutar valuacion aunque scrape falle: rechazado por producir valores con datos
  parcialmente actualizados.
- Permitir estrategias dinamicas: rechazado porque el alcance fija exactamente dos.
- Sobrescribir solo el valor vigente: rechazado porque se requiere consultar el valor
  individual historico de los Token poseidos en cada ejecucion.
