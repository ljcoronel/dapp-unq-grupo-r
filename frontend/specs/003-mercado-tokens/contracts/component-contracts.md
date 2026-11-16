# Component Contracts

**Feature**: 003-mercado-tokens | **Phase**: 1 — Design | **Date**: 2026-11-16

Este documento define los contratos de props e interfaces de los componentes nuevos introducidos por la feature.

---

## AppLayout

**Responsabilidad**: Layout base que renderiza el Sidebar permanente a la izquierda y el contenido principal (Outlet) a la derecha.

```jsx
// No recibe props externas; lee AuthContext internamente.
// Renderiza: <Sidebar /> + <Outlet />
```

---

## Sidebar

**Responsabilidad**: Navegación lateral permanente con las cuatro secciones.

```jsx
// Props: ninguna (usa useLocation de react-router para sección activa)
// Items: Catálogo → /app/catalogo
//        Comerciar → /app/comerciar
//        Portfolio → /app/portfolio
//        Estrategia → /app/estrategia
// Siempre visible. Item activo con estilo diferenciado.
```

---

## CatalogoPage

**Responsabilidad**: Vista de catálogo unificado de jugadores con filtros y ordenamiento.

```jsx
// Estado interno:
//   players: PlayerResponse[]
//   isLoading: boolean
//   error: string
//   filters: PlayerTableFilters
//   sort: PlayerTableSort
// Renderiza: PlayerFilters + PlayerTable
```

---

## PlayerFilters

**Responsabilidad**: Controles de filtro (dropdowns) para liga, equipo y nacionalidad.

```jsx
Props = {
  filters: PlayerTableFilters,
  options: { ligas: string[], equipos: string[], nacionalidades: string[] },
  onChange: (filters: PlayerTableFilters) => void
}
```

---

## PlayerTable

**Responsabilidad**: Tabla de jugadores con columnas ordenables.

```jsx
Props = {
  players: PlayerResponse[],     // Array ya filtrado y ordenado
  sort: PlayerTableSort,
  onSortChange: (sort: PlayerTableSort) => void
}
// Columnas: nombre, equipo, liga, apariciones, minutos jugados, goles,
//           asistencias, tarjetas amarillas, % pases exitosos
```

---

## ComerciarPage

**Responsabilidad**: Vista de comercio con lista de órdenes activas y botones Comprar/Vender.

```jsx
// Estado interno:
//   orders: OrdenPendienteResponse[]
//   strategy: StrategyStateResponse | null
//   showBuyModal: boolean
//   showSellModal: boolean
//   isLoading: boolean
//   error: string
```

---

## OrderList

**Responsabilidad**: Lista de órdenes pendientes (compra o venta).

```jsx
Props = {
  orders: OrdenPendienteResponse[],
  type: 'BUY' | 'SELL',
  players: PlayerResponse[],     // Para mostrar nombre del jugador
  onCancel: (orderId: number) => void
}
```

---

## BuyModal

**Responsabilidad**: Modal de creación de orden de compra.

```jsx
Props = {
  isOpen: boolean,
  players: PlayerResponse[],
  userId: number,
  onConfirm: (order: CrearOrdenRequest) => Promise<void>,
  onClose: () => void
}
// Muestra precio unitario (ValuationResponse), total sin y con tolerancia.
// Deshabilita submit si totalConTolerancia > creditosDisponibles o validaciones fallan.
```

---

## SellModal

**Responsabilidad**: Modal de creación de orden de venta.

```jsx
Props = {
  isOpen: boolean,
  players: PlayerResponse[],
  holdings: HoldingResponse[],
  pendingOrders: OrdenPendienteResponse[],
  userId: number,
  onConfirm: (order: CrearOrdenRequest) => Promise<void>,
  onClose: () => void
}
// Calcula tokensDisponibles = holding.tokens - sum(SELL orders for player)
```

---

## PortfolioPage

**Responsabilidad**: Vista de portfolio con tenencias, historial y gráfico.

```jsx
// Estado interno:
//   holdings: HoldingResponse[]
//   transactions: TransaccionEjecutadaResponse[]
//   players: PlayerResponse[]
//   isLoading: boolean
//   error: string
```

---

## HoldingList

**Responsabilidad**: Tabla de tenencias por jugador.

```jsx
Props = {
  holdings: HoldingResponse[],
  players: PlayerResponse[]   // Para nombre del jugador
}
// Columnas: jugador, cantidad de tokens, precio promedio compra*, valor actual
// *precio promedio compra: no está en HoldingResponse actual — se mostrará valorUnitario
```

---

## TransactionHistory

**Responsabilidad**: Historial de transacciones ejecutadas.

```jsx
Props = {
  transactions: TransaccionEjecutadaResponse[],
  players: PlayerResponse[]
}
// Cada fila: tipo (BUY en verde / SELL en rojo), jugador, cantidad, importe total, fecha
```

---

## PortfolioPieChart

**Responsabilidad**: Gráfico de torta de distribución del portfolio.

```jsx
Props = {
  holdings: HoldingResponse[],
  players: PlayerResponse[]
}
// Usa recharts PieChart. Agrupa en 'Otros' si hay > 10 jugadores.
```

---

## EstrategiaPage

**Responsabilidad**: Vista de estrategia de valuación actual y botón para cambiar.

```jsx
// Estado interno:
//   strategy: StrategyStateResponse | null
//   showModal: boolean
//   isLoading: boolean
//   error: string
```

---

## StrategyDisplay

**Responsabilidad**: Visualización de la estrategia de valuación actual.

```jsx
Props = {
  strategy: StrategyStateResponse
}
// Muestra tipo de estrategia y tabla de métricas con pesos
```

---

## StrategyForm

**Responsabilidad**: Formulario de configuración de estrategia (dentro de modal).

```jsx
Props = {
  initialStrategy: StrategyStateResponse | null,
  userId: number,
  onConfirm: (result: StrategyStateResponse) => void,
  onClose: () => void
}
// Selector ESTRATEGIA_A / ESTRATEGIA_B
// Inputs por métrica con validación de suma de pesos
```

---

## ProfilePage

**Responsabilidad**: Vista de perfil del usuario.

```jsx
// Lee userId de AuthContext
// Llama a userService.getUserProfile(userId)
// Muestra: nombre de usuario
```
