# Modelo de datos: Mercado de órdenes

## Orden

Representa el compromiso completo de compra o venta de tokens.

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | `Long` | Identificador positivo generado por persistencia |
| `usuario` | `User` | Propietario; obligatorio; se resuelve desde `usuarioId` |
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

Se reutiliza la `Holding` existente para cada par usuario/jugador. Una venta
pendiente mueve la cantidad a bloqueado; al ejecutar transfiere los tokens al
comprador. Si la Holding del comprador existe, aumenta su cantidad y conserva
el valor unitario; si no existe, se crea con el valor unitario calculado por la
Strategy activa del comprador y el valor vigente del jugador. Al cancelar se
liberan los tokens bloqueados.

## Estrategia de Valuación

Se reutiliza la Strategy activa existente. Es un prerrequisito para crear
cualquier orden, tanto de compra como de venta. La Strategy sólo determina el
valor unitario inicial de tokens comprados cuando el comprador no tenía Holding;
no reemplaza el valor unitario de una Holding existente ni usa el precio de
liquidación.

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
