# Feature Specification: Refactor de valuación — fórmula compuesta y superusuario

**Feature Branch**: `013-valuation-refactor`

**Created**: 2026-10-30

**Status**: Draft

**Input**: User description: "Refactor al programa. (1) El valor del token se calcula con valorToken = valorBase + (result * factorEscala), donde valorBase es el valor actual previo a la valuación (0 si nunca fue valuado) y factorEscala es una constante igual a 2. (2) El flujo de inicialización del superusuario debe ser: 1) Setearle la tenencia de todos los tokens, cada uno con valor 0. 2) Configurarle su Estrategia. 3) Valuar sus tokens (paso 3 como consecuencia directa del paso 2)."

## Clarifications

### Session 2026-10-30

- Q: ¿Cuál es el valor de `factorEscala`? → A: Constante igual a **2**; no es configurable por el usuario.
- Q: ¿Cuál es el flujo de inicialización del Superusuario? → A: (1) Crear las tenencias de todos los tokens con valor 0; (2) configurar la estrategia del Superusuario como ACTUAL; (3) la valuación de los tokens es consecuencia directa del paso 2 (mismo mecanismo que al configurar la primera estrategia de cualquier usuario).

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Valor de token acumulativo al revaluar (Priority: P1)

Como Usuario, quiero que cada nueva valuación tome como punto de partida el valor actual de mis tokens, de modo que el valor resultante refleje tanto el rendimiento histórico acumulado como el rendimiento relativo calculado en la nueva ejecución.

**Why this priority**: Es el cambio central de comportamiento de negocio. Sin esta regla, los usuarios pierden el historial de valor acumulado en cada revaluación.

**Independent Test**: Dado un token con valor previo conocido, ejecutar una valuación y verificar que el nuevo valor unitario sea igual a `valorBase + (result * 2)` y no sea simplemente `result`.

**Acceptance Scenarios**:

1. **Given** un Usuario con al menos un token cuyo valor unitario actual es 0 (nunca fue valuado), **When** se ejecuta una valuación, **Then** el nuevo valor unitario del token es igual a `0 + (result * 2)`, donde `result` es el valor normalizado calculado por la estrategia del Usuario para ese jugador.
2. **Given** un Usuario con al menos un token cuyo valor unitario actual es mayor que 0 (ya fue valuado previamente), **When** se ejecuta una nueva valuación, **Then** el nuevo valor unitario del token es igual a `valorBase + (result * 2)`, donde `valorBase` es el valor unitario que tenía el token inmediatamente antes de esta ejecución.
3. **Given** dos Usuarios con estrategias distintas y el mismo jugador en tenencia con igual valor previo, **When** ambos ejecutan una valuación, **Then** cada Usuario puede obtener un valor final diferente porque sus `result` difieren.
4. **Given** que se ejecuta una valuación manual (`POST /quotes/recalculate`) y luego una dominical, **When** se comparan los valores, **Then** la segunda valuación parte del valor resultante de la primera y aplica la fórmula acumulativa nuevamente.

---

### User Story 2 — factorEscala fijo en la fórmula de valuación (Priority: P1)

Como desarrollador, quiero que el sistema aplique la constante `factorEscala = 2` al multiplicar `result` antes de sumarlo a `valorBase`, de modo que la magnitud del incremento por período de valuación sea determinista y uniforme.

**Why this priority**: `factorEscala` es un parámetro central de la nueva fórmula; su valor fijo hace que la fórmula sea completamente determinista.

**Independent Test**: Ejecutar una valuación y verificar matemáticamente que el nuevo valor == `valorBase + (result * 2)` en todos los tokens afectados.

**Acceptance Scenarios**:

1. **Given** el sistema con `factorEscala = 2`, **When** se calcula el valor de un token cuyo `result` es 0.50 y `valorBase` es 3.00, **Then** el nuevo `valorToken` es exactamente 4.00 (= 3.00 + 0.50 × 2).
2. **Given** que `result` = 0.00 para un jugador (todas sus métricas en el mínimo normalizado), **When** se ejecuta la valuación, **Then** el valor unitario del token permanece igual a `valorBase` (sin variación).

---

### User Story 3 — Superusuario inicializado correctamente al arrancar el sistema (Priority: P1)

Como Superusuario, quiero que mi cuenta tenga tenencias, estrategia y tokens valuados desde el primer arranque del sistema, sin necesidad de intervención manual.

**Why this priority**: Actualmente el Superusuario no tiene estrategia y sus tenencias muestran valor 0, lo que impide que el inventario inicial del mercado tenga un valor visible y funcional.

**Independent Test**: Reiniciar el sistema, consultar `GET /users/{idSuperUser}/strategy` y verificar que responde una estrategia con estado ACTUAL; consultar `GET /users/{idSuperUser}/holdings` y verificar que al menos un token muestra un valor unitario distinto de 0.

**Acceptance Scenarios**:

1. **Given** que el Superusuario no existe en la base de datos, **When** el sistema arranca, **Then** se crea el Superusuario, se le asigna la tenencia de los 100 tokens de cada jugador con valor unitario inicial = 0, se le configura la estrategia predeterminada con estado ACTUAL y sus tokens son valuados inmediatamente aplicando la nueva fórmula.
2. **Given** que el Superusuario existe pero no tiene ninguna estrategia con estado ACTUAL, **When** el sistema arranca, **Then** se le configura la estrategia predeterminada con estado ACTUAL y sus tokens son valuados inmediatamente.
3. **Given** que el Superusuario ya tiene una estrategia con estado ACTUAL, **When** el sistema arranca de nuevo, **Then** la estrategia existente no es modificada ni duplicada y no se ejecuta una valuación redundante.
4. **Given** que el Superusuario tiene su estrategia activa y al menos un jugador con métricas registradas, **When** se consulta `GET /users/{idSuperUser}/strategy`, **Then** la respuesta contiene la estrategia con estado ACTUAL y sus pesos configurados.
5. **Given** que el Superusuario tiene su estrategia activa y al menos un jugador con métricas registradas, **When** se consulta `GET /users/{idSuperUser}/holdings`, **Then** al menos un token muestra un valor unitario mayor que 0.

---

### Edge Cases

- Si `result` es 0.00 para todos los jugadores del Superusuario (métricas en mínimo normalizado o sin datos suficientes), todos los tokens conservan su `valorBase` (que es 0 en la primera valuación), resultando en valor 0; esto es correcto y esperado.
- Si el Superusuario ya existe con estrategia ACTUAL al reiniciar el sistema, el flujo de inicialización detecta la estrategia existente y no la recrea ni revalúa redundantemente.
- Si no hay jugadores registrados al momento de inicializar el Superusuario, el paso de tenencias queda vacío y el paso de valuación no tiene tokens que valuar; no debe producirse error.
- Si `valorBase` es 0 (primera valuación), la fórmula se reduce a `result * 2`; el resultado puede ser 0.00 si `result` es 0.
- Si una valuación posterior ocurre y el jugador no tiene métricas (`result = 0`), el token conserva su `valorBase` acumulado hasta ese momento.
- Los valores en créditos se almacenan con dos decimales; el redondeo debe aplicarse al resultado final de la fórmula.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST calcular el valor final de cada token mediante la fórmula `valorToken = valorBase + (result * factorEscala)`, donde:
  - `valorBase` es el valor unitario almacenado para ese token inmediatamente antes de la ejecución de la valuación actual;
  - `result` es el valor normalizado producido por el algoritmo de la estrategia para el jugador dado;
  - `factorEscala` es una constante igual a **2**.
- **FR-002**: El sistema MUST usar `valorBase = 0` cuando el token nunca haya sido valuado previamente (valor unitario almacenado es 0 o nulo).
- **FR-003**: El sistema MUST leer `valorBase` del valor unitario persistido del token en el momento de iniciar la ejecución de la valuación, antes de aplicar la fórmula.
- **FR-004**: La fórmula MUST aplicarse en toda ejecución de valuación: la ejecución dominical automática, la ejecución manual (`POST /quotes/recalculate`) y la valuación inmediata que ocurre al configurar la primera estrategia de un Usuario.
- **FR-005**: El sistema MUST inicializar al Superusuario al arrancar la aplicación siguiendo el siguiente orden:
  1. Crear las tenencias del Superusuario para todos los jugadores existentes con valor unitario inicial = 0, si no existen.
  2. Configurar la estrategia del Superusuario como ACTUAL con los pesos predeterminados, si no existe ninguna estrategia ACTUAL.
  3. Valuar los tokens del Superusuario como consecuencia directa de la configuración de la estrategia (mismo mecanismo que al configurar la primera estrategia de cualquier Usuario).
- **FR-006**: La estrategia predeterminada del Superusuario MUST ser la Estrategia A con pesos: `goals=30%`, `assists=25%`, `passSuccessPercentage=15%`, `aerialsWon=15%`, `timesManOfTheMatch=15%`.
- **FR-007**: `GET /users/{idSuperUser}/strategy` MUST devolver la estrategia del Superusuario con estado ACTUAL tras el arranque del sistema.
- **FR-008**: `GET /users/{idSuperUser}/holdings` MUST devolver los tokens del Superusuario con el valor unitario resultante de la valuación aplicada en el paso 3 de la inicialización.
- **FR-009**: La inicialización del Superusuario MUST ser idempotente: si la estrategia ACTUAL ya existe, los pasos 2 y 3 no deben ejecutarse ni producir duplicados.

### Key Entities

- **valorBase**: Valor unitario almacenado en el token inmediatamente antes de la ejecución de la valuación. Es 0 cuando el token nunca fue valuado.
- **result**: Valor normalizado producido por el algoritmo de la estrategia (pesos × métricas normalizadas del jugador). Antes del refactor era el resultado final; ahora es un componente intermedio de la fórmula.
- **factorEscala**: Constante igual a **2**, multiplicada por `result` antes de sumarse a `valorBase`.
- **valorToken**: Valor unitario final resultante de la fórmula; es el valor que se almacena en la tenencia y se devuelve en las consultas.
- **Superusuario**: Actor especial cuyo flujo de inicialización garantiza tenencias, estrategia y valuación activas desde el primer arranque.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El 100% de las valuaciones producen un `valorToken` que satisface matemáticamente `valorToken = valorBase + (result * 2)`, verificable comparando el valor previo almacenado, el `result` calculado y el valor final persistido.
- **SC-002**: El 100% de los tokens valuados por primera vez (con `valorBase = 0`) tiene como nuevo valor exactamente `result * 2`.
- **SC-003**: Tras el arranque del sistema, `GET /users/{idSuperUser}/strategy` devuelve una estrategia con estado ACTUAL en el 100% de los casos, independientemente de cuántas veces se reinicie el sistema.
- **SC-004**: Tras el arranque del sistema y con al menos un jugador con métricas registradas, `GET /users/{idSuperUser}/holdings` devuelve al menos un token con valor unitario > 0 en el 100% de los casos.
- **SC-005**: La inicialización del Superusuario no produce más de un registro de estrategia ACTUAL por arranque del sistema (idempotencia verificable contando los registros de estrategia ACTUAL del Superusuario antes y después de cada reinicio).

## Assumptions

- `factorEscala` es una constante fija igual a **2** definida en el código; no es configurable por el Usuario ni administrable vía API.
- `valorBase` se lee del estado persistido del token en el momento de iniciar la ejecución de la valuación, antes de aplicar la fórmula.
- El valor `result` producido por la estrategia ya está normalizado en el rango [0.00, 1.05] (puede superar 1.00 en Estrategia B por el bonus de Defender); con `factorEscala = 2` el incremento máximo por valuación es ~2.10.
- La estrategia predeterminada del Superusuario es Estrategia A con pesos `goals=30`, `assists=25`, `passSuccessPercentage=15`, `aerialsWon=15`, `timesManOfTheMatch=15`, alineada con la implementación existente en `SuperuserInitializationService`.
- La valuación de las tenencias del Superusuario en el paso 3 de inicialización utiliza el mismo servicio de valuación (`HoldingsValuationService.revalue`) que para cualquier otro Usuario al configurar su primera estrategia.
- Los valores en créditos se almacenan con hasta dos decimales; el redondeo se aplica al resultado final de la fórmula.
- Las transferencias, compras, ventas y cualquier otra forma de asignar tokens a Usuarios quedan fuera del alcance de esta especificación.
- La autenticación, autorización general y administración de jugadores existentes se reutilizan de las funcionalidades actuales.
