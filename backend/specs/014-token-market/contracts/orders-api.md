# Contrato HTTP: Mercado de órdenes

Todos los endpoints requieren la autenticación existente. Los errores siguen el
formato de error estándar del backend.

## Crear compra

`POST /orders/buy`

```json
{
  "playerId": 8,
  "quantity": 20,
  "price": 20.00,
  "tolerance": 2.00
}
```

La identidad del comprador se toma del principal autenticado. Responde `201`
con la orden creada y, si el Superusuario tiene tokens suficientes, con la
orden ya Ejecutada. Debe rechazar cantidades inválidas, precisión superior a
dos decimales, falta de créditos libres o jugador inexistente.

## Crear venta

`POST /orders/sell`

Usa el mismo body. La identidad del vendedor se toma del principal
autenticado. Responde `201`; rechaza falta de tokens libres y validaciones de
cantidad/precio/tolerancia.

## Historial de operaciones

`GET /users/{id}/transactions`

Responde `200` con una lista de órdenes propias `EJECUTADA`, ordenadas por
`fechaEjecucion` descendente. Cada elemento incluye operación, jugador,
cantidad, precio liquidado, comprador, vendedor, `fechaEmision` y
`fechaEjecucion`; nunca incluye órdenes pendientes o canceladas.

## Órdenes pendientes

`GET /users/{id}/orders`

Responde `200` con una lista de órdenes propias `PENDIENTE`, ordenadas por
`fechaEmision` descendente. Cada elemento incluye operación, jugador,
cantidad, precio, tolerancia, estado y `fechaEmision`; no incluye
`fechaEjecucion` ni `fechaCancelacion`.

## Forma de respuesta

Los nombres JSON deben seguir el estilo existente del backend y las fechas
deben serializarse en ISO-8601. Una orden Cancelada, si se expone mediante
servicios internos o una futura extensión, debe incluir `fechaCancelacion` y no
`fechaEjecucion`.
