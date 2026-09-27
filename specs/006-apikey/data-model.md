# Modelo de datos: API Keys

## Entidades

### API Key (modelo de dominio)

Representa una credencial alternativa propiedad de un usuario. El modelo no depende de JPA ni de Spring Security.

| Campo | Tipo | Regla |
|---|---|---|
| `id` | UUID | Selector publico, unico y no secreto; se usa para localizar una clave sin escanear hashes. |
| `ownerUserId` | Long | Identificador de un usuario existente; no se acepta del cliente como identidad autorizada. |
| `label` | String | Nombre descriptivo obligatorio, recortado y limitado en longitud por el DTO. |
| `secretHash` | byte[] | SHA-256 del secreto aleatorio; nunca se serializa ni se devuelve. |
| `createdAt` | Instant | Instante UTC de creacion; asignado por el servidor. |
| `expiresAt` | Instant nullable | Si existe, la clave deja de autenticar desde el instante indicado. |
| `revokedAt` | Instant nullable | Si existe, la clave queda revocada inmediatamente y no puede reactivarse. |

**Relaciones**: muchas claves pueden pertenecer a un usuario; cada clave pertenece a exactamente un usuario. La tabla JPA mantiene clave foranea a usuarios y un indice/constraint unico sobre `id`.

**Invariantes**:
- Selector y propietario obligatorios.
- Hash de secreto obligatorio y de longitud fija.
- `label` no puede ser nulo ni quedar vacio luego de sanitizar.
- Si hay vencimiento, debe ser posterior a la creacion.
- Una clave revocada o vencida no puede autenticar y no se reactiva.
- El secreto original no forma parte del modelo persistido ni de DTOs de lectura.

**Transiciones**:
- Creada -> activa, si no vencio.
- Activa -> vencida, al alcanzar `expiresAt`.
- Activa -> revocada, al asignarse `revokedAt`.
- Vencida y revocada son estados terminales para autenticacion; revocar no elimina el registro de auditoria.

### User

Entidad existente que se reutiliza como propietario y principal autenticado. No se agregan permisos de negocio ni roles nuevos. En solicitudes autenticadas, las authorities del principal API Key se derivan del mismo usuario resuelto para JWT.

### Solicitud de autenticacion

Entrada HTTP que contiene ruta, metodo y, como maximo, una de estas credenciales: `Authorization: Bearer` o `x-api-key`. Si ambas aparecen, o si `x-api-key` aparece en `POST /login` o `POST /register`, la cadena rechaza la solicitud antes del controller.

## DTOs de ciclo de vida

- **Crear**: `label` obligatorio; `expiresAt` opcional y en el futuro. El usuario propietario se deriva del principal JWT. La respuesta `201` incluye el secreto una sola vez.
- **Listar**: solo incluye `id`, `label`, `createdAt`, `expiresAt`, `revokedAt` y estado; no incluye el hash ni el secreto.
- **Revocar**: identifica clave por UUID y limita la operacion al propietario autenticado; no permite cambiar propietario ni reactivar la clave.

## Persistencia

La entidad de persistencia se mantiene separada del modelo de dominio y se convierte mediante mapper. La busqueda de autenticacion se realiza por el selector indexado, valida el estado de ciclo de vida y compara el hash calculado en tiempo constante. Las consultas de administracion siempre incluyen el ID del propietario.
