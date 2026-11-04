# Investigación: Mercado de órdenes de tokens

## 1. Arquitectura y ubicación de la lógica

**Decisión:** agregar `Orden` como modelo de dominio sin anotaciones, entidades
JPA separadas en `persistence`, servicios transaccionales y DTOs/controladores
en sus paquetes existentes.

**Rationale:** la Constitución exige que el modelo contenga invariantes y que el
Repository haga el mapeo entre modelo y persistencia. El backend ya utiliza
`Holding`/`HoldingEntity`, `ValuationControllerRest` y servicios Spring con
`@Transactional`, por lo que este diseño extiende el patrón sin mezclar capas.

**Alternativas consideradas:** usar la entidad JPA como dominio (rechazado por
acoplamiento y violación de la Constitución) o poner el matching en el
Controller (rechazado por mezclar transporte y negocio).

## 2. Persistencia de saldos, tenencias y órdenes

**Decisión:** representar los créditos y tokens bloqueados explícitamente
(`saldo disponible`, `saldo bloqueado`, `tokens disponibles` y `tokens
bloqueados`) y persistir cada orden con estado, fechas y datos de la operación.
Las columnas monetarias usarán precisión/escala decimal y las cantidades serán
enteras.

**Rationale:** reservar no puede inferirse sólo desde una consulta de órdenes
sin riesgo de doble gasto concurrente. El bloqueo explícito permite validar,
reservar, liberar y transferir dentro de una única transacción.

**Alternativas consideradas:** calcular bloqueos como suma de órdenes pendientes
(rechazado por mayor complejidad de concurrencia) y usar `double` (rechazado por
errores de precisión monetaria).

## 3. Matching y liquidación

**Decisión:** el servicio de órdenes crea la orden, intenta primero la liquidez
del Superusuario para compras y luego busca una orden contraria compatible por
usuario distinto, jugador, cantidad y rangos inclusivos. El matching y la
liquidación se ejecutan bajo `@Transactional`; las órdenes se protegen al
seleccionarse para que una coincidencia concurrente no las consuma dos veces.
El precio se elige con el criterio determinista de la especificación y siempre
se redondea a dos decimales.

**Rationale:** mantiene la atomicidad requerida y hace que la liberación del
excedente reservado de una compra sea parte de la misma unidad de trabajo.

**Alternativas consideradas:** ejecuciones parciales (fuera de alcance),
matching asíncrono (no garantiza que la respuesta refleje el estado final) y
reintentos sin bloqueo (susceptibles de doble liquidación).

## 4. Contrato y seguridad de los endpoints

**Decisión:** `POST /orders/buy` y `POST /orders/sell` reciben `playerId`,
`quantity`, `price` y `tolerance`; el usuario emisor se obtiene de la
autenticación existente. `GET /users/{id}/transactions` devuelve sólo órdenes
Ejecutadas y `GET /users/{id}/orders` sólo órdenes Pendientes, con autorización
para que cada usuario consulte únicamente sus propios datos.

**Rationale:** evita confiar en un `userId` manipulable en el body y conserva la
separación entre historial de operaciones y órdenes abiertas. Cada response
incluye `status`, `issuedAt`, `executedAt` o `cancelledAt` según corresponda.

**Alternativas consideradas:** devolver una fecha extra nula para todos los
estados (rechazado: el contrato debe expresar qué fecha aplica) y permitir
consultar cualquier usuario (rechazado por privacidad y FR-022/SC-006).

## 5. Estrategia de pruebas

**Decisión:** cubrir invariantes de `Orden` con unit tests, reservas y
liquidaciones con integración PostgreSQL/Testcontainers, endpoints con MockMvc
en `com.dappunq.e2e` y ejemplos operativos en Postman.

**Rationale:** coincide con la Constitución y con la configuración Gradle
existente (`junit-jupiter`, `testcontainers-postgresql`, `spring-boot-starter-test`).
Docker Desktop es un prerrequisito para los tests de integración.

**Alternativas consideradas:** probar sólo con mocks (no detecta restricciones,
locks ni mapeo JPA) o sólo con pruebas manuales (no es reproducible).
