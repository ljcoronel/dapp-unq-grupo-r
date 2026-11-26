import { Outlet } from 'react-router'

import ProfileMenu from './ProfileMenu'
import Sidebar from './Sidebar'

const AppLayout = () => (
  <div className="min-h-screen bg-slate-100 md:flex">
    <Sidebar />
    <div className="min-w-0 flex-1">
      <header className="flex h-16 items-center justify-between border-b border-slate-200 bg-white px-4 sm:px-6 lg:px-8">
        <p className="text-sm font-semibold text-slate-500">Mercado de Tokens</p>
        <ProfileMenu />
      </header>
      <main className="mx-auto w-full max-w-7xl px-4 py-6 sm:px-6 lg:px-8 lg:py-8">
        <Outlet />
      </main>
    </div>
  </div>
)

export default AppLayout
