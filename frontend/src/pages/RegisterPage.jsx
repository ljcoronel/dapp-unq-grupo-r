import { Link, useNavigate } from 'react-router'

import AuthForm from '../components/AuthForm.jsx'
import { useAuth } from '../context/AuthContext.jsx'

export default function RegisterPage() {
  const navigate = useNavigate()
  const { registration, isLoading, error, setError } = useAuth()

  const handleSubmit = async (nombre, password) => {
    setError('')

    try {
      await registration(nombre, password)
      navigate('/login', { replace: true })
    } catch {
      // The AuthContext already stores the user-facing error message.
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 px-4 py-12">
      <div className="w-full max-w-md">
        <AuthForm
          title="Crear cuenta"
          subtitle="Registra tu nombre de usuario y contraseña para continuar."
          submitLabel="Crear cuenta"
          isLoading={isLoading}
          error={error}
          onSubmit={handleSubmit}
        />

        <p className="mt-4 text-center text-sm text-slate-600">
          ¿Ya tienes cuenta?{' '}
          <Link to="/login" className="font-medium text-sky-600 hover:text-sky-700">
            Inicia sesión
          </Link>
        </p>
      </div>
    </div>
  )
}
