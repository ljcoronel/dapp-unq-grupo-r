import apiClient from './apiClient'

/**
 * Obtiene las tenencias actuales de un usuario.
 * @param {number} userId
 * @returns {Promise<Array<Object>>}
 */
export const getHoldings = async (userId) => {
  const response = await apiClient.get(`/users/${userId}/holdings`)
  return response.data
}
