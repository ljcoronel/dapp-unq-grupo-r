# Investigación: Protección JWT de órdenes

## Decisión: reutilizar la autenticación JWT existente

`JwtAuthenticationFilter` ya lee `Authorization: Bearer <token>`, valida firma,
vigencia y existencia del usuario mediante `UserService`, y coloca un
`UserPrincipal` en el `SecurityContext`. `SecurityConfig` ya es stateless y devuelve
401 para fallos de autenticación. La protección se implementará cambiando únicamente
las reglas de `POST /orders/buy` y `POST /orders/sell` a `authenticated()`.

**Alternativas consideradas:** crear un filtro nuevo por endpoint (duplicaría
validación y rompería la configuración común); usar sesiones (contradice el diseño
stateless); validar el JWT dentro de cada servicio (mezcla seguridad con negocio).

## Decisión: atribuir la orden al principal, no al `usuarioId` confiado

El request actual contiene `usuarioId` y los servicios lo usan para validar usuario,
reservar activos y crear la orden. El controller conservará el campo para no romper
el cuerpo existente, pero obtendrá el ID desde `UserPrincipal`. Un ID distinto al
principal se rechazará con el mecanismo de no autorización existente, sin ejecutar el
servicio. Así se cumple que un JWT válido no pueda crear una orden para otro usuario.

**Alternativas consideradas:** eliminar `usuarioId` (cambio incompatible del contrato);
sobrescribir silenciosamente el ID enviado (oculta errores del consumidor y conserva
una entrada engañosa); permitirlo (vulnera SC-005). Exponer el ID en
`UserPrincipal` es preferible a una consulta adicional por nombre porque el principal
ya contiene el modelo de usuario cargado por el filtro.

## Decisión: mantener el contrato OpenAPI canónico en specs/005-swagger

`app/build.gradle` copia `backend/specs/005-swagger/contracts/openapi.yaml` al
recurso estático durante `processResources`. Ese archivo recibirá los paths de buy y
sell, su `security` Bearer JWT, la respuesta 401, y los esquemas de request/response
necesarios. El archivo estático se actualizará ejecutando la tarea de copia, no como
fuente independiente.

**Alternativas consideradas:** editar solo `app/src/main/resources/static/openapi.yaml`
(se perdería en el siguiente build); introducir un segundo contrato por feature
(crearía dos fuentes de verdad).

## Decisión: probar la cadena JWT real con MockMvc

Las pruebas E2E existentes usan `@SpringBootTest`, `MockMvc`, `springSecurity()` y
PostgreSQL Testcontainers. Se conservará esa configuración y se generarán tokens con
`JwtService` para verificar el filtro real, además de comprobar que los rechazos no
crean órdenes ni modifican reservas.

**Alternativas consideradas:** `with(user(...))` (omite el parsing del JWT);
tests unitarios únicamente del filtro (no prueban autorización de rutas ni efectos de
orden); mocks de repositorio (no prueban transacciones y reservas).

## Decisión: respuesta uniforme para autenticación no válida

Se mantendrá el handler 401 común y se evitarán mensajes que revelen si existe el
usuario, sus fondos, tenencias o la orden. El cambio puede corregir el texto
actualmente específico de API key para que sea genérico de autorización, sin alterar
el status ni el contrato de errores funcionales.
