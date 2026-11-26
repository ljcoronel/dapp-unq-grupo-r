import type { components } from '../api/schema'
import { apiClient, requireApiData } from '../api/client'

export type StrategyState = components['schemas']['StrategyStateResponse']
export type StrategyRequest = components['schemas']['StrategyRequest']

export const getStrategy = async (userId: number): Promise<StrategyState> => {
  const { data, error, response } = await apiClient.GET('/strategy/{usuarioId}', {
    params: { path: { usuarioId: userId } },
  })

  return requireApiData(data, error, response, 'No se pudo obtener la estrategia.')
}

export const configureStrategy = async (
  userId: number,
  strategy: StrategyRequest,
): Promise<StrategyState> => {
  const { data, error, response } = await apiClient.PUT('/strategy/{usuarioId}/configure', {
    params: { path: { usuarioId: userId } },
    body: strategy,
  })

  return requireApiData(data, error, response, 'No se pudo guardar la estrategia.')
}
