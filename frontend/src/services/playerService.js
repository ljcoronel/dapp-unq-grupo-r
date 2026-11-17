import apiClient from './apiClient'

/**
 * Obtiene el catálogo de jugadores agrupados por liga desde la API pública.
 * @returns {Promise<Array<Array<Object>>>} Arreglo con 5 listas de jugadores por liga.
 */
export const fetchPlayers = async () => {
  const response = await apiClient.get('/players')
  return response.data
}

export const getPlayers = fetchPlayers

/**
 * Obtiene todas las valuaciones de un jugador para un usuario.
 * @param {number} playerId
 * @param {number} userId
 * @returns {Promise<Array<Object>>}
 */
export const getPlayerValuations = async (playerId, userId) => {
  const response = await apiClient.get(`/players/${playerId}/quotes/${userId}`)
  return response.data
}

/**
 * Obtiene la valuación actual de un jugador para un usuario.
 * @param {number} playerId
 * @param {number} userId
 * @returns {Promise<Object>}
 */
export const getCurrentValuation = async (playerId, userId) => {
  const response = await apiClient.get(`/players/${playerId}/quotes/${userId}/current`)
  return response.data
}

/**
 * Obtiene la valuación más cercana a una fecha.
 * @param {number} playerId
 * @param {number} userId
 * @param {string} date — ISO 8601 date-time string
 * @returns {Promise<Object>}
 */
export const getClosestValuation = async (playerId, userId, date) => {
  const response = await apiClient.get(`/players/${playerId}/quotes/${userId}/closest`, {
    params: { date },
  })
  return response.data
}
