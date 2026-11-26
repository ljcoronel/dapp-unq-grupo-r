import type { components } from './schema'
import { apiClient, requireApiData } from './client'

export type User = components['schemas']['UserResponse']

interface LoginResult {
  user: User
  token: string
}

const extractToken = (response: Response): string => {
  const authorizationHeader = response.headers.get('Authorization')

  if (!authorizationHeader?.startsWith('Bearer ')) {
    throw new Error('No se recibió un token de autenticación válido.')
  }

  const token = authorizationHeader.slice('Bearer '.length).trim()
  const tokenParts = token.split('.')

  if (!token || tokenParts.length !== 3 || tokenParts.some((part) => !part)) {
    throw new Error('No se recibió un token de autenticación válido.')
  }

  return token
}

export const login = async (nombre: string, password: string): Promise<LoginResult> => {
  const result = await apiClient.POST('/login', {
    body: { nombre, password },
  })
  const user = requireApiData(
    result.data,
    result.error,
    result.response,
    'No se pudo iniciar sesión. Intente nuevamente.',
  )

  return {
    user,
    token: extractToken(result.response),
  }
}

export const register = async (nombre: string, password: string): Promise<User> => {
  const result = await apiClient.POST('/register', {
    body: { nombre, password },
  })

  return requireApiData(
    result.data,
    result.error,
    result.response,
    'No se pudo crear la cuenta. Intente nuevamente.',
  )
}
