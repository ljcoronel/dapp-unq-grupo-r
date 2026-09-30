# Research: Datos de jugadores de futbol

## Decision

- Se implementará una API REST en Java 25 + Spring Boot 4 para exponer el catálogo de jugadores dentro del backend existente.
- El módulo de jugadores usará `JugadorControllerRest` para `GET /players` y `GET /players/{id}` y un `GlobalExceptionHandler` para mapear errores a respuestas HTTP consistentes.
- Los DTOs se definirán como records Java: `JugadorRequestDTO` y `JugadorResponseDTO` para separar la capa de API de la capa de persistencia.
- La fuente externa de consulta principal será Football-Data.org para datos base y WhoScored para métricas detalladas; la información se normalizará a un modelo interno único de jugador.
- La persistencia se hará en PostgreSQL local (`jdbc:postgresql://localhost:5432/dappfc`) y se conservará un histórico de snapshots con marca temporal para consultas offline y trazabilidad.
- Las pruebas de integración y E2E se ejecutarán con Testcontainers y MockMvc; no se usará el perfil local para pruebas automatizadas.

## Rationale

La constitución exige una arquitectura en capas, validación por niveles, DTOs explícitos y testing con Testcontainers. La integración con dos proveedores externos responde a la necesidad de recopilar tanto metadata básica como métricas de rendimiento, mientras que el almacenamiento histórico garantiza continuidad aunque alguno de los proveedores falle o tenga rate limiting. La decisión de separar la API y la persistencia con records y entidades evita acoplar los contratos REST a entidades JPA y hace más robusta la evolución del modelo.

## Alternatives considered

- Exponer entidades JPA directamente: descartado porque rompe la separación entre persistencia y API y dificulta la validación y el control de serialización.
- Persistir solo el último valor y no mantener historial: descartado porque la especificación exige conservar información local e histórica para consultas posteriores.
- Usar un único proveedor externo: descartado porque Football-Data.org y WhoScored aportan fuentes complementarias y no siempre cubren el mismo nivel de detalle.
- Ejecutar pruebas contra la base local: descartado por la constitución y por la necesidad de levantar PostgreSQL real con Testcontainers.

## Open questions and assumptions resolved

- Los endpoints públicos serán `GET /players` y `GET /players/{id}` con filtros por `liga`, `equipo` y `posición`.
- La respuesta incluye los campos exigidos por la especificación: id, nombre, equipo, pases, tiros, intercepciones, calificaciones, minutos jugados, goles, asistencias, tiros al arco, pases realizados, tarjetas amarillas, tarjetas rojas y rating general.
- La integridad del dominio exige identificadores únicos y registros completos; si faltan campos obligatorios, la API debe rechazar la carga con error de validación.
- El historial de sincronización se modela como snapshot del jugador con marca temporal y no como un conjunto de cambios de texto en una sola tabla.
- Si un proveedor externo falla, la API responderá desde la última información persistida local.
