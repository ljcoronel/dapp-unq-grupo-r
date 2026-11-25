import type { components } from '../api/schema'
import { apiClient, requireApiData } from '../api/client'

export type UserProfile = components['schemas']['UserResponse']

export const getUserProfile = async (userId: number): Promise<UserProfile> => {
  const { data, error, response } = await apiClient.GET('/users/{id}', {
    params: { path: { id: userId } },
  })

  return requireApiData(data, error, response, 'No se pudo obtener el perfil.')
}
