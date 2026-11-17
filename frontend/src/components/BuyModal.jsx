import { useEffect, useId, useState } from 'react'

import { createBuyOrder } from '../services/orderService.js'
import { getCurrentValuation } from '../services/playerService.js'
import { calculateBuyPrices, validateBuyOrder } from '../utils/orderValidation.js'

function BuyModalDialog({
  players = [],
  userId,
  userCredits = null,
  onConfirm,
  onClose,
  onSuccess,
}) {
  const initialPlayerId = players.length > 0 ? String(players[0].id) : ''
  const [selectedPlayerId, setSelectedPlayerId] = useState(initialPlayerId)
  const [quantity, setQuantity] = useState(1)
  const [tolerance, setTolerance] = useState(0)
  const [unitPrice, setUnitPrice] = useState(null)
  const [isLoadingPrice, setIsLoadingPrice] = useState(false)
  const [priceError, setPriceError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState('')
  const [submitSuccess, setSubmitSuccess] = useState('')

  const titleId = useId()

  // Cargar valuación cuando cambia el jugador seleccionado (T028)
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

  // Cálculos de precio (T030)
  const { totalSinTolerancia, totalConTolerancia } = calculateBuyPrices(
    quantity,
    unitPrice || 0,
    tolerance,
  )

  // Validación en tiempo real (T029)
  const validation = validateBuyOrder({
    playerId: selectedPlayerId ? Number(selectedPlayerId) : null,
    quantity,
    tolerance,
    unitPrice,
    userCredits,
  })

  const isFormValid =
    validation.isValid &&
    !isLoadingPrice &&
    unitPrice !== null &&
    !isSubmitting

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
        result = await createBuyOrder(orderData)
      }

      setSubmitSuccess('¡Orden de compra emitida exitosamente!')
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
        'Error al emitir la orden de compra. Por favor, revise los datos.'
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
            Emitir Orden de Compra
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
              htmlFor="buy-player-select"
              className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-1"
            >
              Jugador
            </label>
            <select
              id="buy-player-select"
              value={selectedPlayerId}
              onChange={(e) => {
                setSelectedPlayerId(e.target.value)
                setUnitPrice(null)
              }}
              disabled={isSubmitting}
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 shadow-sm focus:border-sky-500 focus:outline-none focus:ring-1 focus:ring-sky-500"
            >
              <option value="">Seleccione un jugador...</option>
              {players.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name} {p.team ? `(${p.team})` : ''}
                </option>
              ))}
            </select>
            {validation.errors.playerId && (
              <p className="mt-1 text-xs text-red-600">
                {validation.errors.playerId}
              </p>
            )}
          </div>

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

          {/* Cantidad de Tokens (1 - 100) */}
          <div>
            <label
              htmlFor="buy-quantity-input"
              className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-1"
            >
              Cantidad de Tokens (1 - 100)
            </label>
            <input
              id="buy-quantity-input"
              type="number"
              min="1"
              max="100"
              step="1"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              disabled={isSubmitting}
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 shadow-sm focus:border-sky-500 focus:outline-none focus:ring-1 focus:ring-sky-500"
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
              htmlFor="buy-tolerance-input"
              className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-1"
            >
              Tolerancia de Variación de Precio ($)
            </label>
            <input
              id="buy-tolerance-input"
              type="number"
              min="0"
              step="0.01"
              value={tolerance}
              onChange={(e) => setTolerance(e.target.value)}
              disabled={isSubmitting}
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 shadow-sm focus:border-sky-500 focus:outline-none focus:ring-1 focus:ring-sky-500"
            />
            {validation.errors.tolerance && (
              <p className="mt-1 text-xs text-red-600">
                {validation.errors.tolerance}
              </p>
            )}
          </div>

          {/* Resumen de Cálculos de Precios (T030) */}
          <div className="space-y-1.5 rounded-lg border border-slate-200 bg-slate-50 p-3 text-xs">
            <div className="flex justify-between text-slate-600">
              <span>Precio total sin tolerancia:</span>
              <span className="font-mono font-semibold text-slate-800">
                ${totalSinTolerancia.toFixed(2)}
              </span>
            </div>
            <div className="flex justify-between text-slate-600 border-t border-slate-200 pt-1.5 font-bold">
              <span className="text-slate-800">Precio total con tolerancia:</span>
              <span className="font-mono text-sm text-sky-700">
                ${totalConTolerancia.toFixed(2)}
              </span>
            </div>
            {userCredits !== null && userCredits !== undefined && (
              <div className="flex justify-between text-slate-500 pt-1">
                <span>Sus créditos disponibles:</span>
                <span className="font-mono">${Number(userCredits).toFixed(2)}</span>
              </div>
            )}
          </div>

          {/* Error de créditos si supera disponibles (T029) */}
          {validation.errors.credits && (
            <div
              role="alert"
              className="rounded-lg bg-amber-50 p-2.5 text-xs font-medium text-amber-800 border border-amber-200"
            >
              {validation.errors.credits}
            </div>
          )}

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
                  ? 'bg-sky-600 hover:bg-sky-700'
                  : 'bg-slate-300 cursor-not-allowed text-slate-500'
              }`}
            >
              {isSubmitting ? 'Emitiendo orden...' : 'Confirmar Compra'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default function BuyModal(props) {
  if (!props.isOpen) return null
  return <BuyModalDialog {...props} />
}
