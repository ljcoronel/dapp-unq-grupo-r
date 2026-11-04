# Modelo de datos: Mercado de órdenes

## Orden

Representa el compromiso completo de compra o venta de tokens.

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | `Long` | Identificador positivo generado por persistencia |
| `usuario` | `User` | Propietario; obligatorio |
| `tipoOperacion` | `COMPRA`/`VENTA` | Obligatorio |
| `jugador` | `Player` | Obligatorio |
| `cantidad` | `Integer` | Entero positivo; compra limitada a 100 |
| `precio` | `BigDecimal` | Total no negativo, máximo 2 decimales |
| `tolerancia` | `BigDecimal` | No negativa, máximo 2 decimales |
| `estado` | `PENDIENTE`/`EJECUTADA`/`CANCELADA` | Inicia Pendiente |
| `fechaEmision` | `Instant` | Obligatoria e inmutable |
| `fechaEjecucion` | `Instant` | Sólo Ejecutada |
| `fechaCancelacion` | `Instant` | Sólo Cancelada |
| `precioLiquidado` | `BigDecimal` | Obligatorio sólo al ejecutar |
| `comprador`/`vendedor` | `User` | Se completan al ejecutar |

La compra define el rango `[precio, precio + tolerancia]`. La venta define
`[precio - tolerancia, precio]`, sin permitir precios negativos. Dos órdenes
coinciden sólo si son de usuarios distintos, tienen el mismo jugador y
cantidad, y sus rangos se intersectan. No existen ejecuciones parciales.

## Saldo de créditos

Cada usuario mantiene `creditosDisponibles` y `creditosBloqueados`, con dos
decimales y suma no negativa. Una compra pendiente mueve
`precio + tolerancia` a bloqueado; al ejecutar debita el precio liquidado y
libera la diferencia; al cancelar libera todo.

## Tenencia de tokens

Cada par usuario/jugador mantiene `tokensDisponibles` y `tokensBloqueados`,
ambos enteros no negativos. Una venta pendiente mueve la cantidad a bloqueado;
al ejecutar transfiere los tokens al comprador; al cancelar los libera.

## Operación

La operación no requiere necesariamente una tabla separada: queda representada
por los datos de liquidación de las dos órdenes ejecutadas. Si la implementación
requiere una entidad para unicidad/auditoría, debe referenciar ambas órdenes,
precio, cantidad y fecha dentro de la misma transacción.

## Transiciones

```text
PENDIENTE --coincidencia válida--> EJECUTADA
PENDIENTE --cancelación del propietario--> CANCELADA
EJECUTADA/CANCELADA --cualquier acción--> rechazado, sin cambios
```

Una orden Pendiente no vence. Las fechas extra son mutuamente excluyentes:
Ejecutada tiene `fechaEjecucion`, Cancelada tiene `fechaCancelacion` y
Pendiente no tiene ninguna.
