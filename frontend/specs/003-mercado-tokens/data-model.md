# Data Model: Mercado de Tokens de Jugadores

**Feature**: 003-mercado-tokens | **Phase**: 1 — Design | **Date**: 2026-11-16

> This document describes the frontend data model: shapes of data flowing through the application, their origins (API responses), transformations applied in the frontend, and validation rules enforced in the UI.

---

## 1. API Response Shapes (from backend)

### 1.1 UserResponse
```ts
// GET /users/{id}, POST /login, POST /register
interface UserResponse {
  nombre: string
  // NOTE: No expone id. El userId se extrae del JWT payload (campo `sub`).
}
```

### 1.2 PlayerResponse
```ts
// GET /players, GET /players/{id}
interface PlayerResponse {
  externalId: number          // Identificador del jugador
  name: string
  team: TeamResponse
  league: LeagueResponse
  age?: number
  heightCM?: number
  weightKG?: number
  position?: string
  appearances: number         // Apariciones
  minutesPlayed: number       // Minutos jugados
  goals: number               // Goles
  assists: number             // Asistencias
  yellowCards: number         // Tarjetas amarillas
  redCards?: number
  avgShotsPerGame?: number
  passSuccessPercentage: number // Porcentaje de éxito en pases
  aerialsWon?: number
  timesManOfTheMatch?: number
}

interface TeamResponse {
  externalId: number
  name: string
}

interface LeagueResponse {
  codigo: string
  nombre: string
  pais: string
}
```

### 1.3 ValuationResponse
```ts
// GET /players/{playerId}/quotes/{userId}/current
// GET /players/{playerId}/quotes/{userId}
// GET /players/{playerId}/quotes/{userId}/closest
interface ValuationResponse {
  usuarioId: number
  jugadorId: number
  valorUnitario: number       // Precio unitario del token
  tipoEstrategia: 'ESTRATEGIA_A' | 'ESTRATEGIA_B'
  ejecutadoEn: string         // ISO 8601 date-time
  tipoEjecucion: 'DOMINICAL' | 'MANUAL'
}
```

### 1.4 OrdenPendienteResponse
```ts
// GET /users/{usuarioId}/orders
interface OrdenPendienteResponse {
  orderId: number
  operation: 'BUY' | 'SELL'
  playerId: number
  quantity: number
  price: number
  tolerance: number
  status: string              // e.g. 'PENDING'
  issuedAt: string            // ISO 8601 date-time
}
```

### 1.5 TransaccionEjecutadaResponse
```ts
// GET /users/{usuarioId}/transactions
interface TransaccionEjecutadaResponse {
  orderId: number
  operation: 'BUY' | 'SELL'
  playerId: number
  quantity: number
  settledPrice: number | null
  buyerId: number | null
  sellerId: number | null
  issuedAt: string            // ISO 8601 date-time
  executedAt: string | null   // ISO 8601 date-time
}
```

### 1.6 HoldingResponse
```ts
// GET /users/{usuarioId}/holdings
interface HoldingResponse {
  id: number
  usuarioId: number
  jugadorId: number
  tokens: number              // Cantidad total de tokens (incluye bloqueados)
  valorTotal: number          // Valor actual de todos los tokens
  valorUnitario: number       // Valor unitario actual
}
```

### 1.7 StrategyStateResponse
```ts
// GET /strategy/{usuarioId}
// PUT /strategy/{usuarioId}/configure
interface StrategyStateResponse {
  estrategia: 'ESTRATEGIA_A' | 'ESTRATEGIA_B' | null
  pesos: Record<string, number>
  estado: string
  vigenteDesde: string | null  // ISO 8601 date-time
  actual: StrategyConfig | null
  pendiente: StrategyConfig | null
}

interface StrategyConfig {
  estrategia: 'ESTRATEGIA_A' | 'ESTRATEGIA_B'
  pesos: Record<string, number>
  vigenteDesde: string         // ISO 8601 date-time
}
```

### 1.8 CrearOrdenRequest
```ts
// POST /orders/buy, POST /orders/sell
interface CrearOrdenRequest {
  usuarioId: number           // Extraído del JWT
  playerId: number            // externalId del jugador
  quantity: number            // Entero ≥ 1
  price: number               // valorUnitario de ValuationResponse
  tolerance: number           // Valor absoluto ≥ 0
}
```

### 1.9 StrategyRequest
```ts
// PUT /strategy/{usuarioId}/configure
interface StrategyRequest {
  tipo: 'ESTRATEGIA_A' | 'ESTRATEGIA_B'
  pesos: Record<string, number>
}
```

---

## 2. Frontend-only State Shapes

### 2.1 AuthSession (en AuthContext)
```ts
interface AuthSession {
  authToken: string | null       // JWT raw string
  userId: number | null          // Extraído del payload JWT (campo `sub`)
  isAuthenticated: boolean
  isLoading: boolean
  error: string
}
```

### 2.2 PlayerTableFilters (en CatalogoPage)
```ts
interface PlayerTableFilters {
  liga: string        // LeagueResponse.nombre — '' = todos
  equipo: string      // TeamResponse.name — '' = todos
  nacionalidad: string // PlayerResponse.position (campo pais de la liga) — '' = todos
}

interface PlayerTableSort {
  column: keyof PlayerResponse | null
  direction: 'asc' | 'desc'
}
```

### 2.3 OrderDraft (estado local de modales)
```ts
interface OrderDraft {
  playerId: number | null
  quantity: number            // Valor del input (entero)
  tolerance: number           // Valor del input (decimal ≥ 0)
  unitPrice: number | null    // Cargado desde ValuationResponse
  isLoadingPrice: boolean
  priceError: string | null
}

// Computed (derived from OrderDraft):
// totalSinTolerancia = quantity × unitPrice
// BUY → totalConTolerancia = totalSinTolerancia + tolerance
// SELL → totalConTolerancia = totalSinTolerancia − (quantity × tolerance)
```

### 2.4 StrategyFormState
```ts
interface StrategyFormState {
  tipo: 'ESTRATEGIA_A' | 'ESTRATEGIA_B' | null
  pesos: Record<string, string>  // string para manejar inputs numéricos
}

// Validación:
// ESTRATEGIA_A: sum(Object.values(pesos).map(Number)) === 1
// ESTRATEGIA_B: sum(Object.values(pesos).map(Number)) === 0.95
```

### 2.5 PortfolioPieChartData
```ts
interface PieSlice {
  name: string    // PlayerResponse.name o 'Otros'
  value: number   // HoldingResponse.valorTotal (o suma de los menores)
}
// Máximo 11 slices: 10 individuales + 1 'Otros' si hay > 10 jugadores
```

---

## 3. Entity Relationships

```
AuthSession (AuthContext)
  └── userId → parámetro en todos los endpoints de usuario

PlayerResponse[] (GET /players)
  └── externalId → usado como playerId en CrearOrdenRequest
                 → usado en ValuationResponse.jugadorId
                 → usado en HoldingResponse.jugadorId
                 → usado en OrdenPendienteResponse.playerId

HoldingResponse (GET /users/{usuarioId}/holdings)
  └── jugadorId → join con PlayerResponse por externalId
  └── tokens (total) − tokensEnOrdenesVentaActivas = tokensDisponibles

OrdenPendienteResponse[] (GET /users/{usuarioId}/orders)
  └── operation='SELL' → quantity contribuye a tokensEnOrdenesVentaActivas

StrategyStateResponse (GET /strategy/{usuarioId})
  └── estrategia !== null → prerrequisito para emitir órdenes
```

---

## 4. Validation Rules

| Campo | Regla | Mensaje de error |
|-------|-------|-----------------|
| `quantity` (compra) | Entero, 1 ≤ quantity ≤ 100 | "La cantidad debe ser un número entero entre 1 y 100" |
| `quantity` (venta) | Entero, 1 ≤ quantity ≤ tokensDisponibles | "La cantidad no puede superar los tokens disponibles ({n})" |
| `tolerance` | Decimal ≥ 0, máx 2 decimales | "La tolerancia debe ser un número positivo" |
| `playerId` | Requerido, debe ser un jugador válido | "Seleccione un jugador" |
| Estrategia A pesos | sum === 1 (±0.001 por floating point) | "La suma de los pesos debe ser exactamente 1" |
| Estrategia B pesos | sum === 0.95 (±0.001) | "La suma de los pesos debe ser exactamente 0.95" |
| Créditos (compra) | totalConTolerancia ≤ creditosDisponibles | "Créditos insuficientes para realizar esta compra" |

---

## 5. State Transitions

### Orden de Compra
```
DRAFT → (validación UI) → PENDING (backend) → EXECUTED | CANCELLED
```

### Estrategia de Valuación
```
null (sin estrategia) → ESTRATEGIA_A | ESTRATEGIA_B
ESTRATEGIA_A ↔ ESTRATEGIA_B  (cambio mediante PUT /strategy/{usuarioId}/configure)
```

### Tokens de un Jugador
```
tokens = tokensDisponibles + tokensEnOrdenesVentaActivas
tokensDisponibles = tokens - sum(SELL orders quantity for that player)
```
