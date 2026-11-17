import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router'

import BuyModal from '../components/BuyModal.jsx'
import FeedbackState from '../components/FeedbackState.jsx'
import OrderList from '../components/OrderList.jsx'
import SellModal from '../components/SellModal.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { getHoldings } from '../services/holdingService.js'
import { cancelOrder, getPendingOrders } from '../services/orderService.js'
import { fetchPlayers } from '../services/playerService.js'
import { getStrategy } from '../services/strategyService.js'
import { normalizePlayers } from '../utils/playerUtils.js'

export default function ComerciarPage() {
  const { userId } = useAuth()

  const [strategy, setStrategy] = useState(null)
  const [hasStrategy, setHasStrategy] = useState(false)
  const [players, setPlayers] = useState([])
  const [holdings, setHoldings] = useState([])
  const [pendingOrders, setPendingOrders] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [showBuyModal, setShowBuyModal] = useState(false)
  const [showSellModal, setShowSellModal] = useState(false)
  const [noStrategyAlert, setNoStrategyAlert] = useState('')
  const [reloadTrigger, setReloadTrigger] = useState(0)

  // Estado para cancelación de órdenes (T044)
  const [cancellingOrderId, setCancellingOrderId] = useState(null)
  const [cancelFeedback, setCancelFeedback] = useState({ error: '', success: '' })

  // Carga inicial y reactiva de datos (T041)
  useEffect(() => {
    if (!userId) {
      return
    }

    let isCancelled = false

    const loadData = async () => {
      try {
        const [strategyData, playersData, holdingsData, ordersData] =
          await Promise.all([
            getStrategy(userId).catch(() => null),
            fetchPlayers().catch(() => []),
            getHoldings(userId).catch(() => []),
            getPendingOrders(userId).catch(() => []),
          ])

        if (isCancelled) return

        const strategyExists = Boolean(
          strategyData &&
            (strategyData.estrategia !== null ||
              strategyData.tipo ||
              strategyData.weights),
        )

        setStrategy(strategyData)
        setHasStrategy(strategyExists)
        setPlayers(normalizePlayers(playersData))
        setHoldings(Array.isArray(holdingsData) ? holdingsData : [])
        setPendingOrders(Array.isArray(ordersData) ? ordersData : [])
      } catch (err) {
        if (isCancelled) return
        const msg =
          err.response?.data?.mensaje ||
          err.response?.data?.message ||
          'Error al cargar la información de comercio.'
        setError(msg)
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    loadData()

    return () => {
      isCancelled = true
    }
  }, [userId, reloadTrigger])

  // Dividir órdenes pendientes en grupos de COMPRA y VENTA (T042)
  const buyOrders = useMemo(() => {
    return pendingOrders.filter((o) => {
      const op = (o.tipoOperacion || o.operation || '').toUpperCase()
      return op === 'BUY'
    })
  }, [pendingOrders])

  const sellOrders = useMemo(() => {
    return pendingOrders.filter((o) => {
      const op = (o.tipoOperacion || o.operation || '').toUpperCase()
      return op === 'SELL'
    })
  }, [pendingOrders])

  const handleOpenBuy = () => {
    if (!hasStrategy) {
      setNoStrategyAlert(
        'Debe configurar una estrategia de valuación antes de poder emitir órdenes de compra.',
      )
      return
    }

    setNoStrategyAlert('')
    setShowBuyModal(true)
  }

  const handleOpenSell = () => {
    if (!hasStrategy) {
      setNoStrategyAlert(
        'Debe configurar una estrategia de valuación antes de poder emitir órdenes de venta.',
      )
      return
    }

    setNoStrategyAlert('')
    setShowSellModal(true)
  }

  const handleRetry = () => {
    setIsLoading(true)
    setError('')
    setNoStrategyAlert('')
    setReloadTrigger((count) => count + 1)
  }

  // Cancelar orden (T044)
  const handleCancelOrder = async (orderId) => {
    if (!userId || cancellingOrderId) return

    setCancellingOrderId(orderId)
    setCancelFeedback({ error: '', success: '' })

    try {
      await cancelOrder(orderId, userId)
      setCancelFeedback({ error: '', success: 'Orden cancelada exitosamente.' })
      setReloadTrigger((count) => count + 1)
      setTimeout(() => {
        setCancelFeedback((prev) => ({ ...prev, success: '' }))
      }, 3500)
    } catch (err) {
      const msg =
        err.response?.data?.mensaje ||
        err.response?.data?.message ||
        'No se pudo cancelar la orden. Por favor intente nuevamente.'
      setCancelFeedback({ error: msg, success: '' })
    } finally {
      setCancellingOrderId(null)
    }
  }

  return (
    <div className="mx-auto max-w-7xl space-y-6">
      {/* Encabezado y acciones de comercio */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between border-b border-slate-200 pb-5">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            Comerciar Tokens
          </h1>
          <p className="mt-1 text-sm text-slate-600">
            Emita órdenes de compra o venta calculadas según su estrategia de valuación.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={handleOpenBuy}
            className="inline-flex items-center gap-2 rounded-lg bg-emerald-600 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-emerald-700 focus:outline-none focus:ring-2 focus:ring-emerald-500"
          >
            <span className="text-base" aria-hidden="true">+</span>
            Comprar Tokens
          </button>
          <button
            type="button"
            onClick={handleOpenSell}
            className="inline-flex items-center gap-2 rounded-lg bg-rose-600 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-rose-700 focus:outline-none focus:ring-2 focus:ring-rose-500"
          >
            <span className="text-base" aria-hidden="true">-</span>
            Vender Tokens
          </button>
        </div>
      </div>

      {/* Alerta de bloqueo cuando no existe estrategia configurada */}
      {noStrategyAlert && (
        <div
          role="alert"
          className="flex flex-col gap-3 rounded-xl border border-amber-300 bg-amber-50 p-4 text-amber-900 sm:flex-row sm:items-center sm:justify-between shadow-sm"
        >
          <div className="flex items-center gap-3">
            <span className="text-xl" aria-hidden="true">⚠️</span>
            <p className="text-sm font-medium">{noStrategyAlert}</p>
          </div>
          <Link
            to="/app/estrategia"
            className="inline-flex shrink-0 items-center justify-center rounded-lg bg-amber-700 px-3.5 py-1.5 text-xs font-semibold text-white transition hover:bg-amber-800 focus:outline-none focus:ring-2 focus:ring-amber-500"
          >
            Configurar Estrategia →
          </Link>
        </div>
      )}

      {/* Feedback de cancelación de orden (T044) */}
      {cancelFeedback.success && (
        <div
          role="status"
          className="rounded-lg border border-emerald-200 bg-emerald-50 p-3.5 text-sm font-semibold text-emerald-800"
        >
          {cancelFeedback.success}
        </div>
      )}

      {cancelFeedback.error && (
        <div
          role="alert"
          className="rounded-lg border border-red-200 bg-red-50 p-3.5 text-sm font-semibold text-red-800"
        >
          {cancelFeedback.error}
        </div>
      )}

      {/* Estado de carga / error general (T043) */}
      <FeedbackState
        isLoading={isLoading}
        error={error}
        onRetry={handleRetry}
        loadingMessage="Cargando órdenes y datos del mercado..."
      >
        <div className="space-y-6">
          {/* Panel informativo de la estrategia */}
          <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm text-sm text-slate-700">
            <span className="font-semibold text-slate-900">Estado de negociación:</span>{' '}
            {hasStrategy ? (
              <span>
                Estrategia activa:{' '}
                <strong className="text-sky-700">
                  {strategy?.estrategia || strategy?.tipo || 'Configurada'}
                </strong>
                .
              </span>
            ) : (
              <span className="text-amber-700 font-medium">
                Sin estrategia configurada. Debe configurar una para emitir nuevas órdenes.
              </span>
            )}
          </div>

          {/* Listas separadas de órdenes activas (T042, T043) */}
          <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
            <OrderList
              type="BUY"
              orders={buyOrders}
              players={players}
              onCancel={handleCancelOrder}
              cancellingOrderId={cancellingOrderId}
            />

            <OrderList
              type="SELL"
              orders={sellOrders}
              players={players}
              onCancel={handleCancelOrder}
              cancellingOrderId={cancellingOrderId}
            />
          </div>
        </div>
      </FeedbackState>

      {/* Modal de compra */}
      <BuyModal
        isOpen={showBuyModal}
        players={players}
        userId={userId}
        onClose={() => setShowBuyModal(false)}
        onSuccess={() => {
          setReloadTrigger((count) => count + 1)
        }}
      />

      {/* Modal de venta */}
      <SellModal
        isOpen={showSellModal}
        players={players}
        holdings={holdings}
        pendingOrders={pendingOrders}
        userId={userId}
        onClose={() => setShowSellModal(false)}
        onSuccess={() => {
          setReloadTrigger((count) => count + 1)
        }}
      />
    </div>
  )
}
