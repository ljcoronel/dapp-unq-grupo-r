import { useState } from 'react'

import { useAuth } from '../context/AuthContext.jsx'

export default function ProfileMenu() {
  const [isOpen, setIsOpen] = useState(false)
  const { logout } = useAuth()

  const handleLogout = () => {
    setIsOpen(false)
    logout()
  }

  return (
    <div className="relative">
      <button
        type="button"
        className="inline-flex h-10 w-10 items-center justify-center rounded-full bg-slate-200 text-slate-700 transition hover:bg-slate-300 focus:outline-none focus:ring-2 focus:ring-sky-500 focus:ring-offset-2"
        aria-label="Abrir menú de perfil"
        aria-expanded={isOpen}
        aria-haspopup="menu"
        onClick={() => setIsOpen((open) => !open)}
      >
        <svg
          aria-hidden="true"
          className="h-5 w-5"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.8"
          viewBox="0 0 24 24"
        >
          <path d="M20 21a8 8 0 0 0-16 0" strokeLinecap="round" />
          <circle cx="12" cy="7" r="4" />
        </svg>
      </button>

      {isOpen && (
        <div
          className="absolute right-0 z-10 mt-2 w-44 rounded-lg border border-slate-200 bg-white py-1 shadow-lg"
          role="menu"
        >
          <button
            type="button"
            className="block w-full px-4 py-2 text-left text-sm text-slate-700 hover:bg-slate-100"
            role="menuitem"
            onClick={() => setIsOpen(false)}
          >
            Ir a mi Perfil
          </button>
          <button
            type="button"
            className="block w-full px-4 py-2 text-left text-sm text-slate-700 hover:bg-slate-100"
            role="menuitem"
            onClick={handleLogout}
          >
            Cerrar sesión
          </button>
        </div>
      )}
    </div>
  )
}
