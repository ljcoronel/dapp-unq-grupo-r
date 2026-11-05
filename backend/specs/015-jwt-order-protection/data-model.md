# Modelo de datos y seguridad

## JWT

Credencial firmada con HS256, `subject` igual al nombre del usuario, fecha de emisión y
fecha de expiración. El filtro valida firma, vigencia y que el usuario siga existiendo.
Un token ausente, malformado, inválido, vencido o asociado a un usuario eliminado no
produce autenticación y termina en HTTP 401 para los dos endpoints protegidos.

## Usuario autenticado

Representado durante la request por `UserPrincipal`, que encapsula el `User` de
dominio cargado por `UserService`.

| Campo | Tipo | Regla |
|---|---|---|
| `id` | `Long` | Identificador persistido; fuente única de atribución de la orden |
| `nombre` | `String` | Subject validado del JWT y clave de carga del usuario |

El principal debe exponer el `id` sin agregar anotaciones de persistencia al modelo.

## CrearOrdenRequest

Se conserva el cuerpo actual para compatibilidad:

| Campo | Tipo | Validación existente |
|---|---|---|
| `usuarioId` | `Long` | obligatorio y positivo; debe coincidir con el principal |
| `playerId` | `Long` | obligatorio y positivo |
| `quantity` | `Integer` | obligatorio, mínimo 1 |
| `price` | `BigDecimal` | obligatorio, no negativo, hasta 13 enteros y 2 decimales |
| `tolerance` | `BigDecimal` | obligatorio, no negativa, hasta 13 enteros y 2 decimales |

El controller verifica la identidad antes de invocar `CrearOrdenCompraService` o
`CrearOrdenVentaService`. La solicitud no autenticada o inconsistente no alcanza
validaciones de negocio ni operaciones de reserva/persistencia.

## Orden de compra / venta

La orden mantiene `usuarioId`, tipo de operación, jugador, cantidad, precio,
tolerancia y estado. Su propietario persistido es `UserEntity` mediante
`OrdenEntity.usuario`. La transición y matching permanecen en los servicios/modelos
actuales; la única nueva invariante de entrada es:

`orden.usuarioId == UserPrincipal.user.id`

La orden nunca se crea para un usuario diferente al autenticado.
