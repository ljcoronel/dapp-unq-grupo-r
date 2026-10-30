# Research: Refactor de valuación — fórmula compuesta y superusuario

## 1. Cómo leer `valorBase` antes de calcular el nuevo valor

### Pregunta
¿Dónde y cómo se debe leer el `valorBase` de cada `HoldingEntity` para garantizar que refleje el valor persistido *antes* de la ejecución actual?

### Decisión
**Leer `h.getValorUnitario()` directamente del `HoldingEntity` cargado desde la base de datos al inicio de la iteración, antes de llamar a `calculator.calculate()`.**

### Rationale
- `HoldingsValuationService.revalue()` y `WeeklyValuationService.execute()` ya cargan los `HoldingEntity` desde el repositorio al comienzo (`holdings.findByUserId(userId)` / `holdingRepository.findByUserId(userId)`). El `valorUnitario` en esa entidad refleja exactamente el valor persistido previo.
- Dentro de la misma transacción `@Transactional`, las escrituras (`holdings.save(h)`) ocurren *después* de la lectura y el cálculo, por lo que no hay riesgo de contaminación entre iteraciones mientras no se haga `flush()` intermedio (comportamiento por defecto de Hibernate: flush al commit).
- **Nota de implementación**: En `WeeklyValuationService.execute()` el bucle recorre todos los usuarios activos. Dado que todos los `HoldingEntity` del mismo usuario se cargan de una vez y se modifican secuencialmente *sin compartir referencias*, el `valorBase` de cada token es el correcto (el del estado pre-transacción).

### Alternativas consideradas
- Usar una consulta explícita `SELECT valor_unitario FROM tenencias WHERE id = ?` antes de cada cálculo: rechazada por ser redundante — el valor ya está en memoria cargado al inicio del método.
- Capturar el `valorBase` en un `Map<Long, BigDecimal>` indexado por `playerId` antes del bucle: válida como refactoring defensivo, pero innecesaria dado el comportamiento transaccional actual.

---

## 2. Dónde vive la constante `factorEscala`

### Pregunta
¿En qué capa debe definirse `FACTOR_ESCALA = 2`?

### Decisión
**Constante estática en `PlayerValuationCalculator` (capa Model).**

```java
public class PlayerValuationCalculator {
    public static final BigDecimal FACTOR_ESCALA = BigDecimal.valueOf(2);
    // ...
}
```

### Rationale
- `PlayerValuationCalculator` es el objeto de dominio que encapsula la lógica de cálculo (principio III — Rich Model).
- Al ser `static final`, es visible en tests sin instanciar la clase, y accesible como referencia documental.
- La spec confirma que `factorEscala` no es configurable, por lo que no requiere inyección de Spring ni propiedad en `application.properties`.

### Alternativas consideradas
- Propiedad en `application.properties` (`valuation.factor.escala=2`): rechazada — la spec indica que es una constante no configurable.
- Enum `ValuationConstants`: aceptable pero over-engineering para una única constante.

---

## 3. Nueva firma de `calculate()` vs lógica de composición en el Service

### Pregunta
¿Debe `PlayerValuationCalculator.calculate()` recibir `valorBase` como parámetro y devolver `valorToken` ya compuesto, o debe mantenerse devolviendo `result` y que el Service aplique la fórmula?

### Decisión
**`PlayerValuationCalculator.calculate()` recibe `valorBase` como parámetro adicional y devuelve `valorToken = valorBase + (result * 2)` directamente.**

```java
public BigDecimal calculate(PlayerSnapshot p, StrategyConfig c, List<PlayerSnapshot> snapshot, BigDecimal valorBase)
```

### Rationale
- La fórmula `valorToken = valorBase + (result * factorEscala)` es una regla de negocio pura → pertenece al Model.
- Si el Service aplica la suma, la lógica de negocio se filtra a la capa de servicio (violación del principio III).
- Un parámetro `valorBase` con valor por defecto `BigDecimal.ZERO` mantiene la retrocompatibilidad conceptual para casos de primera valuación.
- Los tests unitarios de `PlayerValuationCalculatorTest` verifican directamente la fórmula completa, reduciendo el riesgo de inconsistencias entre capas.

### Alternativas consideradas
- Mantener `calculate()` devolviendo `result` y que el Service aplique la suma: rechazada — dispersa lógica de negocio fuera del Model.
- Método overloaded `calculate(p, c, snapshot)` que delega a `calculate(p, c, snapshot, ZERO)`: aceptable como compatibilidad, pero innecesario dado que todos los call sites conocen el `valorBase`.

---

## 4. Flujo de inicialización del Superusuario y punto de entrada

### Pregunta
¿Qué componente debe orquestar el flujo 1→2→3 del Superusuario y cuándo se ejecuta al arrancar la aplicación?

### Decisión
**`SuperuserInitializationService` es el orquestador. Se expone un método `initializeSuperuser()` que ejecuta los tres pasos en orden. Se llama desde un `ApplicationRunner` o `CommandLineRunner` con `@Order` bajo en `AppCommandConfig` (o en un `@Configuration` dedicado).**

### Flujo concreto

```
1. initializeHoldingsForAllPlayers()   → InitialHoldingsService (ya existente, idempotente)
2. initializeSuperuserStrategy()       → SuperuserInitializationService (ya existente, idempotente)
3. holdingsValuationService.revalue()  → Se llama SOLO si el paso 2 creó una nueva estrategia ACTUAL
```

**Idempotencia**: El paso 3 sólo debe ejecutarse si en el paso 2 se creó una estrategia nueva. Si ya existía una estrategia ACTUAL (idempotencia), los pasos 2 y 3 se saltan (FR-009).

### Rationale
- `InitialHoldingsService.initializeHoldingsForAllPlayers()` ya existe y es idempotente.
- `SuperuserInitializationService.initializeSuperuserStrategy()` ya existe y es idempotente.
- Actualmente **no existe un método que invoque `holdingsValuationService.revalue()` tras configurar la estrategia del Superusuario** → esto es el gap a cerrar.
- Un `ApplicationRunner` con `@ConditionalOnProperty` negado (i.e., cuando no se está en modo `rotate-scraping-api-key`) garantiza que el flujo ocurra en el startup normal.

### Alternativas consideradas
- Disparar la valuación desde `initializeSuperuserStrategy()` directamente: viable, pero acopla la lógica de inicialización de estrategia con la de valuación dentro del mismo service (mezcla de responsabilidades).
- Usar `@EventListener(ApplicationReadyEvent.class)`: equivalente a `ApplicationRunner`; preferimos `ApplicationRunner` por consistencia con el patrón ya existente en `AppCommandConfig`.

---

## 5. Impacto en tests existentes

### Pregunta
¿Los tests existentes rompen con la nueva firma de `calculate()` o con el nuevo valor retornado?

### Decisión
**Sí, los tests unitarios de `PlayerValuationCalculatorTest` rompen porque actualmente verifican que `calculate()` devuelve `result` (valor entre 0.00 y 1.00/1.05). Con la nueva firma, el parámetro `valorBase` debe agregarse y los valores esperados cambian.**

### Plan de adaptación
- Los tests existentes que pasan `valorBase = 0` seguirán siendo conceptualmente correctos si se actualiza la firma: `calculate(p, c, snapshot, BigDecimal.ZERO)`.
- Los valores esperados en tests que usan `valorBase = 0` no cambian (0 + result*2 con 0 base → ya no es el mismo resultado que antes, `result * 2` en lugar de `result`).
- **Se deben actualizar los `assertEquals` de todos los tests para reflejar `result * 2`** (con `valorBase = 0`).
- Los tests que verifican el comportamiento de `WeeklyValuationService` y `HoldingsValuationService` que mockean `calculator.calculate()` deben ajustar el stub para la nueva firma.

### Nota
La spec indica que `valorBase = 0` para tokens no valuados previamente, lo que significa que en tests que usan valores de base 0, el resultado es `result * 2`. Los tests deben actualizarse para reflejar esto.

---

## 6. Manejo del redondeo en la fórmula compuesta

### Decisión
**El redondeo HALF_UP a 2 decimales se aplica al resultado final de `valorBase + (result * factorEscala)`**, no a los componentes intermedios.

```java
return valorBase.add(result.multiply(FACTOR_ESCALA)).setScale(2, RoundingMode.HALF_UP);
```

### Rationale
- La spec indica: "Los valores en créditos se almacenan con dos decimales; el redondeo debe aplicarse al resultado final de la fórmula."
- Aplicar redondeo intermedio (e.g., redondear `result` antes de multiplicar por 2) introduce error de redondeo acumulado innecesario.
- `HoldingEntity.valuate()` ya aplica `setScale(2, HALF_UP)` como post-validación; la consistencia se preserva incluso si el valor llega ya redondeado.

---

## Resumen de decisiones

| # | Decisión | Clave |
|---|----------|-------|
| 1 | `valorBase` se lee de `h.getValorUnitario()` antes del cálculo en la misma transacción | Correctness |
| 2 | `FACTOR_ESCALA = 2` como constante estática en `PlayerValuationCalculator` | Rich Model |
| 3 | `calculate()` recibe `valorBase` y devuelve `valorToken` directamente | Rich Model |
| 4 | `SuperuserInitializationService.initializeSuperuser()` orquesta 1→2→3; `revalue()` sólo si se creó una estrategia nueva | Idempotencia |
| 5 | Tests existentes se adaptan: nueva firma + valores esperados multiplicados por 2 | Testing |
| 6 | Redondeo HALF_UP al resultado final de la fórmula completa | Correctness |
