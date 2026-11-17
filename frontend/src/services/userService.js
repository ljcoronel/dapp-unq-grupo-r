import apiClient from './apiClient'

/**
 * Obtiene el perfil de un usuario por ID.
 * @param {number} userId
 * @returns {Promise<{ nombre: string }>}
 */
export const getUserProfile = async (userId) => {
  const response = await apiClient.get(`/users/${userId}`)
  return response.data
}
