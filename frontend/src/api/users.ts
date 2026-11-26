import { apiClient, requireApiData } from './client'

export const getUserProfile = async (id: number) => {
  const result = await apiClient.GET('/users/{id}', {
    params: { path: { id } },
  })

  return requireApiData(
    result.data,
    result.error,
    result.response,
    'No se pudo cargar el perfil de usuario.',
  )
}
