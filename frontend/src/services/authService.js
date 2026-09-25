import apiClient from './apiClient'

const INVALID_TOKEN_ERROR = 'AUTH_TOKEN_INVALID'

function extractToken(response) {
  const authorizationHeader =
    response.headers?.authorization || response.headers?.Authorization

  if (typeof authorizationHeader !== 'string' || !authorizationHeader.startsWith('Bearer ')) {
    throw new Error(INVALID_TOKEN_ERROR)
  }

  const token = authorizationHeader.slice('Bearer '.length).trim()
  const tokenParts = token.split('.')

  if (!token || tokenParts.length !== 3 || tokenParts.some((part) => !part)) {
    throw new Error(INVALID_TOKEN_ERROR)
  }

  return token
}

export async function login(nombre, password) {
  const response = await apiClient.post('/login', { nombre, password })

  return {
    user: response.data,
    token: extractToken(response),
  }
}

export async function register(nombre, password) {
  const response = await apiClient.post('/register', { nombre, password })
  return response.data
}
