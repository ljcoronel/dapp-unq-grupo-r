import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'

import { getUserFacingError } from '../api/client'
import type { CreateOrderRequest, PendingOrder } from '../services/orderService'
import type { Holding } from '../services/holdingService'
import { getCurrentValuation } from '../services/playerService'
import type { Player } from '../types/market'
import { getAvailableTokens, isValidIntegerQuantity, isValidTolerance } from '../utils/orderValidation'

interface SellModalProps {
  isOpen: boolean
  players: Player[]
  holdings: Holding[]
  pendingOrders: PendingOrder[]
  userId: number
  onConfirm: (order: CreateOrderRequest) => Promise<void>
  onClose: () => void
}

const formatCredits = (amount: number) =>
  `${amount.toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} créditos`

const SellModal = ({
  isOpen,
  players,
  holdings,
  pendingOrders,
  userId,
  onConfirm,
  onClose,
}: SellModalProps) => {
  const [playerId, setPlayerId] = useState('')
  const [quantity, setQuantity] = useState('1')
  const [tolerance, setTolerance] = useState('0')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState('')
  const [priceState, setPriceState] = useState<{
    playerId: string
    value: number | null
    error: string
  } | null>(null)
  const selectedPlayerId = Number(playerId)
  const availableTokens = playerId
    ? getAvailableTokens(selectedPlayerId, holdings, pendingOrders)
    : 0
  const quantityLimit = Math.min(availableTokens, 100)

  useEffect(() => {
    if (!isOpen || !playerId) {
      return
    }

    let active = true

    void getCurrentValuation(selectedPlayerId, userId)
      .then((valuation) => {
        if (active) {
          setPriceState({
            playerId,
            value: valuation.valorUnitario ?? null,
            error: '',
          })
        }
      })
      .catch((requestError: unknown) => {
        if (active) {
          setPriceState({
            playerId,
            value: null,
            error: getUserFacingError(requestError, 'No se pudo cargar la cotización.'),
          })
        }
      })

    return () => {
      active = false
    }
  }, [isOpen, playerId, selectedPlayerId, userId])

  const unitPrice = priceState?.playerId === playerId ? priceState.value : null
  const priceError = priceState?.playerId === playerId ? priceState.error : ''
  const isLoadingPrice = Boolean(playerId) && priceState?.playerId !== playerId
  const parsedQuantity = Number(quantity)
  const parsedTolerance = Number(tolerance)
  const totalWithoutTolerance = unitPrice === null ? null : parsedQuantity * unitPrice
  const totalWithTolerance = totalWithoutTolerance === null
    ? null
    : totalWithoutTolerance - parsedQuantity * parsedTolerance
  const canSubmit = useMemo(() =>
    isOpen
    && Boolean(playerId)
    && isValidIntegerQuantity(quantity, 1, quantityLimit)
    && isValidTolerance(tolerance)
    && unitPrice !== null
    && !isLoadingPrice
    && !isSubmitting,
  [isOpen, playerId, quantity, quantityLimit, tolerance, unitPrice, isLoadingPrice, isSubmitting])

  const closeModal = () => {
    setPlayerId('')
    setQuantity('1')
    setTolerance('0')
    onClose()
  }

  if (!isOpen) {
    return null
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!canSubmit || unitPrice === null) {
      return
    }

    setIsSubmitting(true)
    setSubmitError('')
    try {
      await onConfirm({
        usuarioId: userId,
        playerId: selectedPlayerId,
        quantity: parsedQuantity,
        price: unitPrice,
        tolerance: parsedTolerance,
      })
      closeModal()
    } catch (requestError) {
      setSubmitError(getUserFacingError(requestError, 'No se pudo enviar la orden de venta.'))
    } finally {
      setIsSubmitting(false)
    }
  }

  const availablePlayers = holdings
    .filter((holding) => (holding.jugadorId ?? 0) > 0)
    .map((holding) => players.find((player) => player.externalId === holding.jugadorId))
    .filter((player): player is Player => Boolean(player))
    .filter((player) => getAvailableTokens(player.externalId ?? 0, holdings, pendingOrders) > 0)

  return (
    <div className="fixed inset-0 z-30 flex items-end justify-center bg-slate-950/50 p-0 sm:items-center sm:p-4">
      <section role="dialog" aria-modal="true" aria-labelledby="sell-modal-title" className="max-h-[95vh] w-full overflow-y-auto rounded-t-2xl bg-white p-5 shadow-xl sm:max-w-lg sm:rounded-2xl sm:p-6">
        <div className="mb-5 flex items-start justify-between gap-4">
          <div>
            <h2 id="sell-modal-title" className="text-xl font-bold text-slate-900">Vender tokens</h2>
            <p className="mt-1 text-sm text-slate-600">Las órdenes de venta activas bloquean tokens hasta su resolución.</p>
          </div>
          <button type="button" onClick={closeModal} aria-label="Cerrar" className="rounded-lg px-2 py-1 text-xl text-slate-500 hover:bg-slate-100">×</button>
        </div>

        <form className="space-y-4" onSubmit={(event) => void handleSubmit(event)}>
          <label className="block text-sm font-medium text-slate-700">
            Jugador
            <select
              required
              value={playerId}
              onChange={(event) => {
                setPriceState(null)
                setPlayerId(event.target.value)
              }}
              className="mt-1.5 w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
            >
              <option value="">Seleccione un jugador</option>
              {availablePlayers.map((player, index) => (
                <option key={player.externalId ?? index} value={player.externalId ?? ''}>
                  {player.name ?? 'Jugador sin nombre'}
                </option>
              ))}
            </select>
          </label>

          {playerId && <p className="text-sm text-slate-600">Tokens disponibles: <strong>{availableTokens}</strong></p>}

          <label className="block text-sm font-medium text-slate-700">
            Cantidad (máximo {quantityLimit})
            <input
              type="number"
              min="1"
              max={quantityLimit}
              step="1"
              value={quantity}
              onChange={(event) => setQuantity(event.target.value)}
              className="mt-1.5 w-full rounded-lg border border-slate-300 px-3 py-2"
            />
          </label>

          <label className="block text-sm font-medium text-slate-700">
            Tolerancia por token (máximo 2 decimales)
            <input
              type="number"
              min="0"
              step="0.01"
              value={tolerance}
              onChange={(event) => setTolerance(event.target.value)}
              className="mt-1.5 w-full rounded-lg border border-slate-300 px-3 py-2"
            />
          </label>

          {playerId && (
            <div className="space-y-1 rounded-lg bg-slate-50 p-4 text-sm">
              {isLoadingPrice && <p className="text-slate-600">Cargando cotización...</p>}
              {priceError && <p className="text-red-700">{priceError}</p>}
              {unitPrice !== null && (
                <>
                  <p>Precio unitario: <strong>{formatCredits(unitPrice)}</strong></p>
                  <p>Total sin tolerancia: <strong>{formatCredits(totalWithoutTolerance ?? 0)}</strong></p>
                  <p>Total con tolerancia: <strong>{formatCredits(totalWithTolerance ?? 0)}</strong></p>
                </>
              )}
            </div>
          )}
          {quantity && !isValidIntegerQuantity(quantity, 1, quantityLimit) && (
            <p className="text-sm text-red-700">La cantidad debe estar entre 1 y los tokens disponibles (máximo 100).</p>
          )}
          {tolerance && !isValidTolerance(tolerance) && (
            <p className="text-sm text-red-700">La tolerancia debe ser positiva y tener hasta 2 decimales.</p>
          )}
          {submitError && <p role="alert" className="text-sm text-red-700">{submitError}</p>}

          <div className="flex flex-col-reverse gap-2 pt-2 sm:flex-row sm:justify-end">
            <button type="button" onClick={closeModal} className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50">Cancelar</button>
            <button type="submit" disabled={!canSubmit} className="rounded-lg bg-sky-700 px-4 py-2 text-sm font-semibold text-white hover:bg-sky-800 disabled:cursor-not-allowed disabled:bg-slate-300">
              {isSubmitting ? 'Enviando...' : 'Confirmar venta'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}

export default SellModal
