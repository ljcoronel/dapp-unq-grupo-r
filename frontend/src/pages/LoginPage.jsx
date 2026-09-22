import { Link, useNavigate } from 'react-router'

import AuthForm from '../components/AuthForm.jsx'
import { useAuth } from '../context/AuthContext.jsx'

export default function LoginPage() {
  const navigate = useNavigate()
  const { login, isLoading, error, setError } = useAuth()

  const handleSubmit = async (nombre, password) => {
    setError('')

    try {
      await login(nombre, password)
      navigate('/home', { replace: true })
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
          submitLabel="Iniciar sesión"
          isLoading={isLoading}
          error={error}
          onSubmit={handleSubmit}
        />

        <p className="mt-4 text-center text-sm text-slate-600">
          ¿No tienes cuenta?{' '}
          <Link to="/register" className="font-medium text-sky-600 hover:text-sky-700">
            Crear cuenta
          </Link>
        </p>
      </div>
    </div>
  )
}
