import type { components } from '../api/schema'
import { apiClient, requireApiData } from '../api/client'

export type Holding = components['schemas']['HoldingResponse']

export const getHoldings = async (userId: number): Promise<Holding[]> => {
  const { data, error, response } = await apiClient.GET('/users/{usuarioId}/holdings', {
    params: { path: { usuarioId: userId } },
  })

  return requireApiData(data, error, response, 'No se pudieron obtener las tenencias.')
}
