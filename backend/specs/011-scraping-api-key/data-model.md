# Modelo de datos: API key de scraping

## `ApiKey` (modelo de dominio)

Representa la autorización compartida para una operación protegida.

| Campo | Tipo | Reglas |
|---|---|---|
| `permission` | `String` | Obligatorio; para esta feature debe ser exactamente `scraping`. |
| `keyHash` | `String` | Obligatorio; contiene BCrypt y nunca el valor plano. |
| `active` | `boolean` | Solo una fila activa para `scraping`; una clave inactiva no autoriza. |
| `createdAt` | `LocalDateTime` | Obligatorio; se asigna al crear. |
| `updatedAt` | `LocalDateTime` | Obligatorio; se actualiza en cada rotación. |

El valor plano generado no pertenece al modelo persistente: se transporta como
resultado efímero del comando y no se registra.

## `ApiKeyEntity`

Entidad JPA separada del modelo de dominio, almacenada en `api_keys`. Debe tener
una restricción única sobre `permission` y un índice para la consulta de
`permission + active`. El repository debe ofrecer búsqueda activa por permiso y
una operación de actualización protegida por lock pesimista para serializar
rotaciones concurrentes.

## Relaciones y transiciones

No hay relación con usuarios: la credencial es deliberadamente compartida.

`NO_EXISTE -> ACTIVA` al crear por primera vez.

`ACTIVA(vieja) -> ACTIVA(nueva)` como una única operación transaccional de
reemplazo. El hash viejo deja de coincidir al confirmar la actualización.

Si la creación o actualización falla, la transacción vuelve a `ACTIVA(vieja)` y
el comando no imprime un valor. No se permite un estado exitoso con cero o más
de una credencial activa para `scraping`.
