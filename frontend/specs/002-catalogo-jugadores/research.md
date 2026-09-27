# Research: Catálogo de jugadores por liga

## Decision: Reutilizar el cliente HTTP existente

- **Decision**: La consulta se encapsula en un servicio de jugadores y usa el cliente Axios `src/services/apiClient.js` con `GET /players`.
- **Rationale**: El frontend ya centraliza la `baseURL` (`http://localhost:8080`) y encabezados comunes en ese cliente. Así se conserva la URL base y no se introduce otro cliente HTTP.
- **Alternatives considered**: Crear una instancia Axios independiente o hacer la solicitud directamente desde Home; ambas opciones duplican configuración o mezclan presentación con acceso a red.

## Decision: No enviar JWT a la consulta pública

- **Decision**: El servicio no agrega encabezado `Authorization` ni consulta el contexto de autenticación para construir la petición.
- **Rationale**: La acción del usuario define que `GET /players` no requiere token. El cliente actual no tiene un interceptor que adjunte el token. Home puede seguir dentro de la ruta protegida vigente sin que el endpoint dependa de ese token.
- **Alternatives considered**: Enviar el JWT disponible desde el contexto o agregar un interceptor; no se necesita y haría que la solicitud dependa de credenciales.

## Decision: Consumir listas agrupadas conservando el orden de la respuesta

- **Decision**: El payload se procesa como cinco arreglos de jugadores, que se corresponden en orden con las ligas requeridas. Cada arreglo alimenta una sección independiente.
- **Rationale**: La respuesta descrita es una lista de cinco listas y el contrato del endpoint disponible describe posiciones fijas. Usar el orden evita aplanar la respuesta y mantener el agrupamiento por liga.
- **Alternatives considered**: Aplanar jugadores y reagruparlos por `liga.codigo`; la descripción no proporciona los cinco códigos ni es necesario para respetar el agrupamiento entregado por el servidor.

## Decision: Representar valores ausentes en la presentación

- **Decision**: Para cada campo presentado, usar `-` si el valor es `null` o no está disponible; preservar valores existentes, incluido `0`.
- **Rationale**: Cumple la especificación y distingue la ausencia de datos de una estadística con valor cero.
- **Alternatives considered**: Mostrar `null`, texto técnico, o reemplazar cualquier valor falsy; las primeras opciones son poco claras para el usuario y la última oculta ceros válidos.

## Decision: Mantener estados visibles de la solicitud

- **Decision**: Home presenta carga, error y respuesta sin datos en español; no disimula fallos con una lista vacía con forma de éxito.
- **Rationale**: La constitución exige manejo explícito de fallos y feedback amigable. En la integración el endpoint documenta la posibilidad de respuesta de servicio no disponible.
- **Alternatives considered**: Silenciar el error o tratar la falla como respuesta vacía; esto confunde un fallo de red/servidor con un catálogo genuinamente vacío.

## Data contract note

La especificación pide diez jugadores por liga, mientras que el contrato de endpoint disponible garantiza cinco grupos con un máximo de diez jugadores y permite grupos vacíos. La interfaz debe mostrar todos los jugadores devueltos y no inventar filas; confirmar con el backend/datos que cada grupo contiene diez registros para satisfacer el criterio de diez filas. La respuesta reducida incluida en la solicitud es un ejemplo de estructura, no una muestra de volumen esperado.
