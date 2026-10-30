# Contrato de API: Valuación de Tenencias

> [!NOTE]
> Este contrato documenta los endpoints afectados por el refactor de valuación. No se agregan ni eliminan endpoints. Los contratos de request/response no cambian — el cambio es de comportamiento (nueva fórmula).

---

## Endpoints afectados

### `POST /quotes/recalculate`

**Descripción**: Ejecuta una valuación manual de todas las tenencias de todos los usuarios con estrategia ACTUAL.

**Request**: No body. Autenticación: API Key (`X-Api-Key` header) o JWT con rol ADMIN.

**Response `200 OK`**:
```json
{
  "status": "COMPLETED",
  "playersProcessed": 42,
  "holdingsRecalculated": 1250,
  "executedAt": "2026-10-30T15:00:00Z"
}
```

**Comportamiento nuevo**: Cada `valorToken` se calcula como `valorBase + (result × 2)`, donde `valorBase` es el `valor_unitario` persistido **antes** de esta ejecución.

---

### `GET /users/{id}/holdings`

**Descripción**: Retorna las tenencias de un usuario con el valor unitario y total actuales.

**Path param**: `id` — ID del usuario (Long).

**Response `200 OK`**:
```json
[
  {
    "jugadorId": 123,
    "nombre": "Lionel Messi",
    "tokens": 100,
    "valorUnitario": 4.00,
    "valorTotal": 400.00
  }
]
```

**Comportamiento nuevo**: `valorUnitario` es ahora acumulativo entre valuaciones: `valorBase + (result × 2)`.

**Caso Superusuario**: Tras el arranque del sistema (con al menos un jugador con métricas), `GET /users/{idSuperUser}/holdings` devuelve al menos un token con `valorUnitario > 0`.

---

### `GET /users/{id}/strategy`

**Descripción**: Retorna el estado de la estrategia del usuario.

**Path param**: `id` — ID del usuario (Long).

**Response `200 OK`** (con estrategia ACTUAL):
```json
{
  "estrategia": "ESTRATEGIA_A",
  "pesos": {
    "goals": 30,
    "assists": 25,
    "passSuccessPercentage": 15,
    "aerialsWon": 15,
    "timesManOfTheMatch": 15
  },
  "estado": "ACTUAL",
  "vigenteDesde": "2026-10-30T15:00:00Z"
}
```

**Comportamiento nuevo**: Tras el arranque del sistema, el Superusuario siempre tiene una estrategia con `estado = "ACTUAL"` (FR-007).

---

### `POST /users/{id}/strategy` (configurar primera estrategia)

**Descripción**: Configura la estrategia de un usuario. Cuando el usuario no tiene ninguna estrategia previa, la nueva estrategia se configura como ACTUAL y se ejecuta una valuación inmediata de sus tenencias.

**Comportamiento nuevo**: La valuación inmediata aplica la nueva fórmula `valorToken = valorBase + (result × 2)`. Para la primera configuración, `valorBase = 0` para todos los tokens.

---

## Cambio de comportamiento — comparación

| Escenario | Antes | Después |
|-----------|-------|---------|
| Primera valuación (valorBase=0) | `valorToken = result` | `valorToken = result × 2` |
| Re-valuación (valorBase>0) | `valorToken = result` (sobreescribe) | `valorToken = valorBase + (result × 2)` (acumula) |
| Superusuario sin estrategia al arrancar | No se configura ni valúa automáticamente | Se crea estrategia ACTUAL y se valúan los tokens |
| Superusuario con estrategia ACTUAL al arrancar | N/A | No se modifica ni re-valúa (idempotencia) |

> [!IMPORTANT]
> Los valores de `valorUnitario` devueltos por `GET /users/{id}/holdings` serán **mayores** que antes del refactor para usuarios ya valuados, ya que el valor anterior se suma al nuevo cálculo.
