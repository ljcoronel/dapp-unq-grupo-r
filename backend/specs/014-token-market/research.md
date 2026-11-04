# Investigación: Mercado de órdenes de tokens

## 1. Arquitectura y ubicación de la lógica

**Decisión:** agregar `Orden` como modelo de dominio sin anotaciones, entidades
JPA separadas en `persistence`, servicios transaccionales y DTOs/controladores
en sus paquetes existentes.

**Rationale:** la Constitución exige que el modelo contenga invariantes y que el
Repository haga el mapeo entre modelo y persistencia. El backend ya utiliza
`Holding`/`HoldingEntity`, `StrategyConfigurationService`,
`ValuationControllerRest` y servicios Spring con `@Transactional`, por lo que
este diseño extiende el patrón sin mezclar capas. Strategy y Holding son
dependencias existentes y quedan fuera de la creación de entidades y modelos
de esta feature.

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

## 4. Integración con Strategy y Holding

**Decisión:** validar una Strategy activa al crear cualquier orden. Durante la
liquidación, una compra incrementa la Holding existente conservando su valor
unitario; si no existe, crea la Holding usando el valor producido por la
Strategy activa del comprador y el valor vigente del jugador, nunca el precio
liquidado.

**Rationale:** FR-003 y FR-024–FR-026 agregan reglas de negocio al flujo de
órdenes, pero no requieren duplicar las capacidades ya implementadas. La
actualización de la Holding y la orden debe pertenecer a la misma transacción.

**Alternativas consideradas:** usar el precio liquidado como valor unitario
(rechazado explícitamente por la especificación), promediar valores existentes
(rechazado) o crear una segunda implementación de Strategy/Holding (rechazado
por duplicación).

## 5. Contrato y seguridad de los endpoints

**Decisión:** `POST /orders/buy` y `POST /orders/sell` reciben `usuarioId`,
`playerId`, `quantity`, `price` y `tolerance`. Los cuatro endpoints son
públicos en esta feature: no se agregan filtros JWT ni API key. `GET
/users/{id}/transactions` devuelve sólo órdenes Ejecutadas del id indicado y
`GET /users/{id}/orders` sólo órdenes Pendientes del id indicado.

**Rationale:** sin autenticación el usuario debe estar explícito en la creación;
el Service debe resolver que exista, y las consultas filtran estrictamente por
el identificador solicitado. Cada response incluye `status`, `issuedAt`,
`executedAt` o `cancelledAt` según corresponda.

**Alternativas consideradas:** obtener el usuario desde un principal JWT
(rechazado por el alcance actualizado), aceptar usuario sólo desde headers
(rechazado por ser implícito) y devolver una fecha extra nula para todos los
estados (rechazado: el contrato debe expresar qué fecha aplica).

## 6. Estrategia de pruebas

**Decisión:** cubrir invariantes de `Orden` con unit tests, reservas y
liquidaciones con integración PostgreSQL/Testcontainers, endpoints con MockMvc
en `com.dappunq.e2e` y ejemplos operativos en Postman.

**Rationale:** coincide con la Constitución y con la configuración Gradle
existente (`junit-jupiter`, `testcontainers-postgresql`, `spring-boot-starter-test`).
Docker Desktop es un prerrequisito para los tests de integración.

**Alternativas consideradas:** probar sólo con mocks (no detecta restricciones,
locks ni mapeo JPA) o sólo con pruebas manuales (no es reproducible).
