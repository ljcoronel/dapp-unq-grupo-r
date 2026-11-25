import { Link, useNavigate } from 'react-router'
import type { MouseEvent } from 'react'

import AuthForm from '../components/AuthForm'
import { useAuth } from '../context/AuthContext'

const LoginPage = () => {
  const navigate = useNavigate()
  const { login, isLoading, error, setError } = useAuth()

  const handleRegisterNavigation = (event: MouseEvent<HTMLAnchorElement>) => {
    if (isLoading) {
      event.preventDefault()
      return
    }

    setError('')
  }

  const handleSubmit = async (nombre: string, password: string) => {
    setError('')

    try {
      await login(nombre, password)
      navigate('/app/catalogo', { replace: true })
    } catch {
      // The AuthContext already stores the user-facing error message.
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 px-4 py-12">
      <div className="w-full max-w-md">
        <AuthForm
          title="Iniciar sesión"
          subtitle="Ingresa tus credenciales para acceder a tu cuenta."
          submitLabel="iniciar sesión"
          isLoading={isLoading}
          error={error}
          showCredentialRequirements={false}
          showValidationErrors={false}
          onSubmit={handleSubmit}
        />

        <p className="mt-4 text-center text-sm text-slate-600">
          ¿Todavía no tienes una cuenta?{' '}
          <Link
            to="/register"
            onClick={handleRegisterNavigation}
            aria-disabled={isLoading}
            className="font-medium text-sky-600 hover:text-sky-700"
          >
            Crear cuenta
          </Link>
        </p>
      </div>
    </div>
  )
}

export default LoginPage
