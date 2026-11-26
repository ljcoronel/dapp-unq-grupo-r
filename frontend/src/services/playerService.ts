import type { components } from '../api/schema'
import { apiClient, requireApiData } from '../api/client'

export type Player = components['schemas']['PlayerResponse']
export type Valuation = components['schemas']['ValuationResponse']

export const getPlayers = async (signal?: AbortSignal): Promise<Player[]> => {
  const { data, error, response } = await apiClient.GET('/players', { signal })
  return requireApiData(data, error, response, 'No se pudo cargar el catálogo de jugadores.')
}

export const getCurrentValuation = async (
  playerId: number,
  userId: number,
): Promise<Valuation> => {
  console.log("Pasé acá");
  const { data, error, response } = await apiClient.GET(
    '/players/{playerId}/quotes/{userId}/current',
    { params: { path: { playerId, userId } } },
  )

  return requireApiData(data, error, response, 'No se pudo obtener la cotización actual.')
}

export const getPlayerValuations = async (
  playerId: number,
  userId: number,
): Promise<Valuation[]> => {
  const { data, error, response } = await apiClient.GET(
    '/players/{playerId}/quotes/{userId}',
    { params: { path: { playerId, userId } } },
  )

  return requireApiData(data, error, response, 'No se pudieron obtener las cotizaciones.')
}

export const getClosestValuation = async (
  playerId: number,
  userId: number,
  date: string,
): Promise<Valuation> => {
  const { data, error, response } = await apiClient.GET(
    '/players/{playerId}/quotes/{userId}/closest',
    { params: { path: { playerId, userId }, query: { date } } },
  )

  return requireApiData(data, error, response, 'No se pudo obtener la cotización solicitada.')
}
