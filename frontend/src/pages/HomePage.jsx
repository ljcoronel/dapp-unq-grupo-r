export default function HomePage() {
  return (
    <div className="min-h-screen bg-slate-100">
      <nav className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-4">
          <div className="text-lg font-semibold text-slate-900">Home</div>
          <button
            type="button"
            className="inline-flex h-10 w-10 items-center justify-center rounded-full bg-slate-200 text-slate-700"
            aria-label="Abrir menú de perfil"
          >
            👤
          </button>
        </div>
      </nav>

      <main className="mx-auto max-w-5xl px-4 py-10">
        <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
          <h1 className="text-2xl font-bold text-slate-900">Bienvenido</h1>
          <p className="mt-2 text-slate-600">Tu sesión está activa y la vista protegida está disponible.</p>
        </div>
      </main>
    </div>
  )
}
