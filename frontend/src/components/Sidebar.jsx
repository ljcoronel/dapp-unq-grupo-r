import { useState } from 'react'
import { Link, useLocation } from 'react-router'

const NAV_ITEMS = [
  {
    path: '/app/catalogo',
    label: 'Catálogo',
    icon: (
      <svg
        className="h-5 w-5 shrink-0"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        viewBox="0 0 24 24"
        aria-hidden="true"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          d="M3.75 6A2.25 2.25 0 0 1 6 3.75h2.25A2.25 2.25 0 0 1 10.5 6v2.25a2.25 2.25 0 0 1-2.25 2.25H6a2.25 2.25 0 0 1-2.25-2.25V6ZM3.75 15.75A2.25 2.25 0 0 1 6 13.5h2.25a2.25 2.25 0 0 1 2.25 2.25V18a2.25 2.25 0 0 1-2.25 2.25H6A2.25 2.25 0 0 1 3.75 18v-2.25ZM13.5 6a2.25 2.25 0 0 1 2.25-2.25H18A2.25 2.25 0 0 1 20.25 6v2.25A2.25 2.25 0 0 1 18 10.5h-2.25a2.25 2.25 0 0 1-2.25-2.25V6ZM13.5 15.75a2.25 2.25 0 0 1 2.25-2.25H18a2.25 2.25 0 0 1 2.25 2.25V18A2.25 2.25 0 0 1 18 20.25h-2.25A2.25 2.25 0 0 1 13.5 18v-2.25Z"
        />
      </svg>
    ),
  },
  {
    path: '/app/comerciar',
    label: 'Comerciar',
    icon: (
      <svg
        className="h-5 w-5 shrink-0"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        viewBox="0 0 24 24"
        aria-hidden="true"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          d="M7.5 21 3 16.5m0 0L7.5 12M3 16.5h13.5m0-13.5L21 7.5m0 0L16.5 12M21 7.5H7.5"
        />
      </svg>
    ),
  },
  {
    path: '/app/portfolio',
    label: 'Portfolio',
    icon: (
      <svg
        className="h-5 w-5 shrink-0"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        viewBox="0 0 24 24"
        aria-hidden="true"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          d="M2.25 18.75a60.07 60.07 0 0 1 15.797 2.101c.727.198 1.453-.342 1.453-1.096V18.75M3.75 4.5v.75A.75.75 0 0 1 3 6H2.25m0 0a3 3 0 0 0-3 3v8.25a3 3 0 0 0 3 3h16.5a3 3 0 0 0 3-3V9a3 3 0 0 0-3-3h-1.5m-15 0a3 3 0 0 1 3-3h12a3 3 0 0 1 3 3m-15 0h15M9 12a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0Zm8.25 0a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0Z"
        />
      </svg>
    ),
  },
  {
    path: '/app/estrategia',
    label: 'Estrategia',
    icon: (
      <svg
        className="h-5 w-5 shrink-0"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        viewBox="0 0 24 24"
        aria-hidden="true"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          d="M10.5 6h9.75M10.5 6a1.5 1.5 0 1 1-3 0m3 0a1.5 1.5 0 1 0-3 0M3.75 6H7.5m3 12h9.75m-9.75 0a1.5 1.5 0 0 1-3 0m3 0a1.5 1.5 0 0 0-3 0m-3.75 0H7.5m9-6h3.75m-3.75 0a1.5 1.5 0 0 1-3 0m3 0a1.5 1.5 0 0 0-3 0m-9.75 0h9.75"
        />
      </svg>
    ),
  },
]

export default function Sidebar() {
  const location = useLocation()
  const [isCollapsed, setIsCollapsed] = useState(false)

  const isCurrentActive = (path) => {
    if (path === '/app/catalogo') {
      return (
        location.pathname === '/app/catalogo' ||
        location.pathname === '/app' ||
        location.pathname === '/app/'
      )
    }
    return location.pathname.startsWith(path)
  }

  return (
    <aside
      aria-label="Navegación principal"
      className={`flex flex-col border-r border-slate-800 bg-slate-900 text-slate-100 transition-all duration-200 ${
        isCollapsed ? 'w-16 md:w-20' : 'w-16 sm:w-20 md:w-64'
      }`}
    >
      {/* Brand / Title & Collapse Toggle */}
      <div className="flex h-16 items-center justify-between border-b border-slate-800 px-3 md:px-4">
        <Link
          to="/app/catalogo"
          className="flex items-center gap-2 overflow-hidden font-bold tracking-tight text-white focus:outline-none focus:ring-2 focus:ring-sky-500 rounded-md"
          title="Mercado de Tokens"
        >
          <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-sky-600 font-black text-white">
            M
          </div>
          <span
            className={`whitespace-nowrap text-base font-semibold transition-opacity duration-200 ${
              isCollapsed ? 'hidden' : 'hidden md:inline'
            }`}
          >
            Token Market
          </span>
        </Link>

        <button
          type="button"
          onClick={() => setIsCollapsed((prev) => !prev)}
          className="hidden md:inline-flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 hover:bg-slate-800 hover:text-white focus:outline-none focus:ring-2 focus:ring-sky-500"
          aria-label={isCollapsed ? 'Expandir barra lateral' : 'Colapsar barra lateral'}
          title={isCollapsed ? 'Expandir' : 'Colapsar'}
        >
          <svg
            className={`h-4 w-4 transform transition-transform ${isCollapsed ? 'rotate-180' : ''}`}
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            viewBox="0 0 24 24"
          >
            <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 19.5 8.25 12l7.5-7.5" />
          </svg>
        </button>
      </div>

      {/* Navigation links */}
      <nav className="flex-1 space-y-1.5 p-2 md:p-3 overflow-y-auto">
        {NAV_ITEMS.map((item) => {
          const active = isCurrentActive(item.path)
          return (
            <Link
              key={item.path}
              to={item.path}
              aria-current={active ? 'page' : undefined}
              title={item.label}
              className={`group flex items-center gap-3 rounded-lg px-2.5 py-2.5 text-sm font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-sky-500 ${
                active
                  ? 'bg-sky-600 text-white shadow-sm'
                  : 'text-slate-300 hover:bg-slate-800 hover:text-white'
              }`}
            >
              {item.icon}
              <span
                className={`truncate ${
                  isCollapsed ? 'hidden' : 'hidden md:inline'
                }`}
              >
                {item.label}
              </span>
            </Link>
          )
        })}
      </nav>
    </aside>
  )
}
