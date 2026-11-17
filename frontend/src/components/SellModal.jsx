import { useEffect, useId, useMemo, useState } from 'react'

import { createSellOrder } from '../services/orderService.js'
import { getCurrentValuation } from '../services/playerService.js'
import { calculateSellPrices, validateSellOrder } from '../utils/orderValidation.js'

function SellModalDialog({
  players = [],
  holdings = [],
  pendingOrders = [],
  userId,
  onConfirm,
  onClose,
  onSuccess,
}) {
  // Preseleccionar el primer jugador del que tenga tenencias si es posible
  const playersWithTokens = useMemo(() => {
    const holdingsMap = new Map()
    holdings.forEach((h) => {
      const pid = h.jugadorId ?? h.playerId
      if (pid) {
        holdingsMap.set(Number(pid), (holdingsMap.get(Number(pid)) || 0) + (h.tokens || 0))
      }
    })

    const blockedMap = new Map()
    pendingOrders.forEach((o) => {
      const op = o.tipoOperacion || o.operation
      const pid = o.jugadorId ?? o.playerId
      if (op === 'SELL' && pid) {
        const qty = o.cantidad ?? o.quantity ?? 0
        blockedMap.set(Number(pid), (blockedMap.get(Number(pid)) || 0) + qty)
      }
    })

    return players.map((p) => {
      const totalTokens = holdingsMap.get(Number(p.id)) || 0
      const blockedTokens = blockedMap.get(Number(p.id)) || 0
      const availableTokens = Math.max(0, totalTokens - blockedTokens)
      return {
        ...p,
        totalTokens,
        blockedTokens,
        availableTokens,
      }
    })
  }, [players, holdings, pendingOrders])

  const initialPlayer =
    playersWithTokens.find((p) => p.availableTokens > 0) ||
    playersWithTokens[0] ||
    null

  const [selectedPlayerId, setSelectedPlayerId] = useState(
    initialPlayer ? String(initialPlayer.id) : '',
  )
  const [quantity, setQuantity] = useState(1)
  const [tolerance, setTolerance] = useState(0)
  const [unitPrice, setUnitPrice] = useState(null)
  const [isLoadingPrice, setIsLoadingPrice] = useState(false)
  const [priceError, setPriceError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState('')
  const [submitSuccess, setSubmitSuccess] = useState('')

  const titleId = useId()

  const selectedPlayerData = useMemo(() => {
    return playersWithTokens.find((p) => String(p.id) === String(selectedPlayerId)) || null
  }, [playersWithTokens, selectedPlayerId])

  const availableTokens = selectedPlayerData?.availableTokens ?? 0

  // Cargar valuación cuando cambia el jugador seleccionado (T035)
  useEffect(() => {
    if (!selectedPlayerId || !userId) {
      return
    }

    let isCancelled = false

    const fetchPrice = async () => {
      setIsLoadingPrice(true)
      setPriceError('')

      try {
        const valuation = await getCurrentValuation(Number(selectedPlayerId), userId)
        if (isCancelled) return

        if (valuation && typeof valuation.valorUnitario === 'number') {
          setUnitPrice(valuation.valorUnitario)
        } else {
          setUnitPrice(null)
          setPriceError('No se recibió un valor unitario válido del servidor.')
        }
      } catch (err) {
        if (isCancelled) return
        setUnitPrice(null)
        const msg =
          err.response?.data?.mensaje ||
          err.response?.data?.message ||
          'No se pudo obtener la valuación del jugador según su estrategia.'
        setPriceError(msg)
      } finally {
        if (!isCancelled) {
          setIsLoadingPrice(false)
        }
      }
    }

    fetchPrice()

    return () => {
      isCancelled = true
    }
  }, [selectedPlayerId, userId])

  // Cálculos de precio de venta (T037)
  const { totalSinTolerancia, totalConTolerancia } = calculateSellPrices(
    quantity,
    unitPrice || 0,
    tolerance,
  )

  // Validación en tiempo real (T036)
  const validation = validateSellOrder({
    playerId: selectedPlayerId ? Number(selectedPlayerId) : null,
    quantity,
    tolerance,
    unitPrice,
    availableTokens,
  })

  const isFormValid =
    validation.isValid &&
    !isLoadingPrice &&
    unitPrice !== null &&
    !isSubmitting &&
    availableTokens > 0

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!isFormValid || !userId) return

    setIsSubmitting(true)
    setSubmitError('')
    setSubmitSuccess('')

    const orderData = {
      usuarioId: Number(userId),
      playerId: Number(selectedPlayerId),
      quantity: Number(quantity),
      price: Number(unitPrice),
      tolerance: Number(tolerance),
    }

    try {
      let result
      if (onConfirm) {
        result = await onConfirm(orderData)
      } else {
        result = await createSellOrder(orderData)
      }

      setSubmitSuccess('¡Orden de venta emitida exitosamente!')
      if (onSuccess) {
        onSuccess(result)
      }

      setTimeout(() => {
        onClose()
      }, 1200)
    } catch (err) {
      const msg =
        err.response?.data?.mensaje ||
        err.response?.data?.message ||
        'Error al emitir la orden de venta. Por favor, revise los datos.'
      setSubmitError(msg)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby={titleId}
      className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto bg-slate-900/60 p-4 backdrop-blur-sm"
    >
      <div className="relative w-full max-w-lg rounded-xl border border-slate-200 bg-white p-6 shadow-2xl transition-all">
        {/* Cabecera del modal */}
        <div className="flex items-center justify-between border-b border-slate-100 pb-3">
          <h2 id={titleId} className="text-xl font-bold text-slate-900">
            Emitir Orden de Venta
          </h2>
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-700 focus:outline-none focus:ring-2 focus:ring-sky-500"
            aria-label="Cerrar modal"
          >
            ✕
          </button>
        </div>

        {/* Mensajes de feedback */}
        {submitSuccess && (
          <div
            role="status"
            className="mt-4 rounded-lg bg-emerald-50 p-3 text-sm font-semibold text-emerald-800 border border-emerald-200"
          >
            {submitSuccess}
          </div>
        )}

        {submitError && (
          <div
            role="alert"
            className="mt-4 rounded-lg bg-red-50 p-3 text-sm font-semibold text-red-800 border border-red-200"
          >
            {submitError}
          </div>
        )}

        <form onSubmit={handleSubmit} className="mt-4 space-y-4">
          {/* Selector de Jugador */}
          <div>
            <label
              htmlFor="sell-player-select"
              className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-1"
            >
              Jugador
            </label>
            <select
              id="sell-player-select"
              value={selectedPlayerId}
              onChange={(e) => {
                setSelectedPlayerId(e.target.value)
                setUnitPrice(null)
              }}
              disabled={isSubmitting}
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 shadow-sm focus:border-sky-500 focus:outline-none focus:ring-1 focus:ring-sky-500"
            >
              <option value="">Seleccione un jugador...</option>
              {playersWithTokens.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name} — {p.availableTokens} disponibles ({p.totalTokens} en portfolio)
                </option>
              ))}
            </select>
            {validation.errors.playerId && (
              <p className="mt-1 text-xs text-red-600">
                {validation.errors.playerId}
              </p>
            )}
          </div>

          {/* Tokens Disponibles y Bloqueados (T034) */}
          <div className="rounded-lg border border-slate-200 bg-slate-50 p-3">
            <div className="flex items-center justify-between text-xs text-slate-600">
              <span>Tokens en Portfolio:</span>
              <span className="font-mono font-semibold text-slate-800">
                {selectedPlayerData?.totalTokens ?? 0}
              </span>
            </div>
            <div className="flex items-center justify-between text-xs text-slate-600 pt-1">
              <span>Bloqueados en órdenes activas:</span>
              <span className="font-mono text-amber-700 font-semibold">
                {selectedPlayerData?.blockedTokens ?? 0}
              </span>
            </div>
            <div className="flex items-center justify-between border-t border-slate-200 pt-1.5 text-xs font-bold text-slate-800">
              <span>Tokens disponibles para venta:</span>
              <span className="font-mono text-sm text-emerald-700">
                {availableTokens}
              </span>
            </div>
          </div>

          {validation.errors.available && (
            <div
              role="alert"
              className="rounded-lg bg-amber-50 p-2.5 text-xs font-medium text-amber-800 border border-amber-200"
            >
              {validation.errors.available}
            </div>
          )}

          {/* Precio Unitario */}
          <div className="rounded-lg border border-slate-200 bg-slate-50 p-3">
            <div className="flex items-center justify-between text-xs font-medium text-slate-600">
              <span>Precio Unitario (según Estrategia):</span>
              {isLoadingPrice ? (
                <span className="text-sky-600 font-semibold animate-pulse">
                  Consultando valuación...
                </span>
              ) : unitPrice !== null ? (
                <span className="font-mono text-sm font-bold text-slate-900">
                  ${unitPrice.toFixed(2)}
                </span>
              ) : (
                <span className="text-slate-400">Sin datos</span>
              )}
            </div>
            {priceError && (
              <p className="mt-1 text-xs font-medium text-red-600">
                {priceError}
              </p>
            )}
          </div>

          {/* Cantidad de Tokens a Vender */}
          <div>
            <label
              htmlFor="sell-quantity-input"
              className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-1"
            >
              Cantidad de Tokens a Vender (Máx: {availableTokens})
            </label>
            <input
              id="sell-quantity-input"
              type="number"
              min="1"
              max={availableTokens > 0 ? availableTokens : 1}
              step="1"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              disabled={isSubmitting || availableTokens <= 0}
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 shadow-sm focus:border-sky-500 focus:outline-none focus:ring-1 focus:ring-sky-500 disabled:bg-slate-100 disabled:cursor-not-allowed"
            />
            {validation.errors.quantity && (
              <p className="mt-1 text-xs text-red-600">
                {validation.errors.quantity}
              </p>
            )}
          </div>

          {/* Tolerancia de Variación de Precio */}
          <div>
            <label
              htmlFor="sell-tolerance-input"
              className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-1"
            >
              Tolerancia de Variación de Precio por Token ($)
            </label>
            <input
              id="sell-tolerance-input"
              type="number"
              min="0"
              step="0.01"
              value={tolerance}
              onChange={(e) => setTolerance(e.target.value)}
              disabled={isSubmitting || availableTokens <= 0}
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 shadow-sm focus:border-sky-500 focus:outline-none focus:ring-1 focus:ring-sky-500 disabled:bg-slate-100 disabled:cursor-not-allowed"
            />
            {validation.errors.tolerance && (
              <p className="mt-1 text-xs text-red-600">
                {validation.errors.tolerance}
              </p>
            )}
          </div>

          {/* Resumen de Cálculos de Venta (T037) */}
          <div className="space-y-1.5 rounded-lg border border-slate-200 bg-slate-50 p-3 text-xs">
            <div className="flex justify-between text-slate-600">
              <span>Precio total sin tolerancia:</span>
              <span className="font-mono font-semibold text-slate-800">
                ${totalSinTolerancia.toFixed(2)}
              </span>
            </div>
            <div className="flex justify-between text-slate-600 border-t border-slate-200 pt-1.5 font-bold">
              <span className="text-slate-800">Precio total con tolerancia (mínimo a recibir):</span>
              <span className="font-mono text-sm text-emerald-700">
                ${totalConTolerancia.toFixed(2)}
              </span>
            </div>
          </div>

          {/* Botones de acción */}
          <div className="flex items-center justify-end gap-3 pt-2 border-t border-slate-100">
            <button
              type="button"
              onClick={onClose}
              disabled={isSubmitting}
              className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-slate-400"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={!isFormValid}
              className={`rounded-lg px-4 py-2 text-sm font-semibold text-white shadow-sm transition focus:outline-none focus:ring-2 focus:ring-sky-500 ${
                isFormValid
                  ? 'bg-rose-600 hover:bg-rose-700'
                  : 'bg-slate-300 cursor-not-allowed text-slate-500'
              }`}
            >
              {isSubmitting ? 'Emitiendo orden...' : 'Confirmar Venta'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default function SellModal(props) {
  if (!props.isOpen) return null
  return <SellModalDialog {...props} />
}
