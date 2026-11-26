import { useState } from 'react'
import { NavLink } from 'react-router'

const links = [
  { to: '/app/catalogo', label: 'Catálogo', icon: '⚽' },
  { to: '/app/comerciar', label: 'Comerciar', icon: '↔' },
  { to: '/app/portfolio', label: 'Portfolio', icon: '▤' },
  { to: '/app/estrategia', label: 'Estrategia', icon: '◈' },
]

const Sidebar = () => {
  const [isOpen, setIsOpen] = useState(false)

  return (
    <aside className="w-full shrink-0 border-b border-slate-200 bg-white md:min-h-screen md:w-60 md:border-b-0 md:border-r">
      <div className="flex items-center justify-between px-4 py-4 md:block md:px-5 md:py-7">
        <NavLink
          to="/app/catalogo"
          className="text-lg font-bold tracking-tight text-slate-900"
          onClick={() => setIsOpen(false)}
        >
          Mercado de Tokens
        </NavLink>
        <button
          type="button"
          className="rounded-lg border border-slate-300 px-3 py-2 text-sm md:hidden"
          aria-expanded={isOpen}
          aria-controls="app-navigation"
          onClick={() => setIsOpen((open) => !open)}
        >
          {isOpen ? 'Cerrar' : 'Menú'}
        </button>
      </div>
      <nav
        id="app-navigation"
        aria-label="Navegación principal"
        className={`${isOpen ? 'block' : 'hidden'} space-y-1 px-3 pb-4 md:block md:px-3 md:pb-6`}
      >
        {links.map(({ to, label, icon }) => (
          <NavLink
            key={to}
            to={to}
            onClick={() => setIsOpen(false)}
            className={({ isActive }) =>
              `flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition ${
                isActive
                  ? 'bg-sky-100 text-sky-800'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
              }`
            }
          >
            <span aria-hidden="true" className="w-5 text-center text-base">{icon}</span>
            {label}
          </NavLink>
        ))}
      </nav>
    </aside>
  )
}

export default Sidebar
