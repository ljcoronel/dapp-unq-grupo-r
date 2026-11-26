import { useCallback, useEffect, useState } from 'react'

import { getUserFacingError } from '../api/client'
import FeedbackState from '../components/FeedbackState'
import { useAuth } from '../context/AuthContext'
import { getUserProfile, type UserProfile } from '../services/userService'

const ProfilePage = () => {
  const { userId } = useAuth()
  const [profile, setProfile] = useState<UserProfile | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')

  const loadProfile = useCallback(async () => {
    if (userId === null) {
      return
    }
    try {
      setProfile(await getUserProfile(userId))
    } catch (requestError) {
      setError(getUserFacingError(requestError, 'No se pudo cargar el perfil.'))
    } finally {
      setIsLoading(false)
    }
  }, [userId])

  useEffect(() => {
    if (userId === null) {
      return
    }
    let active = true
    void getUserProfile(userId)
      .then((loadedProfile) => {
        if (active) {
          setProfile(loadedProfile)
        }
      })
      .catch((requestError: unknown) => {
        if (active) {
          setError(getUserFacingError(requestError, 'No se pudo cargar el perfil.'))
        }
      })
      .finally(() => {
        if (active) {
          setIsLoading(false)
        }
      })

    return () => {
      active = false
    }
  }, [userId])

  if (userId === null) {
    return <FeedbackState variant="error" message="No se pudo identificar al usuario en la sesión. Inicie sesión nuevamente." />
  }
  if (isLoading) {
    return <FeedbackState variant="loading" message="Cargando el perfil..." />
  }
  if (error) {
    return <FeedbackState variant="error" message={error} action={{ label: 'Reintentar', onClick: () => void loadProfile() }} />
  }

  return (
    <section className="space-y-6">
      <header>
        <p className="text-sm font-semibold uppercase tracking-wide text-sky-700">Cuenta</p>
        <h1 className="mt-1 text-2xl font-bold tracking-tight text-slate-900">Mi perfil</h1>
      </header>
      <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <p className="text-sm text-slate-500">Nombre de usuario</p>
        <p className="mt-2 text-xl font-semibold text-slate-900">{profile?.nombre?.trim() || 'Nombre no disponible'}</p>
      </div>
    </section>
  )
}

export default ProfilePage
