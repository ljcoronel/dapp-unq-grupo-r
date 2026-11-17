import { useEffect, useMemo, useState } from 'react'

import FeedbackState from '../components/FeedbackState.jsx'
import HoldingList from '../components/HoldingList.jsx'
import TransactionHistory from '../components/TransactionHistory.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { getHoldings } from '../services/holdingService.js'
import { getTransactions } from '../services/orderService.js'
import { fetchPlayers } from '../services/playerService.js'
import { normalizePlayers } from '../utils/playerUtils.js'

export default function PortfolioPage() {
  const { userId } = useAuth()

  const [holdings, setHoldings] = useState([])
  const [transactions, setTransactions] = useState([])
  const [players, setPlayers] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadTrigger, setReloadTrigger] = useState(0)

  useEffect(() => {
    if (!userId) {
      return
    }

    let isCancelled = false

    const loadPortfolioData = async () => {
      try {
        const [holdingsData, transactionsData, playersData] = await Promise.all([
          getHoldings(userId).catch(() => []),
          getTransactions(userId).catch(() => []),
          fetchPlayers().catch(() => []),
        ])

        if (isCancelled) return

        setHoldings(Array.isArray(holdingsData) ? holdingsData : [])
        setTransactions(Array.isArray(transactionsData) ? transactionsData : [])
        setPlayers(normalizePlayers(playersData))
      } catch (err) {
        if (isCancelled) return
        const msg =
          err.response?.data?.mensaje ||
          err.response?.data?.message ||
          'Error al cargar la información del portfolio.'
        setError(msg)
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    loadPortfolioData()

    return () => {
      isCancelled = true
    }
  }, [userId, reloadTrigger])

  const handleRetry = () => {
    setIsLoading(true)
    setError('')
    setReloadTrigger((count) => count + 1)
  }

  // Métricas agregadas
  const metrics = useMemo(() => {
    const totalTokens = holdings.reduce((sum, h) => sum + (h.tokens || 0), 0)
    const totalValue = holdings.reduce((sum, h) => {
      const val =
        h.valorTotal !== null && h.valorTotal !== undefined
          ? Number(h.valorTotal)
          : Number(h.tokens || 0) * Number(h.valorUnitario || 0)
      return sum + (Number.isFinite(val) ? val : 0)
    }, 0)
    const activeHoldingsCount = holdings.filter((h) => (h.tokens || 0) > 0).length

    return {
      totalTokens,
      totalValue,
      activeHoldingsCount,
    }
  }, [holdings])

  return (
    <div className="mx-auto max-w-7xl space-y-6">
      {/* Encabezado */}
      <header className="border-b border-slate-200 pb-5">
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">
          Mi Portfolio de Tokens
        </h1>
        <p className="mt-1 text-sm text-slate-600">
          Consulte el valor y cantidad de sus tenencias de jugadores, y revise el
          historial de operaciones ejecutadas.
        </p>
      </header>

      {/* Tarjetas de métricas rápidas */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
          <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
            Valor Total del Portfolio
          </p>
          <p className="mt-2 text-2xl font-black text-sky-700">
            ${metrics.totalValue.toFixed(2)}
          </p>
        </div>

        <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
          <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
            Tokens en Tenencia
          </p>
          <p className="mt-2 text-2xl font-black text-slate-900">
            {metrics.totalTokens}
          </p>
        </div>

        <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
          <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
            Jugadores en Cartera
          </p>
          <p className="mt-2 text-2xl font-black text-slate-900">
            {metrics.activeHoldingsCount}
          </p>
        </div>
      </div>

      {/* Manejo de carga / error / contenido (T050) */}
      <FeedbackState
        isLoading={isLoading}
        error={error}
        onRetry={handleRetry}
        loadingMessage="Cargando tenencias y transacciones..."
      >
        <div className="space-y-8">
          {/* Tabla de tenencias (T046, T048) */}
          <HoldingList holdings={holdings} players={players} />

          {/* Historial de transacciones ejecutadas (T047, T049) */}
          <TransactionHistory transactions={transactions} players={players} />
        </div>
      </FeedbackState>
    </div>
  )
}
