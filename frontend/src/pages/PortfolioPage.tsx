import { lazy, Suspense, useCallback, useEffect, useState } from 'react'

import { getUserFacingError } from '../api/client'
import FeedbackState from '../components/FeedbackState'
import HoldingList from '../components/HoldingList'
import TransactionHistory from '../components/TransactionHistory'
import { useAuth } from '../context/AuthContext'
import { getHoldings, type Holding } from '../services/holdingService'
import { getTransactions, type Transaction } from '../services/orderService'
import { getPlayers } from '../services/playerService'
import type { Player } from '../types/market'

const PortfolioPieChart = lazy(() => import('../components/PortfolioPieChart'))

const PortfolioPage = () => {
  const { userId } = useAuth()
  const [holdings, setHoldings] = useState<Holding[]>([])
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [players, setPlayers] = useState<Player[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')

  const loadPortfolio = useCallback(async () => {
    if (userId === null) {
      return
    }
    try {
      const [loadedHoldings, loadedTransactions, loadedPlayers] = await Promise.all([
        getHoldings(userId),
        getTransactions(userId),
        getPlayers(),
      ])
      setHoldings(loadedHoldings)
      setTransactions(loadedTransactions)
      setPlayers(loadedPlayers)
    } catch (requestError) {
      setError(getUserFacingError(requestError, 'No se pudo cargar el portfolio.'))
    } finally {
      setIsLoading(false)
    }
  }, [userId])

  useEffect(() => {
    if (userId === null) {
      return
    }
    let active = true
    void Promise.all([getHoldings(userId), getTransactions(userId), getPlayers()])
      .then(([loadedHoldings, loadedTransactions, loadedPlayers]) => {
        if (active) {
          setHoldings(loadedHoldings)
          setTransactions(loadedTransactions)
          setPlayers(loadedPlayers)
        }
      })
      .catch((requestError: unknown) => {
        if (active) {
          setError(getUserFacingError(requestError, 'No se pudo cargar el portfolio.'))
        }
      })
      .finally(() => {
        if (active) {
          setIsLoading(false)
        }
      })

    return () => {
      active = false
    }
  }, [userId])

  if (userId === null) {
    return <FeedbackState variant="error" message="No se pudo identificar al usuario en la sesión. Inicie sesión nuevamente." />
  }
  if (isLoading) {
    return <FeedbackState variant="loading" message="Cargando el portfolio..." />
  }
  if (error) {
    return <FeedbackState variant="error" message={error} action={{ label: 'Reintentar', onClick: () => void loadPortfolio() }} />
  }

  return (
    <section className="space-y-6">
      <header>
        <p className="text-sm font-semibold uppercase tracking-wide text-sky-700">Inversiones</p>
        <h1 className="mt-1 text-2xl font-bold tracking-tight text-slate-900">Portfolio</h1>
        <p className="mt-1 text-sm text-slate-600">Consulte sus tenencias, valuación actual e historial de operaciones.</p>
      </header>
      {holdings.length === 0 ? (
        <FeedbackState variant="empty" title="Portfolio vacío" message="Cuando concrete compras de tokens, sus tenencias aparecerán aquí." />
      ) : (
        <HoldingList holdings={holdings} players={players} />
      )}
      <TransactionHistory transactions={transactions} players={players} />
      {holdings.length > 0 && (
        <Suspense fallback={<FeedbackState variant="loading" message="Preparando el gráfico del portfolio..." />}>
          <PortfolioPieChart holdings={holdings} players={players} />
        </Suspense>
      )}
    </section>
  )
}

export default PortfolioPage
