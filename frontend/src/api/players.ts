import type { components } from './schema'
import { apiClient, requireApiData } from './client'

export type Player = components['schemas']['PlayerResponse']

export const getPlayers = async (signal?: AbortSignal): Promise<Player[]> => {
  const result = await apiClient.GET('/players', { signal })

  return requireApiData(
    result.data,
    result.error,
    result.response,
    'No se pudo cargar el catálogo de jugadores.',
  )
}
