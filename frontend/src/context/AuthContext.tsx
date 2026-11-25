import { createContext, useCallback, useContext, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { useNavigate } from 'react-router'

import { login as loginRequest, register as registerRequest, type User } from '../api/auth'
import { getUserFacingError } from '../api/client'

const STORAGE_KEY = 'authToken'

interface AuthContextValue {
  authToken: string | null
  isAuthenticated: boolean
  isLoading: boolean
  error: string
  login: (nombre: string, password: string) => Promise<User>
  registration: (nombre: string, password: string) => Promise<User>
  logout: () => void
  setError: (message: string) => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

const getStoredToken = (): string | null => {
  if (typeof window === 'undefined') {
    return null
  }

  return window.localStorage.getItem(STORAGE_KEY)
}

interface AuthProviderProps {
  children: ReactNode
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const navigate = useNavigate()
  const [authToken, setAuthToken] = useState(() => getStoredToken())
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')

  const isAuthenticated = Boolean(authToken)

  const persistToken = (nextToken: string | null) => {
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

  const login = useCallback(async (nombre: string, password: string) => {
    setIsLoading(true)
    setError('')

    try {
      const { user, token } = await loginRequest(nombre, password)
      setAuthToken(token)
      persistToken(token)
      return user
    } catch (requestError) {
      const message = getUserFacingError(requestError, 'No se pudo iniciar sesión.')
      setError(message)
      throw requestError
    } finally {
      setIsLoading(false)
    }
  }, [])

  const registration = useCallback(async (nombre: string, password: string) => {
    setIsLoading(true)
    setError('')

    try {
      const user = await registerRequest(nombre, password)
      return user
    } catch (requestError) {
      const message = getUserFacingError(requestError, 'No se pudo crear la cuenta.')
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
export const useAuth = (): AuthContextValue => {
  const context = useContext(AuthContext)

  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }

  return context
}
/* eslint-enable react-refresh/only-export-components */
