/**
 * Utilidades de cálculo y validación para órdenes de compra y venta.
 */

/**
 * Valida que la cantidad de tokens sea un número entero dentro del rango permitido (1 a 100 por defecto para compras).
 * @param {unknown} quantity
 * @param {{ min?: number, max?: number }} [options]
 * @returns {{ isValid: boolean, error?: string }}
 */
export function validateQuantity(quantity, { min = 1, max = 100 } = {}) {
  if (quantity === '' || quantity === null || quantity === undefined) {
    return { isValid: false, error: 'La cantidad de tokens es obligatoria.' }
  }

  const num = Number(quantity)

  if (!Number.isInteger(num)) {
    return { isValid: false, error: 'La cantidad de tokens debe ser un número entero.' }
  }

  if (num < min) {
    return { isValid: false, error: `La cantidad mínima de tokens es ${min}.` }
  }

  if (max !== undefined && num > max) {
    return { isValid: false, error: `Una orden de compra no puede superar los ${max} tokens.` }
  }

  return { isValid: true }
}

/**
 * Valida que la tolerancia sea un número mayor o igual a 0 con hasta 2 decimales.
 * @param {unknown} tolerance
 * @returns {{ isValid: boolean, error?: string }}
 */
export function validateTolerance(tolerance) {
  if (tolerance === '' || tolerance === null || tolerance === undefined) {
    return { isValid: false, error: 'La tolerancia es obligatoria.' }
  }

  const str = String(tolerance).trim()
  const num = Number(str)

  if (Number.isNaN(num) || num < 0) {
    return { isValid: false, error: 'La tolerancia debe ser un número mayor o igual a 0.' }
  }

  // Comprobar que no tenga más de dos decimales
  const decimalParts = str.split('.')
  if (decimalParts.length > 1 && decimalParts[1].length > 2) {
    return { isValid: false, error: 'La tolerancia no puede tener más de dos decimales.' }
  }

  return { isValid: true }
}

/**
 * Calcula precios para órdenes de compra:
 * - totalSinTolerancia = cantidad * precioUnitario
 * - totalConTolerancia = totalSinTolerancia + tolerancia
 *
 * @param {number} quantity
 * @param {number} unitPrice
 * @param {number} tolerance
 * @returns {{ totalSinTolerancia: number, totalConTolerancia: number }}
 */
export function calculateBuyPrices(quantity, unitPrice, tolerance = 0) {
  const qty = Number(quantity) || 0
  const price = Number(unitPrice) || 0
  const tol = Number(tolerance) || 0

  const totalSinTolerancia = Number((qty * price).toFixed(2))
  const totalConTolerancia = Number((totalSinTolerancia + tol).toFixed(2))

  return {
    totalSinTolerancia,
    totalConTolerancia,
  }
}

/**
 * Calcula precios para órdenes de venta:
 * - totalSinTolerancia = cantidad * precioUnitario
 * - totalConTolerancia = totalSinTolerancia - (cantidad * tolerancia)
 *
 * @param {number} quantity
 * @param {number} unitPrice
 * @param {number} tolerance
 * @returns {{ totalSinTolerancia: number, totalConTolerancia: number }}
 */
export function calculateSellPrices(quantity, unitPrice, tolerance = 0) {
  const qty = Number(quantity) || 0
  const price = Number(unitPrice) || 0
  const tol = Number(tolerance) || 0

  const totalSinTolerancia = Number((qty * price).toFixed(2))
  const deduction = qty * tol
  const totalConTolerancia = Number(Math.max(0, totalSinTolerancia - deduction).toFixed(2))

  return {
    totalSinTolerancia,
    totalConTolerancia,
  }
}

/**
 * Valida un borrador completo de orden de compra.
 * @param {{
 *   playerId: number | null,
 *   quantity: unknown,
 *   tolerance: unknown,
 *   unitPrice: number | null,
 *   userCredits?: number | null,
 * }} draft
 * @returns {{ isValid: boolean, errors: Record<string, string> }}
 */
export function validateBuyOrder({
  playerId,
  quantity,
  tolerance,
  unitPrice,
  userCredits,
}) {
  const errors = {}

  if (!playerId) {
    errors.playerId = 'Debe seleccionar un jugador.'
  }

  const qtyValidation = validateQuantity(quantity, { min: 1, max: 100 })
  if (!qtyValidation.isValid) {
    errors.quantity = qtyValidation.error
  }

  const tolValidation = validateTolerance(tolerance)
  if (!tolValidation.isValid) {
    errors.tolerance = tolValidation.error
  }

  if (unitPrice === null || unitPrice === undefined || unitPrice <= 0) {
    errors.unitPrice = 'No se pudo obtener el precio unitario del jugador.'
  }

  if (!errors.quantity && !errors.tolerance && unitPrice > 0) {
    const { totalConTolerancia } = calculateBuyPrices(quantity, unitPrice, tolerance)
    if (userCredits !== null && userCredits !== undefined && totalConTolerancia > userCredits) {
      errors.credits = `Créditos insuficientes. Total requerido: ${totalConTolerancia}, Créditos disponibles: ${userCredits}.`
    }
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
  }
}

/**
 * Valida un borrador completo de orden de venta.
 * @param {{
 *   playerId: number | null,
 *   quantity: unknown,
 *   tolerance: unknown,
 *   unitPrice: number | null,
 *   availableTokens: number,
 * }} draft
 * @returns {{ isValid: boolean, errors: Record<string, string> }}
 */
export function validateSellOrder({
  playerId,
  quantity,
  tolerance,
  unitPrice,
  availableTokens,
}) {
  const errors = {}

  if (!playerId) {
    errors.playerId = 'Debe seleccionar un jugador.'
  }

  const safeAvailable = Math.max(0, Number(availableTokens) || 0)

  if (safeAvailable <= 0) {
    errors.available = 'No posee tokens disponibles para vender de este jugador.'
  }

  const qtyValidation = validateQuantity(quantity, { min: 1, max: safeAvailable })
  if (!qtyValidation.isValid) {
    errors.quantity = qtyValidation.error
  }

  const tolValidation = validateTolerance(tolerance)
  if (!tolValidation.isValid) {
    errors.tolerance = tolValidation.error
  }

  if (unitPrice === null || unitPrice === undefined || unitPrice <= 0) {
    errors.unitPrice = 'No se pudo obtener el precio unitario del jugador.'
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
  }
}

