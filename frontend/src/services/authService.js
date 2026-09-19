import apiClient from './apiClient'

export async function login(nombre, password) {
  const response = await apiClient.post('/login', { nombre, password })
  const authorizationHeader =
    response.headers?.authorization || response.headers?.Authorization

  if (!authorizationHeader) {
    throw new Error('La respuesta del servidor no incluye un token válido.')
  }

  const token = authorizationHeader.startsWith('Bearer ')
    ? authorizationHeader.slice('Bearer '.length).trim()
    : authorizationHeader.trim()

  if (!token) {
    throw new Error('La respuesta del servidor no incluye un token válido.')
  }

  return {
    user: response.data,
    token,
  }
}

export async function register(nombre, password) {
  const response = await apiClient.post('/register', { nombre, password })
  return response.data
}
