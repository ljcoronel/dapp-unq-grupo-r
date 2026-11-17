import apiClient from './apiClient'

/**
 * Obtiene el estado actual de la estrategia de valuación de un usuario.
 * @param {number} userId
 * @returns {Promise<Object>}
 */
export const getStrategy = async (userId) => {
  const response = await apiClient.get(`/strategy/${userId}`)
  return response.data
}

/**
 * Configura (crea o actualiza) la estrategia de valuación de un usuario.
 * @param {number} userId
 * @param {Object} strategyData
 * @returns {Promise<Object>}
 */
export const configureStrategy = async (userId, strategyData) => {
  const response = await apiClient.put(`/strategy/${userId}/configure`, strategyData)
  return response.data
}
