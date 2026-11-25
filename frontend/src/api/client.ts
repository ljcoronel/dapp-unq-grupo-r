import createClient from 'openapi-fetch'
import type { paths } from './schema'

export const apiClient = createClient<paths>({
  baseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
})

export class ApiRequestError extends Error {
  constructor(message: string, public readonly status?: number) {
    super(message)
    this.name = 'ApiRequestError'
  }
}

const getApiErrorMessage = (error: unknown): string | undefined => {
  if (!error || typeof error !== 'object') {
    return undefined
  }

  const errorBody = error as Record<string, unknown>
  const message = errorBody.mensaje ?? errorBody.message
  return typeof message === 'string' && message.trim() ? message : undefined
}

export const requireApiData = <T>(
  data: T | undefined,
  error: unknown,
  response: Response,
  fallbackMessage: string,
): T => {
  if (!response.ok || error) {
    throw new ApiRequestError(
      getApiErrorMessage(error) ?? fallbackMessage,
      response.status,
    )
  }

  if (data === undefined || data === null) {
    throw new ApiRequestError(fallbackMessage, response.status)
  }

  return data
}

export const getUserFacingError = (error: unknown, fallbackMessage: string): string => {
  if (error instanceof ApiRequestError) {
    return error.message
  }

  if (error instanceof TypeError) {
    return 'No se pudo conectar con el servidor. Intente nuevamente.'
  }

  return fallbackMessage
}
