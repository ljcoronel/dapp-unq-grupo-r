import { Navigate, Route, Routes } from 'react-router'

function App() {
  return (
    <div className="min-h-screen bg-slate-100 text-slate-900">
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route
          path="*"
          element={
            <div className="flex min-h-screen items-center justify-center text-lg font-medium text-slate-600">
              Cargando autenticación...
            </div>
          }
        />
      </Routes>
    </div>
  )
}

export default App
