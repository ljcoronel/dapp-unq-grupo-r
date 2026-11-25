import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'

import { getUserFacingError } from '../api/client'
import type { CreateOrderRequest } from '../services/orderService'
import { getCurrentValuation } from '../services/playerService'
import type { Player } from '../types/market'
import { isValidIntegerQuantity, isValidTolerance } from '../utils/orderValidation'

interface BuyModalProps {
  isOpen: boolean
  players: Player[]
  userId: number
  onConfirm: (order: CreateOrderRequest) => Promise<void>
  onClose: () => void
}

const formatCredits = (amount: number) =>
  `${amount.toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} créditos`

const BuyModal = ({ isOpen, players, userId, onConfirm, onClose }: BuyModalProps) => {
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

  useEffect(() => {
    if (!isOpen || !playerId) {
      return
    }

    const selectedPlayerId = Number(playerId)
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
  }, [isOpen, playerId, userId])

  const unitPrice = priceState?.playerId === playerId ? priceState.value : null
  const priceError = priceState?.playerId === playerId ? priceState.error : ''
  const isLoadingPrice = Boolean(playerId) && priceState?.playerId !== playerId
  const parsedQuantity = Number(quantity)
  const parsedTolerance = Number(tolerance)
  const totalWithoutTolerance = unitPrice === null ? null : parsedQuantity * unitPrice
  const totalWithTolerance = totalWithoutTolerance === null
    ? null
    : totalWithoutTolerance + parsedTolerance
  const canSubmit = useMemo(() =>
    isOpen
    && Boolean(playerId)
    && isValidIntegerQuantity(quantity)
    && isValidTolerance(tolerance)
    && unitPrice !== null
    && !isLoadingPrice
    && !isSubmitting,
  [isOpen, playerId, quantity, tolerance, unitPrice, isLoadingPrice, isSubmitting])

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
        playerId: Number(playerId),
        quantity: parsedQuantity,
        price: unitPrice,
        tolerance: parsedTolerance,
      })
      closeModal()
    } catch (requestError) {
      setSubmitError(getUserFacingError(requestError, 'No se pudo enviar la orden de compra.'))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="fixed inset-0 z-30 flex items-end justify-center bg-slate-950/50 p-0 sm:items-center sm:p-4">
      <section
        role="dialog"
        aria-modal="true"
        aria-labelledby="buy-modal-title"
        className="max-h-[95vh] w-full overflow-y-auto rounded-t-2xl bg-white p-5 shadow-xl sm:max-w-lg sm:rounded-2xl sm:p-6"
      >
        <div className="mb-5 flex items-start justify-between gap-4">
          <div>
            <h2 id="buy-modal-title" className="text-xl font-bold text-slate-900">Comprar tokens</h2>
            <p className="mt-1 text-sm text-slate-600">La orden quedará pendiente hasta ser ejecutada.</p>
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
              {players.map((player, index) => (
                <option key={player.externalId ?? index} value={player.externalId ?? ''}>
                  {player.name ?? 'Jugador sin nombre'}
                </option>
              ))}
            </select>
          </label>

          <label className="block text-sm font-medium text-slate-700">
            Cantidad (1 a 100)
            <input
              type="number"
              min="1"
              max="100"
              step="1"
              value={quantity}
              onChange={(event) => setQuantity(event.target.value)}
              className="mt-1.5 w-full rounded-lg border border-slate-300 px-3 py-2"
            />
          </label>

          <label className="block text-sm font-medium text-slate-700">
            Tolerancia total (máximo 2 decimales)
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
              <p className="pt-2 text-xs text-slate-500">
                La disponibilidad de créditos se valida al confirmar la orden.
              </p>
            </div>
          )}
          {quantity && !isValidIntegerQuantity(quantity) && (
            <p className="text-sm text-red-700">La cantidad debe ser un entero entre 1 y 100.</p>
          )}
          {tolerance && !isValidTolerance(tolerance) && (
            <p className="text-sm text-red-700">La tolerancia debe ser positiva y tener hasta 2 decimales.</p>
          )}
          {submitError && <p role="alert" className="text-sm text-red-700">{submitError}</p>}

          <div className="flex flex-col-reverse gap-2 pt-2 sm:flex-row sm:justify-end">
            <button type="button" onClick={closeModal} className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50">
              Cancelar
            </button>
            <button type="submit" disabled={!canSubmit} className="rounded-lg bg-sky-700 px-4 py-2 text-sm font-semibold text-white hover:bg-sky-800 disabled:cursor-not-allowed disabled:bg-slate-300">
              {isSubmitting ? 'Enviando...' : 'Confirmar compra'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}

export default BuyModal
