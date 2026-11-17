import { Outlet } from 'react-router'

import ProfileMenu from './ProfileMenu.jsx'
import Sidebar from './Sidebar.jsx'

export default function AppLayout() {
  return (
    <div className="flex h-screen overflow-hidden bg-slate-100 text-slate-900">
      <Sidebar />
      <div className="flex flex-1 flex-col overflow-hidden">
        <header className="flex h-16 shrink-0 items-center justify-between border-b border-slate-200 bg-white px-4 md:px-6 shadow-sm">
          <div className="text-base md:text-lg font-semibold text-slate-800">
            Mercado de Tokens
          </div>
          <div className="flex items-center gap-3">
            <ProfileMenu />
          </div>
        </header>
        <main className="flex-1 overflow-y-auto p-4 md:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
