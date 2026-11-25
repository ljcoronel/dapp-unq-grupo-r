export const isValidIntegerQuantity = (
  value: string | number,
  min = 1,
  max = 100,
): boolean => {
  if (typeof value === 'string' && !value.trim()) {
    return false
  }

  const quantity = Number(value)
  return Number.isInteger(quantity) && quantity >= min && quantity <= max
}

export const isValidTolerance = (value: string | number): boolean => {
  if (typeof value === 'string' && !value.trim()) {
    return false
  }

  const tolerance = Number(value)
  return Number.isFinite(tolerance)
    && tolerance >= 0
    && Math.abs(tolerance * 100 - Math.round(tolerance * 100)) < 1e-8
}

export const isSellOperation = (operation: string | undefined): boolean =>
  operation === 'SELL' || operation === 'VENTA'

export const isBuyOperation = (operation: string | undefined): boolean =>
  operation === 'BUY' || operation === 'COMPRA'

export const getAvailableTokens = (
  playerId: number,
  holdings: { jugadorId?: number; tokens?: number }[],
  pendingOrders: { operation?: string; playerId?: number; quantity?: number }[],
): number => {
  const totalTokens = holdings.find((holding) => holding.jugadorId === playerId)?.tokens ?? 0
  const blockedTokens = pendingOrders
    .filter((order) => isSellOperation(order.operation) && order.playerId === playerId)
    .reduce((total, order) => total + (order.quantity ?? 0), 0)

  return Math.max(0, totalTokens - blockedTokens)
}
