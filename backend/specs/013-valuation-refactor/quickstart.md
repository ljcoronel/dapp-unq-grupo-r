# Quickstart — Validación del Refactor de Valuación

## Prerrequisitos

- Java 25, Gradle instalado
- Docker corriendo (para Testcontainers en tests de integración)
- PostgreSQL accesible o iniciado vía Docker Compose
- Variables de entorno configuradas (ver `application.properties` / `application-test.properties`)

---

## 1. Compilar el proyecto

```bash
cd backend
./gradlew clean build -x test
```

**Resultado esperado**: `BUILD SUCCESSFUL`

---

## 2. Ejecutar todos los tests

```bash
cd backend
./gradlew test
```

**Resultado esperado**: todos los tests pasan, incluyendo:
- `unit/PlayerValuationCalculatorTest` — verifica la nueva fórmula
- `service/HoldingsValuationServiceTest` — verifica que `valorBase` se lee antes del cálculo
- `service/SuperuserInitializationServiceTest` — verifica el flujo 1→2→3
- `integration/ValuationPersistenceIntegrationTest` — verifica persistencia con la nueva fórmula
- `integration/InitialHoldingsIntegrationTest` — verifica el flujo del Superusuario end-to-end con DB real
- `e2e/ValuationConfigurationE2ETest` — verifica la acumulación de valores vía HTTP

---

## 3. Iniciar la aplicación

```bash
cd backend
./gradlew bootRun
```

**Resultado esperado**: Startup exitoso con logs similares a:

```
INFO SuperuserInitializationService - Superusuario encontrado/creado: superuser (id=1)
INFO InitialHoldingsService        - Tenencias iniciales del superusuario: N tenencias creadas/verificadas
INFO SuperuserInitializationService - Estrategia ACTUAL configurada para superusuario
INFO HoldingsValuationService      - Revaluadas N tenencias para el usuario 1 con tipo MANUAL
```

---

## 4. Verificar la fórmula de valuación (US-1 y US-2)

### Escenario: valorBase = 0 (primera valuación)

1. Registrar un usuario nuevo (o usar el Superusuario).
2. Configurar la primera estrategia → dispara valuación inmediata.
3. Consultar `GET /users/{id}/holdings`.

**Verificación matemática**:
```
valorToken = 0 + (result × 2)
```
Si `result = 0.50` → `valorToken = 1.00` (no 0.50 como antes del refactor).

### Escenario: re-valuación acumulativa (valorBase > 0)

1. Usar un usuario con tenencias ya valuadas (valorUnitario > 0).
2. Ejecutar `POST /quotes/recalculate`.
3. Verificar que el nuevo `valorUnitario` = `valorUnitario_anterior + (result × 2)`.

**Ejemplo concreto** (ver [Acceptance Scenario 2 en spec](../spec.md#user-story-1)):
```
valorBase = 3.00, result = 0.50
valorToken = 3.00 + (0.50 × 2) = 4.00   ✓
```

---

## 5. Verificar inicialización del Superusuario (US-3)

### Escenario: Primer arranque

1. Vaciar/reiniciar la base de datos.
2. Iniciar la aplicación.
3. Consultar `GET /users/{idSuperUser}/strategy`:
   ```json
   { "estado": "ACTUAL", "estrategia": "ESTRATEGIA_A", ... }
   ```
4. Consultar `GET /users/{idSuperUser}/holdings` (si hay jugadores con métricas):
   - Al menos un token debe tener `valorUnitario > 0`.

### Escenario: Idempotencia (reinicio)

1. Reiniciar la aplicación sin vaciar la DB.
2. Verificar que existe exactamente **una** estrategia ACTUAL para el Superusuario (no se duplicó).
3. Verificar que los `valorUnitario` de las tenencias no cambiaron (no hubo re-valuación redundante).

---

## 6. Referencias

- Contratos de API: [contracts/valuation-api.md](./contracts/valuation-api.md)
- Modelo de datos: [data-model.md](./data-model.md)
- Fórmula y constantes: [research.md](./research.md)
- Scenarios de aceptación completos: [spec.md](./spec.md)

> [!TIP]
> Para verificar la fórmula matemáticamente sin depender del scraping, se pueden crear datos de prueba en la DB directamente (insertando jugadores con métricas conocidas) y ejecutar `POST /quotes/recalculate` vía Postman.
