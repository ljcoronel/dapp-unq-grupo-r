import apiClient from './apiClient'

/**
 * Crea una orden de compra.
 * Requiere Authorization header (adjuntado por interceptor de apiClient).
 * @param {Object} orderData
 * @returns {Promise<Object>}
 */
export const createBuyOrder = async (orderData) => {
  const response = await apiClient.post('/orders/buy', orderData)
  return response.data
}

/**
 * Crea una orden de venta.
 * Requiere Authorization header (adjuntado por interceptor de apiClient).
 * @param {Object} orderData
 * @returns {Promise<Object>}
 */
export const createSellOrder = async (orderData) => {
  const response = await apiClient.post('/orders/sell', orderData)
  return response.data
}

/**
 * Obtiene las órdenes pendientes de un usuario.
 * @param {number} userId
 * @returns {Promise<Array<Object>>}
 */
export const getPendingOrders = async (userId) => {
  const response = await apiClient.get(`/users/${userId}/orders`)
  return response.data
}

/**
 * Obtiene las transacciones ejecutadas de un usuario.
 * @param {number} userId
 * @returns {Promise<Array<Object>>}
 */
export const getTransactions = async (userId) => {
  const response = await apiClient.get(`/users/${userId}/transactions`)
  return response.data
}

/**
 * Cancela una orden pendiente.
 * @param {number} orderId
 * @param {number} userId
 * @returns {Promise<void>}
 */
export const cancelOrder = async (orderId, userId) => {
  await apiClient.delete(`/orders/${orderId}`, { params: { usuarioId: userId } })
}
