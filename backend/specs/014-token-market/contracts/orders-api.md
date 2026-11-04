# Contrato HTTP: Mercado de órdenes

Ninguno de estos endpoints requiere JWT ni API key. Los errores siguen el
formato de error estándar del backend.

## Crear compra

`POST /orders/buy`

```json
{
  "usuarioId": 2,
  "playerId": 8,
  "quantity": 20,
  "price": 20.00,
  "tolerance": 2.00
}
```

El usuario se resuelve mediante `usuarioId`. Responde `201` con la orden
creada y, si el Superusuario tiene tokens suficientes, con la
orden ya Ejecutada. Debe rechazar cantidades inválidas, precisión superior a
dos decimales, falta de créditos libres o jugador inexistente.

## Crear venta

`POST /orders/sell`

Usa el mismo body y `usuarioId` identifica al vendedor. Responde `201`;
rechaza falta de Strategy activa, tokens libres y validaciones de
cantidad/precio/tolerancia.

## Historial de operaciones

`GET /users/{id}/transactions`

Responde `200` con una lista de órdenes del `id` solicitado en estado
`EJECUTADA`, ordenadas por `fechaEjecucion` descendente. Cada elemento incluye
operación, jugador, cantidad, precio liquidado, comprador, vendedor,
`fechaEmision` y `fechaEjecucion`; nunca incluye órdenes pendientes o
canceladas.

## Órdenes pendientes

`GET /users/{id}/orders`

Responde `200` con una lista de órdenes del `id` solicitado en estado
`PENDIENTE`, ordenadas por `fechaEmision` descendente. Cada elemento incluye
operación, jugador, cantidad, precio, tolerancia, estado y `fechaEmision`; no
incluye `fechaEjecucion` ni `fechaCancelacion`.

## Forma de respuesta

Los nombres JSON deben seguir el estilo existente del backend y las fechas
deben serializarse en ISO-8601. Al crear una orden se debe comprobar Strategy
activa antes de bloquear saldo o tokens. Una orden Cancelada, si se expone mediante
servicios internos o una futura extensión, debe incluir `fechaCancelacion` y no
`fechaEjecucion`.
