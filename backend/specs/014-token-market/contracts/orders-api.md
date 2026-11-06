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
La cantidad de compra está limitada a 100 tokens. Al crearse una orden que
permanece pendiente, responde con `status: "PENDIENTE"` y `issuedAt`; si se
liquida inmediatamente, incluye `executedAt`, `settledPrice`, `buyerId` y
`sellerId`.

## Crear venta

`POST /orders/sell`

Usa el mismo body y `usuarioId` identifica al vendedor. Responde `201`;
rechaza falta de Strategy activa, tokens libres y validaciones de
cantidad/precio/tolerancia.

## Historial de operaciones

`GET /users/{id}/transactions`

Responde `200` con una lista de órdenes del `id` solicitado en estado
`EJECUTADA`, ordenadas por `fechaEjecucion` descendente. Cada elemento incluye
`orderId`, `operation`, `playerId`, `quantity`, `settledPrice`, `buyerId`,
`sellerId`, `issuedAt` y `executedAt`; nunca incluye órdenes pendientes o
canceladas.

## Órdenes pendientes

`GET /users/{id}/orders`

Responde `200` con una lista de órdenes del `id` solicitado en estado
`PENDIENTE`, ordenadas por `fechaEmision` descendente. Cada elemento incluye
`orderId`, `operation`, `playerId`, `quantity`, `price`, `tolerance`, `status`
y `issuedAt`; no incluye `executedAt` ni `cancelledAt`.

## Cancelar orden

`DELETE /orders/{orderId}?usuarioId={id}`

Responde `204` cuando la orden pendiente del propietario queda Cancelada y se
libera la reserva completa. Responde `403` si el `usuarioId` no es propietario,
`404` si la orden no existe y `409` si ya no está Pendiente.

## Forma de respuesta

La respuesta de creación usa `orderId`, `userId`, `playerId`, `operation`,
`quantity`, `price`, `tolerance`, `status`, `issuedAt`, `executedAt`,
`cancelledAt`, `settledPrice`, `buyerId` y `sellerId`. Las propiedades de fecha
que no aplican al estado no se incluyen. Todas las fechas se serializan en
ISO-8601 y los importes tienen dos decimales.

El historial expone únicamente órdenes Ejecutadas y sus datos de liquidación;
la consulta de órdenes pendientes no incluye órdenes Ejecutadas o Canceladas.
Los errores siguen el formato estándar `{"message":"..."}`. Al crear una orden
se comprueba la Estrategia activa antes de bloquear saldo o tokens.
