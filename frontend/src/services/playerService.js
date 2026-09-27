import apiClient from './apiClient'

/**
 * Obtiene el catálogo de jugadores agrupados por liga desde la API pública.
 * No adjunta encabezado Authorization (JWT) ya que es un endpoint público.
 *
 * @returns {Promise<Array<Array<Object>>>} Arreglo con 5 listas de jugadores por liga.
 */
export const fetchPlayers = async () => {
  const response = await apiClient.get('/players')
  return response.data
}

export const getPlayers = fetchPlayers
