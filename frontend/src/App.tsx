import { Navigate, Route, Routes } from 'react-router'

import AppLayout from './components/AppLayout'
import ProtectedRoute from './components/ProtectedRoute'
import CatalogoPage from './pages/CatalogoPage'
import ComerciarPage from './pages/ComerciarPage'
import EstrategiaPage from './pages/EstrategiaPage'
import LoginPage from './pages/LoginPage'
import PortfolioPage from './pages/PortfolioPage'
import ProfilePage from './pages/ProfilePage'
import RegisterPage from './pages/RegisterPage'
import { useAuth } from './context/AuthContext'

const HomeRedirect = () => {
  const { isAuthenticated } = useAuth()
  return <Navigate to={isAuthenticated ? '/app/catalogo' : '/login'} replace />
}

const App = () => {
  return (
    <div className="min-h-screen text-slate-900">
      <Routes>
        <Route path="/" element={<HomeRedirect />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route
          path="/app"
          element={
            <ProtectedRoute>
              <AppLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Navigate to="catalogo" replace />} />
          <Route path="catalogo" element={<CatalogoPage />} />
          <Route path="comerciar" element={<ComerciarPage />} />
          <Route path="portfolio" element={<PortfolioPage />} />
          <Route path="estrategia" element={<EstrategiaPage />} />
          <Route path="perfil" element={<ProfilePage />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </div>
  )
}

export default App
