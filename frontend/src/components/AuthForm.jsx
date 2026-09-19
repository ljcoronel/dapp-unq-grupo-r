import { useState } from 'react'

import { validateNombre, validatePassword } from '../utils/validation'

export default function AuthForm({
  title,
  subtitle,
  submitLabel,
  isLoading = false,
  error = '',
  onSubmit,
}) {
  const [formData, setFormData] = useState({ nombre: '', password: '' })

  const nombreError = validateNombre(formData.nombre)
  const passwordError = validatePassword(formData.password)
  const isDisabled = isLoading || !!nombreError || !!passwordError || !formData.nombre || !formData.password

  const handleChange = (event) => {
    const { name, value } = event.target
    setFormData((current) => ({
      ...current,
      [name]: value,
    }))
  }

  const handleSubmit = (event) => {
    event.preventDefault()

    if (isDisabled) {
      return
    }

    onSubmit(formData.nombre, formData.password)
  }

  return (
    <div className="mx-auto w-full max-w-md rounded-2xl border border-slate-200 bg-white p-8 shadow-sm">
      <div className="mb-6">
        {title ? <h1 className="text-2xl font-bold text-slate-900">{title}</h1> : null}
        {subtitle ? <p className="mt-2 text-sm text-slate-600">{subtitle}</p> : null}
      </div>

      <form className="space-y-5" onSubmit={handleSubmit} noValidate>
        <div>
          <label htmlFor="nombre" className="mb-2 block text-sm font-medium text-slate-700">
            Nombre de usuario
          </label>
          <input
            id="nombre"
            name="nombre"
            type="text"
            value={formData.nombre}
            onChange={handleChange}
            className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm text-slate-900 outline-none transition focus:border-sky-500 focus:ring-2 focus:ring-sky-100"
            placeholder="Ingrese su nombre"
            autoComplete="username"
          />
          {nombreError ? <p className="mt-2 text-sm text-red-600">{nombreError}</p> : null}
        </div>

        <div>
          <label htmlFor="password" className="mb-2 block text-sm font-medium text-slate-700">
            Contraseña
          </label>
          <input
            id="password"
            name="password"
            type="password"
            value={formData.password}
            onChange={handleChange}
            className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm text-slate-900 outline-none transition focus:border-sky-500 focus:ring-2 focus:ring-sky-100"
            placeholder="Ingrese su contraseña"
            autoComplete="current-password"
          />
          {passwordError ? <p className="mt-2 text-sm text-red-600">{passwordError}</p> : null}
        </div>

        <ul className="space-y-1 text-sm text-slate-600">
          <li>• El nombre debe tener entre 4 y 16 caracteres.</li>
          <li>• El nombre no puede contener espacios.</li>
          <li>• La contraseña debe tener entre 4 y 16 caracteres.</li>
        </ul>

        {error ? (
          <div className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            {error}
          </div>
        ) : null}

        <button
          type="submit"
          disabled={isDisabled}
          className="w-full rounded-lg bg-sky-600 px-4 py-2.5 font-medium text-white transition hover:bg-sky-700 disabled:cursor-not-allowed disabled:bg-slate-300"
        >
          {isLoading ? 'Cargando...' : submitLabel}
        </button>
      </form>
    </div>
  )
}
