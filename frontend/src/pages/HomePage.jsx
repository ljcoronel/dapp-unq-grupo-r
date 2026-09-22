import ProfileMenu from '../components/ProfileMenu.jsx'

export default function HomePage() {
  return (
    <div className="min-h-screen bg-slate-100">
      <nav className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-4">
          <div className="text-lg font-semibold text-slate-900">Home</div>
          <ProfileMenu />
        </div>
      </nav>
    </div>
  )
}
