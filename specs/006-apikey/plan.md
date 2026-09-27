# Plan de implementacion: API Keys como autenticacion alternativa

**Branch**: `006-apikey` | **Date**: 2026-09-27 | **Spec**: `/specs/006-apikey/spec.md`

**Input**: Especificacion de la funcionalidad en `/specs/006-apikey/spec.md`, con alcance aclarado por el usuario: claves por usuario (no un secreto compartido) y autenticacion requerida en todas las rutas salvo `POST /login` y `POST /register`.

## Summary

Se incorporara autenticacion con API Keys generadas por usuario junto a la autenticacion JWT existente. Cada clave tendra un selector publico y un secreto aleatorio de alta entropia; el backend mostrara el secreto solo al crearla y persistira unicamente su hash. Un filtro Spring Security extraera `x-api-key` y delegara validacion y resolucion del usuario a un `AuthenticationProvider`. La cadena sera stateless, rechazara credenciales Bearer y API Key simultaneas y exigira autenticacion en todas las rutas excepto `POST /login` y `POST /register`. Las operaciones para crear, listar y revocar claves seran exclusivas de JWT para impedir que una clave administre credenciales.

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Spring Boot 4.0.0, Spring Security, Spring Web, Spring Data JPA, PostgreSQL, Gradle Wrapper 9.1.0; las dependencias necesarias ya estan declaradas

**Storage**: PostgreSQL existente; nueva tabla JPA para claves, con indice por selector y relacion con el usuario propietario

**Testing**: JUnit 5; pruebas unitarias del modelo, generador, hasher y provider; pruebas de persistencia con Testcontainers; pruebas E2E con MockMvc exclusivamente en `com.dappunq.e2e`

**Target Platform**: Servicio web JVM y CI del monorepo existente

**Project Type**: Backend REST de un monorepo con frontend React; el cambio de autenticacion y su contrato corresponden al backend

**Performance Goals**: Buscar por un selector indexado y verificar un hash SHA-256 por solicitud; mantener una latencia comparable a la validacion JWT existente

**Constraints**: Sin estado de sesion; mantener JWT; no almacenar, retornar nuevamente ni registrar el secreto completo; respetar arquitectura controller/service/model/persistence; textos y documentacion en espanol; actualizar el contrato OpenAPI canonico junto con los mappings y la politica de seguridad

**Scale/Scope**: API existente de autenticacion, usuarios, jugadores y documentacion; nuevas operaciones para crear, listar y revocar varias claves por usuario

## Constitution Check

*GATE: Debe pasar antes de Phase 0 research y volver a evaluarse despues del diseno.*

- Pass: El cambio se limita al backend del monorepo; el frontend no tiene llamadas a recursos protegidos que requieran modificar su flujo actual.
- Pass: Controllers recibiran DTOs y delegaran en services; el service coordinara modelo y persistencia; el modelo de dominio validara su estado y revocacion.
- Pass: La entidad JPA y su mapper permaneceran en persistence y no contendran logica de autenticacion.
- Pass: Los filtros y el provider adaptaran credenciales HTTP a Spring Security; no incorporaran reglas de negocio ni acceso directo al repositorio desde controllers.
- Pass: La clave se validara por capas: DTO para formato y sanitizacion de metadatos, service para propietario y existencia, modelo para invariantes de ciclo de vida.
- Pass: Pruebas unitarias, integracion PostgreSQL/Testcontainers y E2E MockMvc se mantendran en sus paquetes separados; no se modificaran pruebas existentes.
- Pass: La autenticacion sera stateless y conservara JWT como mecanismo alternativo a API Key.
- Pass with contract update: Se protegen rutas actualmente publicas, incluido `/players` y Swagger UI, por decision explicita del usuario. El contrato canonico y sus pruebas de paridad deben documentar este cambio en el mismo cambio versionado.
- Pass: No se agrega un secreto estatico compartido; se evita que una filtracion de configuracion o entorno otorgue identidad comun a todos los clientes.

## Design Decisions

- **Clave aleatoria y formato**: generar 32 bytes con `SecureRandom`, codificar el secreto para transporte y asociarlo a un identificador selector aleatorio no secreto. El selector permite obtener el registro mediante indice sin buscar todos los hashes.
- **Persistencia segura**: guardar SHA-256 del secreto aleatorio, no el valor en claro; comparar hashes en tiempo constante. Un KDF lento no es necesario para un secreto aleatorio de 256 bits. No registrar cabeceras, valores de credenciales ni secretos.
- **Exposicion del secreto**: responder con el secreto completo solo a `POST /api-keys`; operaciones posteriores solo exponen metadatos. La expiracion es opcional; la revocacion se registra y bloquea el uso desde la siguiente solicitud.
- **Operaciones de ciclo de vida**: `POST /api-keys` crea una clave; `GET /api-keys` lista metadatos del usuario autenticado; `DELETE /api-keys/{id}` revoca una clave propia. Todas exigen JWT. Una API Key no puede crear ni revocar claves.
- **Autenticacion HTTP**: `ApiKeyAuthenticationFilter` extrae un unico valor no vacio de `x-api-key` y ejecuta antes de `JwtAuthenticationFilter`. Sin API Key, el flujo JWT continua sin cambios. Con API Key, un `ApiKeyAuthenticationToken` no autenticado que extiende `AbstractAuthenticationToken` es verificado por un provider y convertido a principal `User` con las authorities del usuario; el secreto se elimina de las credenciales autenticadas.
- **Credenciales simultaneas**: si una solicitud incluye `Authorization: Bearer ...` y `x-api-key`, responder `401` sin ejecutar ninguno de los mecanismos; no se permite precedencia silenciosa ni mezcla de identidades.
- **Rutas de autenticacion**: cualquier presencia de `x-api-key` en `POST /login` o `POST /register` se rechaza antes del controller, incluso si la clave valida. Esas rutas siguen disponibles sin API Key segun su flujo actual.
- **Autorizacion**: todas las demas rutas requieren JWT o API Key. `/api-keys/**` requiere JWT especificamente; los endpoints de Swagger UI, OpenAPI, usuarios y jugadores tambien quedan protegidos. El preflight CORS se mantendra funcional sin abrir las operaciones de negocio.
- **Manejo de errores y eventos**: la respuesta de autenticacion fallida es generica y no distingue clave desconocida, hash incorrecto, vencimiento o revocacion. Se registran creacion y rechazos con resultado y, cuando exista, selector; nunca el secreto.
- **Rotacion**: se crea una clave de reemplazo, el cliente migra y luego revoca la anterior; no se rota ni se reemplaza silenciosamente una clave activa.
- **Contrato**: `specs/006-apikey/contracts/api-keys.yaml` define las nuevas operaciones y esquemas de autenticacion. Durante implementacion se actualizara `specs/005-swagger/contracts/openapi.yaml` como contrato canonico para reflejar la nueva seguridad global y la excepcion JWT-only del ciclo de vida; Swagger UI no persistira credenciales.
- **Dependencias**: se reutilizan Spring Security, Spring Data JPA, PostgreSQL, JUnit, MockMvc y Testcontainers existentes; JCA provee `SecureRandom` y SHA-256, sin dependencia adicional.

## Project Structure

### Documentation (this feature)

```text
specs/006-apikey/
|-- spec.md
|-- plan.md
|-- research.md
|-- data-model.md
|-- quickstart.md
|-- contracts/
|   `-- api-keys.yaml
`-- tasks.md                 # Se genera en Phase 2
```

### Source Code (repository root)

```text
backend/app/src/main/java/com/dappunq/
|-- controller/               # Endpoints REST de ciclo de vida de API Keys
|-- dto/                       # Requests y respuestas sin secretos reutilizables
|-- model/                     # API Key y reglas de ciclo de vida
|-- persistence/               # Entidad, repositorio y mapper JPA
|-- security/                  # Token, provider, filtro API Key y SecurityConfig
`-- service/                   # Generacion, gestion, hasheo y auditoria segura

backend/app/src/test/java/com/dappunq/
|-- unit/                      # Reglas de dominio y generacion/hasheo
|-- security/                  # Token, provider, filtro y precedencia
|-- integration/               # Persistencia de claves con Testcontainers
`-- e2e/                       # Politica de seguridad y API con MockMvc

specs/005-swagger/contracts/openapi.yaml  # Contrato canonico actualizado en implementacion
```

**Structure Decision**: La funcionalidad pertenece al backend Spring existente y reutiliza los paquetes actuales. Se separan el modelo `ApiKey` y la entidad de persistencia `ApiKeyEntity`; el selector de clave se indexa en PostgreSQL. El contrato de API Keys se diseña junto con esta feature y se integra al contrato OpenAPI canonico al implementar.

## Complexity Tracking

No se identifican violaciones constitucionales que requieran excepcion.
