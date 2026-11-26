# Especificación de Funcionalidad: Acceso público al scraping de cotizaciones

**Rama de funcionalidad**: `017-refactor-scraping`

**Creada**: 2026-11-26

**Estado**: Borrador

**Entrada**: Hacer público `POST /quotes/scrape`, sin exigir `X-API-Key`, conservando las validaciones del parámetro `numberOfPlayers` y la protección de los demás endpoints.

## Escenarios de usuario y pruebas

### Historia de usuario 1 - Ejecutar scraping sin credenciales (Prioridad: P1)

Como cliente de la API, quiero iniciar el scraping con una solicitud válida sin proporcionar credenciales, para obtener el resultado del proceso sin necesitar una API key.

**Por qué tiene esta prioridad**: El acceso directo sin credenciales es el objetivo principal de esta funcionalidad.

**Prueba independiente**: Enviar una solicitud válida a `POST /quotes/scrape` con `numberOfPlayers` y sin `X-API-Key`; comprobar que se procesa con el resultado normal del endpoint.

**Escenarios de aceptación**:

1. **Dado** un valor de `numberOfPlayers` válido, **cuando** un cliente envía `POST /quotes/scrape` sin `X-API-Key`, **entonces** la solicitud no recibe un rechazo de autenticación `401` o `403` y el scraping se procesa normalmente.
2. **Dado** un valor de `numberOfPlayers` válido, **cuando** un cliente envía la solicitud con cualquier valor de `X-API-Key`, **entonces** el valor del encabezado se ignora y la solicitud se procesa normalmente.

---

### Historia de usuario 2 - Conservar la validación del parámetro (Prioridad: P1)

Como consumidor del endpoint, quiero que `numberOfPlayers` siga sujeto a las mismas validaciones, para recibir errores claros cuando la solicitud no cumpla el contrato actual.

**Por qué tiene esta prioridad**: Hacer público el endpoint no debe permitir que solicitudes con parámetros inválidos alteren el comportamiento existente.

**Prueba independiente**: Enviar solicitudes sin el parámetro y comprobar que se conserva el valor predeterminado actual de 50; enviar un valor no numérico o no positivo y comprobar que recibe `400 Bad Request`. Enviar un valor válido y comprobar que el proceso continúa normalmente.

**Escenarios de aceptación**:

1. **Dado** que falta `numberOfPlayers`, **cuando** se envía la solicitud, **entonces** se conserva el valor predeterminado actual de 50 y se mantiene el procesamiento normal del endpoint.
2. **Dado** que `numberOfPlayers` no es numérico o no es un entero positivo, **cuando** se envía la solicitud, **entonces** se devuelve `400 Bad Request`.
3. **Dado** que `numberOfPlayers` es un entero positivo, **cuando** se envía la solicitud, **entonces** se mantiene el procesamiento normal del endpoint.

---

### Historia de usuario 3 - Mantener la protección del resto de la API (Prioridad: P1)

Como responsable de la API, quiero que el acceso público se limite a `POST /quotes/scrape`, para que el resto de los endpoints protegidos conserve su autenticación actual.

**Por qué tiene esta prioridad**: El cambio debe habilitar el caso de uso solicitado sin debilitar el control de acceso de otras operaciones.

**Prueba independiente**: Enviar solicitudes sin una API key válida a otros endpoints que actualmente la exigen y verificar que siguen siendo rechazadas según el comportamiento de autenticación existente.

**Escenarios de aceptación**:

1. **Dado** un endpoint distinto de `POST /quotes/scrape` que actualmente está protegido, **cuando** se invoca sin una API key válida, **entonces** conserva el rechazo de autenticación existente.
2. **Dado** un endpoint distinto de `POST /quotes/scrape` que actualmente está protegido, **cuando** se invoca con una API key válida, **entonces** conserva su comportamiento de autenticación existente.

### Casos borde

- La ausencia de `numberOfPlayers` debe seguir usando el valor predeterminado actual de 50, aunque la solicitud no requiera credenciales.
- Un valor no numérico o no positivo debe seguir produciendo `400 Bad Request`.
- Un valor válido acompañado por una API key inválida no debe ser rechazado por autenticación en este endpoint.
- La presencia o ausencia de `X-API-Key` no debe modificar el resultado de autenticación de `POST /quotes/scrape`.
- Ninguna otra ruta o método debe quedar exento de autenticación por este cambio.

## Requisitos

### Requisitos funcionales

- **FR-001**: El sistema MUST procesar solicitudes `POST /quotes/scrape` con un valor válido de `numberOfPlayers` sin exigir el encabezado `X-API-Key`.
- **FR-002**: En `POST /quotes/scrape`, el sistema MUST ignorar cualquier valor recibido en `X-API-Key`; su presencia, ausencia o validez no debe cambiar la autorización de la solicitud.
- **FR-003**: El sistema MUST conservar el valor predeterminado actual de 50 cuando `numberOfPlayers` está ausente y sus validaciones actuales: un valor no numérico o no entero positivo debe producir `400 Bad Request`.
- **FR-004**: Una solicitud a `POST /quotes/scrape` con un valor válido de `numberOfPlayers` MUST continuar con el comportamiento normal de scraping y su respuesta existente.
- **FR-005**: El cambio de acceso MUST limitarse a `POST /quotes/scrape`; los demás endpoints protegidos MUST conservar el esquema de autenticación por `X-API-Key` que tienen actualmente.
- **FR-006**: El cambio MUST NOT modificar la lógica interna del scraping, el formato de respuesta ni la persistencia de datos.
- **FR-007**: El cambio MUST NOT incorporar mecanismos de autenticación nuevos ni limitación de frecuencia.

## Criterios de éxito

### Resultados medibles

- **SC-001**: El 100% de las solicitudes de aceptación con `numberOfPlayers` válido a `POST /quotes/scrape` se procesa sin requerir `X-API-Key`.
- **SC-002**: El 100% de las solicitudes de aceptación con un valor válido de `numberOfPlayers` obtiene el mismo resultado de acceso tanto si omite `X-API-Key` como si incluye un valor válido o inválido.
- **SC-003**: El 100% de los casos cubiertos con `numberOfPlayers` no numérico o no entero positivo recibe `400 Bad Request`; las solicitudes sin el parámetro conservan el valor predeterminado de 50.
- **SC-004**: El 100% de las verificaciones de endpoints protegidos ajenos a `POST /quotes/scrape` conserva el resultado de autenticación esperado antes del cambio.
- **SC-005**: Al menos 9 de cada 10 clientes de prueba pueden completar una invocación válida de scraping sin configurar ni enviar credenciales.

## Supuestos

- El método y la ruta públicos son exclusivamente `POST /quotes/scrape`; otras rutas o métodos no se incluyen.
- El valor predeterminado y las reglas de validación para `numberOfPlayers` son los que ya aplica el endpoint; esta especificación no introduce ni redefine límites.
- “Procesamiento normal” significa conservar la lógica, respuesta y persistencia actuales para una solicitud con parámetros válidos.
- Los endpoints protegidos se identifican por su comportamiento actual y mantienen sus respuestas de autenticación sin cambios.
