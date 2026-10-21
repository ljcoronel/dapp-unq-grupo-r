# Quickstart de validacion

## Prerrequisitos

- Java 25 y Gradle disponibles.
- PostgreSQL local o Docker, con las propiedades del backend configuradas.
- Chromium instalado para Playwright si se prueba el scraping real.

## Validacion automatizada

Desde `backend`:

```powershell
.\gradlew.bat :app:test
```

La suite cubre:
- Invariantes de pesos de Estrategia A (suma 100%) y Estrategia B (suma 95% + 5% fijo de `position` para `Defender`).
- Normalización min/max en rango `[0,1]`, métricas faltantes tratadas como 0 y redondeo monetario a dos decimales (`valorTotal = tokens * valorUnitario`).
- Persistencia e invariantes de tenencias (100 tokens iniciales por jugador bajo el Superusuario, sin duplicación).
- Reemplazo atómico de configuraciones pendientes y rechazo `409 Conflict` cuando se intenta crear una pendiente sin configuración actual previa.
- Ejecución de la ventana semanal dominical y del recálculo manual idempotente.
- Tolerancia a fallos y rollback transaccional ante scraping fallido conservando la última valuación válida.
- Pruebas E2E de contratos MockMvc para todos los endpoints.

---

## Escenarios de prueba detallados

### 1. Escenario de Configuración Inicial (Estrategia A) y Consulta de Tenencias

1. Obtener token JWT autenticando al usuario (e.g. `POST /auth/login`).
2. Configurar por primera vez la Estrategia A enviando los 5 pesos que suman 100%:
   ```bash
   curl -X PUT "http://localhost:8080/users/1/strategy" \
     -H "Authorization: Bearer <TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{
       "estrategia": "ESTRATEGIA_A",
       "pesos": {
         "goals": 30.0,
         "assists": 25.0,
         "passSuccessPercentage": 15.0,
         "aerialsWon": 15.0,
         "timesManOfTheMatch": 15.0
       }
     }'
   ```
   **Respuesta esperada (200 OK)**:
   - `estado`: `"ACTUAL"`.
   - `actual`: contiene la estrategia configurada y `vigenteDesde`.
   - `pendiente`: `null`.
   - Dispara valuación inmediata de las tenencias del usuario.

3. Consultar las tenencias del usuario:
   ```bash
   curl -X GET "http://localhost:8080/users/1/holdings" \
     -H "Authorization: Bearer <TOKEN>"
   ```
   **Verificación**:
   - Cada tenencia tiene `valorUnitario` y `valorTotal` redondeados a 2 decimales.
   - Se cumple estrictamente: `valorTotal = tokens * valorUnitario`.

---

### 2. Escenario de Estrategia Pendiente y Reemplazo

1. Con una estrategia ya activa (`ACTUAL`), enviar una nueva configuración (por ejemplo, Estrategia B con 95% total para las 4 métricas variables):
   ```bash
   curl -X PUT "http://localhost:8080/users/1/strategy" \
     -H "Authorization: Bearer <TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{
       "estrategia": "ESTRATEGIA_B",
       "pesos": {
         "passSuccessPercentage": 30.0,
         "aerialsWon": 25.0,
         "minutesPlayed": 20.0,
         "appearances": 20.0
       }
     }'
   ```
   **Respuesta esperada (200 OK)**:
   - `estado`: `"PENDIENTE"`.
   - `actual`: mantiene la Estrategia A vigente sin alteración de valores actuales.
   - `pendiente`: contiene la Estrategia B (con el 5% de `position` asignado).
   - No altera las tenencias ni dispara recálculo inmediato.

2. **Reemplazo de pendiente**: Enviar un tercer cambio antes de la ventana dominical.
   - Se reemplaza la configuración `PENDIENTE` previa sin crear múltiples registros pendientes (solo existe una pendiente activa por usuario).

3. **Rechazo sin estrategia actual (409 Conflict)**:
   - Para un usuario nuevo que intente explícitamente enviar `estado=PENDIENTE` sin tener una estrategia `ACTUAL` registrada:
     ```bash
     curl -X PUT "http://localhost:8080/users/2/strategy?estado=PENDIENTE" \
       -H "Authorization: Bearer <TOKEN>" \
       -H "Content-Type: application/json" \
       -d '{
         "estrategia": "ESTRATEGIA_A",
         "pesos": {"goals": 30.0, "assists": 25.0, "passSuccessPercentage": 15.0, "aerialsWon": 15.0, "timesManOfTheMatch": 15.0}
       }'
     ```
   - **Respuesta esperada**: `409 Conflict` con `{"message": "No existe estrategia actual para crear una pendiente"}`.

---

### 3. Escenario de Recálculo Manual Público

1. Invocar el endpoint de recálculo manual **sin cabecera de autenticación**:
   ```bash
   curl -X POST "http://localhost:8080/quotes/recalculate"
   ```
   **Respuesta esperada (200 OK)**:
   ```json
   {
     "status": "COMPLETED",
     "playersScraped": 50,
     "recalculatedHoldings": 50,
     "executedAt": "2026-10-21T19:00:00Z"
   }
   ```
   **Efectos colaterales verificados**:
   - Ejecuta `WhoScoredScrapingService.scrape("50")`.
   - Promueve las estrategias `PENDIENTE` a `ACTUAL` antes de valuar.
   - Actualiza las tenencias de todos los usuarios con estrategia activa.
   - Registra un snapshot en el historial (`/users/{usuarioId}/holdings/history`) con `tipoEjecucion: "MANUAL"`.
   - Omite silenciosamente a los usuarios sin estrategia configurada (`SIN_ESTRATEGIA`).

---

### 4. Escenario de Idempotencia de la Ventana y Recálculos Repetidos

1. Ejecutar de manera consecutiva dos solicitudes a `POST /quotes/recalculate` o dos ejecuciones del scheduler dentro de la misma ventana dominical.
2. **Verificación de Idempotencia**:
   - La restricción de unicidad `(usuario_id, jugador_id, ventana)` en `ValuacionEntity` y `ValuationHistoryEntity` evita filas duplicadas por ejecución.
   - Las cantidades de `tokens` en `HoldingEntity` permanecen constantes (invariante de 100 tokens por jugador en el Superusuario, sin creación ni pérdida de tokens).
   - El historial preserva la trazabilidad exacta de las ejecuciones sin duplicar snapshots para la misma clave única de ventana y tipo.

---

### 5. Escenario de Tolerancia a Fallos y Rollback ante Scraping Incompleto

1. Simular un fallo durante el proceso de scraping (por ejemplo, desconexión del proveedor WhoScored o snapshot devuelto vacío/incompleto).
2. Invocar `POST /quotes/recalculate`:
   ```bash
   curl -X POST "http://localhost:8080/quotes/recalculate"
   ```
   **Respuesta esperada (503 Service Unavailable)**:
   ```json
   {
     "message": "No se pudo completar el scraping o el recalculo; se conserva la ultima valuacion valida. Snapshot de jugadores vacio o incompleto"
   }
   ```
   **Verificación del Rollback Transaccional**:
   - La transacción de `WeeklyValuationService.execute(...)` se revierte por completo.
   - Ninguna estrategia `PENDIENTE` es promovida a `ACTUAL`.
   - No se alteran los valores vigentes de `valorUnitario` o `valorTotal` en las tenencias.
   - No se crean snapshots corruptos en `ValuationHistoryEntity`.
   - La última valuación válida permanece intacta y disponible para consulta.
