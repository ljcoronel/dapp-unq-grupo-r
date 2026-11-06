# Data Model: Refactor de valuación — fórmula compuesta y superusuario

> [!NOTE]
> Este refactor **no introduce cambios de esquema de base de datos**. Las tablas `tenencias`, `estrategias_config`, `historial_valuaciones` y `valuaciones` mantienen sus columnas actuales. Los cambios son exclusivamente de lógica de negocio en la capa Model y Service.

---

## Entidades existentes afectadas

### `HoldingEntity` (`tenencias`)

**Cambio**: Ninguno en esquema. El campo `valor_unitario` ya almacena 2 decimales.  
**Semántica nueva**: `valor_unitario` ahora actúa como `valorBase` en la próxima ejecución de valuación.

| Columna | Tipo | Constraint | Descripción |
|---------|------|-----------|-------------|
| `id` | BIGINT | PK, auto | Identificador |
| `usuario_id` | BIGINT | FK → `usuarios` | Usuario propietario |
| `jugador_id` | BIGINT | FK → `jugadores` | Jugador tokenizado |
| `tokens` | INT | NOT NULL | Cantidad de tokens |
| `valor_unitario` | DECIMAL(15,2) | NOT NULL, ≥ 0 | **valorBase para la próxima valuación** |
| `valor_total` | DECIMAL(15,2) | NOT NULL, ≥ 0 | `valor_unitario × tokens` |

**Invariante de negocio**:
- Al crear: `valor_unitario = 0.00` (nunca valuado).
- Al valuar: `valor_unitario_nuevo = valor_unitario_anterior + (result × 2)`, redondeado HALF_UP a 2 decimales.
- `valor_total = valor_unitario × tokens`, redondeado HALF_UP.

---

### `PlayerValuationCalculator` (Model — no persiste)

**Cambio**: Nueva firma + fórmula compuesta.

```
calculate(playerSnapshot, strategyConfig, allSnapshots, valorBase) → valorToken
```

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `playerSnapshot` | `PlayerSnapshot` | Métricas del jugador a valuar |
| `strategyConfig` | `StrategyConfig` | Estrategia y pesos activos |
| `allSnapshots` | `List<PlayerSnapshot>` | Todos los jugadores (para normalización) |
| `valorBase` | `BigDecimal` | Valor unitario previo del token (≥ 0; 0 si nunca valuado) |
| **retorno** | `BigDecimal` | `valorBase + (result × FACTOR_ESCALA)`, scale 2, HALF_UP |

**Constante**:
```java
public static final BigDecimal FACTOR_ESCALA = BigDecimal.valueOf(2);
```

**Algoritmo**:
1. Normalizar cada métrica del jugador en rango [0,1] respecto al snapshot global.
2. Calcular `result = Σ(weight_i × normalized_i)`.
3. Si `ESTRATEGIA_B` y posición = "Defender": `result += 0.05`.
4. Aplicar fórmula: `valorToken = valorBase + (result × 2)`.
5. Redondear `valorToken` con HALF_UP a 2 decimales.
6. Si `valorBase` o snapshot nulo/vacío → retornar `0.00`.

---

### `StrategyConfigEntity` (`estrategias_config`)

**Cambio**: Ninguno. Se usa tal como está.  
**Rol en el flujo del Superusuario**: La existencia de un registro con `estado = ACTUAL` para el Superusuario determina si los pasos 2 y 3 de inicialización se ejecutan.

| Columna | Tipo | Descripción relevante |
|---------|------|-----------------------|
| `id` | BIGINT | PK |
| `usuario_id` | BIGINT | FK → `usuarios` |
| `estrategia` | VARCHAR | `ESTRATEGIA_A` / `ESTRATEGIA_B` |
| `pesos` | TEXT | JSON de pesos |
| `estado` | VARCHAR | `ACTUAL` / `PENDIENTE` / `SIN_ESTRATEGIA` |
| `vigente_desde` | TIMESTAMP | Timestamp de activación |

---

## Flujo de inicialización del Superusuario

```
ApplicationRunner.run()
  │
  ├─ Paso 1: InitialHoldingsService.initializeHoldingsForAllPlayers()
  │    Para cada jugador en DB:
  │      si no existe HoldingEntity(superuserId, playerId) → crear con valor_unitario=0.00
  │
  ├─ Paso 2: SuperuserInitializationService.initializeSuperuserStrategy()
  │    si no existe StrategyConfigEntity(superuserId, ACTUAL):
  │      crear con ESTRATEGIA_A, pesos predeterminados, estado=ACTUAL
  │      → strategyCreated = true
  │    si ya existe:
  │      → strategyCreated = false
  │
  └─ Paso 3 (condicional): if (strategyCreated)
       HoldingsValuationService.revalue(superuserId, estrategiaConfig, ExecutionType.MANUAL)
         Para cada HoldingEntity del superusuario:
           valorBase = h.getValorUnitario()   ← 0.00 en primer arranque
           result    = calculator.calculate(snapshot, config, allSnapshots, valorBase)
           h.valuate(result)
           guardar HoldingEntity + ValuationHistoryEntity
```

**Idempotencia garantizada**:
- Paso 1: `findByUserIdAndPlayerId` → sólo crea si no existe.
- Paso 2: `findFirstByUserIdAndEstado(ACTUAL)` → sólo crea si no existe.
- Paso 3: No se ejecuta si la estrategia ya existía → sin valuación redundante.

---

## Reglas de validación

| Regla | Capa | Descripción |
|-------|------|-------------|
| `valorBase ≥ 0` | Model (`HoldingEntity.valuate()`) | Lanza `ValuationDomainException` si negativo |
| `valorToken ≥ 0` | Model (`PlayerValuationCalculator`) | `result ≥ 0` siempre; `valorBase ≥ 0` → `valorToken ≥ 0` |
| `tokens ≥ 0` | Model (`HoldingEntity` constructor) | Lanza `ValuationDomainException` |
| `factorEscala = 2` | Model (`PlayerValuationCalculator`) | Constante, no configurable |
| `valorBase = 0` si nunca valuado | Service (`HoldingsValuationService`) | `HoldingEntity` nuevo tiene `valorUnitario = 0.00` |

---

## Transiciones de estado del token de un jugador

```
[CREADO]
valor_unitario = 0.00
    │
    │  Primera valuación
    │  valorToken = 0 + (result₁ × 2) = result₁ × 2
    ▼
[VALUADO_1]
valor_unitario = result₁ × 2
    │
    │  Segunda valuación
    │  valorToken = (result₁ × 2) + (result₂ × 2)
    ▼
[VALUADO_2]
valor_unitario = (result₁ + result₂) × 2
    │
    │  ...acumula en cada ejecución...
    ▼
[VALUADO_N]
valor_unitario = (result₁ + ... + resultN) × 2
```
