import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router'

import { getUserFacingError } from '../api/client'
import BuyModal from '../components/BuyModal'
import FeedbackState from '../components/FeedbackState'
import OrderList from '../components/OrderList'
import SellModal from '../components/SellModal'
import { useAuth } from '../context/AuthContext'
import { getHoldings, type Holding } from '../services/holdingService'
import {
  cancelOrder,
  createBuyOrder,
  createSellOrder,
  getPendingOrders,
  type PendingOrder,
} from '../services/orderService'
import { getPlayers } from '../services/playerService'
import { getStrategy, type StrategyState } from '../services/strategyService'
import type { Player } from '../types/market'
import { isBuyOperation, isSellOperation } from '../utils/orderValidation'

const ComerciarPage = () => {
  const { userId } = useAuth()
  const [players, setPlayers] = useState<Player[]>([])
  const [holdings, setHoldings] = useState<Holding[]>([])
  const [orders, setOrders] = useState<PendingOrder[]>([])
  const [strategy, setStrategy] = useState<StrategyState | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isMutating, setIsMutating] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [isBuyOpen, setIsBuyOpen] = useState(false)
  const [isSellOpen, setIsSellOpen] = useState(false)

  const loadData = useCallback(async (showLoading = true) => {
    if (userId === null) {
      return
    }
    if (showLoading) {
      setIsLoading(true)
      setError('')
    }
    try {
      const [loadedPlayers, loadedOrders, loadedHoldings, loadedStrategy] = await Promise.all([
        getPlayers(),
        getPendingOrders(userId),
        getHoldings(userId),
        getStrategy(userId),
      ])
      setPlayers(loadedPlayers)
      setOrders(loadedOrders)
      setHoldings(loadedHoldings)
      setStrategy(loadedStrategy)
    } catch (requestError) {
      setError(getUserFacingError(requestError, 'No se pudieron cargar los datos de comercio.'))
    } finally {
      setIsLoading(false)
    }
  }, [userId])

  useEffect(() => {
    if (userId === null) {
      return
    }
    let active = true
    void Promise.all([
      getPlayers(),
      getPendingOrders(userId),
      getHoldings(userId),
      getStrategy(userId),
    ])
      .then(([loadedPlayers, loadedOrders, loadedHoldings, loadedStrategy]) => {
        if (active) {
          setPlayers(loadedPlayers)
          setOrders(loadedOrders)
          setHoldings(loadedHoldings)
          setStrategy(loadedStrategy)
        }
      })
      .catch((requestError: unknown) => {
        if (active) {
          setError(getUserFacingError(requestError, 'No se pudieron cargar los datos de comercio.'))
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

  const ensureStrategy = (action: 'comprar' | 'vender') => {
    if (!strategy?.estrategia) {
      setNotice(`Debe configurar una estrategia de valuación antes de ${action}.`)
      return false
    }
    setNotice('')
    return true
  }

  const handleBuy = async (order: Parameters<typeof createBuyOrder>[0]) => {
    setIsMutating(true)
    try {
      await createBuyOrder(order)
      setNotice('La orden de compra se creó correctamente.')
      await loadData(false)
    } finally {
      setIsMutating(false)
    }
  }

  const handleSell = async (order: Parameters<typeof createSellOrder>[0]) => {
    setIsMutating(true)
    try {
      await createSellOrder(order)
      setNotice('La orden de venta se creó correctamente.')
      await loadData(false)
    } finally {
      setIsMutating(false)
    }
  }

  const handleCancel = async (orderId: number) => {
    if (userId === null) {
      setNotice('No se pudo identificar al usuario en la sesión.')
      return
    }
    setIsMutating(true)
    setNotice('')
    try {
      await cancelOrder(orderId, userId)
      setNotice('La orden se canceló correctamente.')
      await loadData(false)
    } catch (requestError) {
      setNotice(getUserFacingError(requestError, 'No se pudo cancelar la orden.'))
    } finally {
      setIsMutating(false)
    }
  }

  const buy = (order: Parameters<typeof createBuyOrder>[0]) =>
    handleBuy(order).catch((requestError: unknown) => {
      throw new Error(getUserFacingError(requestError, 'No se pudo enviar la orden de compra.'))
    })
  const sell = (order: Parameters<typeof createSellOrder>[0]) =>
    handleSell(order).catch((requestError: unknown) => {
      throw new Error(getUserFacingError(requestError, 'No se pudo enviar la orden de venta.'))
    })

  if (userId === null) {
    return <FeedbackState variant="error" message="No se pudo identificar al usuario en la sesión. Inicie sesión nuevamente." />
  }

  if (isLoading) {
    return <FeedbackState variant="loading" message="Cargando órdenes y datos de comercio..." />
  }

  if (error) {
    return <FeedbackState variant="error" message={error} action={{ label: 'Reintentar', onClick: () => void loadData() }} />
  }

  return (
    <section className="space-y-6">
      <header className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-sm font-semibold uppercase tracking-wide text-sky-700">Mercado</p>
          <h1 className="mt-1 text-2xl font-bold tracking-tight text-slate-900">Comerciar</h1>
          <p className="mt-1 text-sm text-slate-600">Administre órdenes activas de compra y venta.</p>
        </div>
        <div className="flex flex-col gap-2 sm:flex-row">
          <button
            type="button"
            disabled={isMutating}
            onClick={() => ensureStrategy('comprar') && setIsBuyOpen(true)}
            className="rounded-lg bg-sky-700 px-4 py-2.5 text-sm font-semibold text-white hover:bg-sky-800 disabled:opacity-60"
          >
            Comprar
          </button>
          <button
            type="button"
            disabled={isMutating}
            onClick={() => ensureStrategy('vender') && setIsSellOpen(true)}
            className="rounded-lg border border-slate-300 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-50 disabled:opacity-60"
          >
            Vender
          </button>
        </div>
      </header>

      {!strategy?.estrategia && (
        <div className="rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">
          Debe configurar una estrategia de valuación antes de operar.{' '}
          <Link to="/app/estrategia" className="font-semibold underline">Configurar estrategia</Link>
        </div>
      )}
      {notice && <p role="status" className="rounded-lg border border-sky-200 bg-sky-50 p-3 text-sm text-sky-900">{notice}</p>}

      <div className="grid gap-5 xl:grid-cols-2">
        <OrderList orders={orders.filter((order) => isBuyOperation(order.operation))} type="BUY" players={players} onCancel={(id) => void handleCancel(id)} />
        <OrderList orders={orders.filter((order) => isSellOperation(order.operation))} type="SELL" players={players} onCancel={(id) => void handleCancel(id)} />
      </div>

      {userId !== null && (
        <>
          <BuyModal
            isOpen={isBuyOpen}
            players={players}
            userId={userId}
            onConfirm={buy}
            onClose={() => setIsBuyOpen(false)}
          />
          <SellModal
            isOpen={isSellOpen}
            players={players}
            holdings={holdings}
            pendingOrders={orders}
            userId={userId}
            onConfirm={sell}
            onClose={() => setIsSellOpen(false)}
          />
        </>
      )}
    </section>
  )
}

export default ComerciarPage
