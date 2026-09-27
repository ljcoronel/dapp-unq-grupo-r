import { createContext, useCallback, useContext, useMemo, useState } from 'react'
import { useNavigate } from 'react-router'

import { login as loginRequest, register as registerRequest } from '../services/authService'

const STORAGE_KEY = 'authToken'

const AuthContext = createContext(null)

function getStoredToken() {
  if (typeof window === 'undefined') {
    return null
  }

  return window.localStorage.getItem(STORAGE_KEY)
}

function getErrorMessage(error) {
  if (!error) {
    return 'Ocurrió un error inesperado.'
  }

  if (error.message === 'AUTH_TOKEN_INVALID') {
    return 'No se pudo completar el inicio de sesión. Intente nuevamente.'
  }

  if (error.response?.data?.message) {
    return error.response.data.message
  }

  if (error.response?.status === 401) {
    return 'Nombre de usuario o contraseña incorrectos.'
  }

  if (
    error.response?.status === 400 &&
    error.response?.data?.message?.toLowerCase().includes('credencial')
  ) {
    return 'Nombre de usuario o contraseña incorrectos.'
  }

  if (error.request || error.code === 'ERR_NETWORK') {
    return 'No se pudo conectar con el servidor. Intente nuevamente.'
  }

  return error.message || 'Ocurrió un error inesperado.'
}

export function AuthProvider({ children }) {
  const navigate = useNavigate()
  const [authToken, setAuthToken] = useState(() => getStoredToken())
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')

  const isAuthenticated = Boolean(authToken)

  const persistToken = (nextToken) => {
    if (!nextToken) {
      window.localStorage.removeItem(STORAGE_KEY)
      return
    }

    window.localStorage.setItem(STORAGE_KEY, nextToken)
  }

  const clearSession = useCallback(() => {
    setAuthToken(null)
    setError('')
    persistToken(null)
  }, [])

  const login = useCallback(async (nombre, password) => {
    setIsLoading(true)
    setError('')

    try {
      const { user, token } = await loginRequest(nombre, password)
      setAuthToken(token)
      persistToken(token)
      return user
    } catch (requestError) {
      const message = getErrorMessage(requestError)
      setError(message)
      throw requestError
    } finally {
      setIsLoading(false)
    }
  }, [])

  const registration = useCallback(async (nombre, password) => {
    setIsLoading(true)
    setError('')

    try {
      const user = await registerRequest(nombre, password)
      return user
    } catch (requestError) {
      const message = getErrorMessage(requestError)
      setError(message)
      throw requestError
    } finally {
      setIsLoading(false)
    }
  }, [])

  const logout = useCallback(() => {
    clearSession()
    navigate('/login', { replace: true })
  }, [clearSession, navigate])

  const value = useMemo(
    () => ({
      authToken,
      isAuthenticated,
      isLoading,
      error,
      login,
      registration,
      logout,
      setError,
    }),
    [authToken, isAuthenticated, isLoading, error, login, registration, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

/* eslint-disable react-refresh/only-export-components */
export function useAuth() {
  const context = useContext(AuthContext)

  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }

  return context
}
/* eslint-enable react-refresh/only-export-components */
