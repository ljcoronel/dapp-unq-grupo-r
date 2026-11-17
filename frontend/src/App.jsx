import { Navigate, Route, Routes } from 'react-router'

import AppLayout from './components/AppLayout.jsx'
import ProtectedRoute from './components/ProtectedRoute.jsx'
import CatalogoPage from './pages/CatalogoPage.jsx'
import ComerciarPage from './pages/ComerciarPage.jsx'
import EstrategiaPage from './pages/EstrategiaPage.jsx'
import LoginPage from './pages/LoginPage.jsx'
import PortfolioPage from './pages/PortfolioPage.jsx'
import ProfilePage from './pages/ProfilePage.jsx'
import RegisterPage from './pages/RegisterPage.jsx'

function App() {
  return (
    <div className="min-h-screen bg-slate-100 text-slate-900">
      <Routes>
        <Route path="/" element={<Navigate to="/app/catalogo" replace />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        
        {/* Rutas protegidas anidadas en AppLayout */}
        <Route
          path="/app"
          element={
            <ProtectedRoute>
              <AppLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Navigate to="/app/catalogo" replace />} />
          <Route path="catalogo" element={<CatalogoPage />} />
          <Route path="comerciar" element={<ComerciarPage />} />
          <Route path="portfolio" element={<PortfolioPage />} />
          <Route path="estrategia" element={<EstrategiaPage />} />
          <Route path="perfil" element={<ProfilePage />} />
        </Route>

        {/* Redirección de compatibilidad para ruta previa */}
        <Route path="/home" element={<Navigate to="/app/catalogo" replace />} />
        
        {/* Redirección por defecto */}
        <Route path="*" element={<Navigate to="/app/catalogo" replace />} />
      </Routes>
    </div>
  )
}

export default App
