import { useMemo, useState } from 'react'
import type { FormEvent } from 'react'

import { getUserFacingError } from '../api/client'
import type { StrategyRequest, StrategyState } from '../services/strategyService'

interface StrategyFormProps {
  initialStrategy: StrategyState | null
  onConfirm: (strategy: StrategyRequest) => Promise<void>
  onClose: () => void
}

const fields = {
  ESTRATEGIA_A: [
    { key: 'goals', label: 'Goles' },
    { key: 'assists', label: 'Asistencias' },
    { key: 'passSuccessPercentage', label: 'Porcentaje de pases exitosos' },
    { key: 'aerialsWon', label: 'Duelos aéreos ganados' },
    { key: 'timesManOfTheMatch', label: 'Jugador del partido' },
  ],
  ESTRATEGIA_B: [
    { key: 'passSuccessPercentage', label: 'Porcentaje de pases exitosos' },
    { key: 'aerialsWon', label: 'Duelos aéreos ganados' },
    { key: 'minutesPlayed', label: 'Minutos jugados' },
    { key: 'appearances', label: 'Apariciones' },
  ],
} as const

const StrategyForm = ({ initialStrategy, onConfirm, onClose }: StrategyFormProps) => {
  const [type, setType] = useState<'ESTRATEGIA_A' | 'ESTRATEGIA_B'>(
    initialStrategy?.estrategia ?? 'ESTRATEGIA_A',
  )
  const initialWeights = initialStrategy?.actual?.pesos ?? initialStrategy?.pesos ?? {}
  const [weights, setWeights] = useState<Record<string, string>>(() =>
    Object.fromEntries(
      fields[type].map(({ key }) => [key, String(initialWeights[key] ?? 0)]),
    ),
  )
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState('')
  const weightSum = fields[type].reduce((sum, { key }) => {
    const value = weights[key]
    return sum + (value?.trim() ? Number(value) : 0)
  }, 0)
  const expectedSum = type === 'ESTRATEGIA_A' ? 1 : 0.95
  const isValid = fields[type].every(({ key }) => {
    const value = weights[key]
    return Boolean(value?.trim()) && Number.isFinite(Number(value)) && Number(value) >= 0
  }) && Math.abs(weightSum - expectedSum) < 0.001

  const displayedSum = useMemo(
    () => weightSum.toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 3 }),
    [weightSum],
  )

  const handleTypeChange = (nextType: 'ESTRATEGIA_A' | 'ESTRATEGIA_B') => {
    setType(nextType)
    setWeights((current) => Object.fromEntries(
      fields[nextType].map(({ key }) => [key, current[key] ?? '0']),
    ))
    setError('')
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!isValid || isSubmitting) {
      return
    }

    setIsSubmitting(true)
    setError('')
    try {
      await onConfirm({
        tipo: type,
        pesos: Object.fromEntries(fields[type].map(({ key }) => [key, Number(weights[key])])),
      })
      onClose()
    } catch (requestError) {
      setError(getUserFacingError(requestError, 'No se pudo guardar la estrategia.'))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="fixed inset-0 z-30 flex items-end justify-center bg-slate-950/50 p-0 sm:items-center sm:p-4">
      <section role="dialog" aria-modal="true" aria-labelledby="strategy-form-title" className="max-h-[95vh] w-full overflow-y-auto rounded-t-2xl bg-white p-5 shadow-xl sm:max-w-xl sm:rounded-2xl sm:p-6">
        <div className="mb-5 flex items-start justify-between gap-4">
          <div>
            <h2 id="strategy-form-title" className="text-xl font-bold text-slate-900">Configurar estrategia</h2>
            <p className="mt-1 text-sm text-slate-600">Defina el peso de cada métrica de valuación.</p>
          </div>
          <button type="button" onClick={onClose} aria-label="Cerrar" className="rounded-lg px-2 py-1 text-xl text-slate-500 hover:bg-slate-100">×</button>
        </div>

        <form className="space-y-4" onSubmit={(event) => void handleSubmit(event)}>
          <label className="block text-sm font-medium text-slate-700">
            Tipo de estrategia
            <select
              value={type}
              onChange={(event) =>
                handleTypeChange(event.target.value === 'ESTRATEGIA_B' ? 'ESTRATEGIA_B' : 'ESTRATEGIA_A')
              }
              className="mt-1.5 w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
            >
              <option value="ESTRATEGIA_A">Estrategia A</option>
              <option value="ESTRATEGIA_B">Estrategia B</option>
            </select>
          </label>

          {fields[type].map(({ key, label }) => (
            <label key={key} className="block text-sm font-medium text-slate-700">
              {label}
              <input
                type="number"
                min="0"
                max="1"
                step="0.01"
                value={weights[key] ?? ''}
                onChange={(event) => setWeights((current) => ({ ...current, [key]: event.target.value }))}
                className="mt-1.5 w-full rounded-lg border border-slate-300 px-3 py-2"
              />
            </label>
          ))}

          <p className={`text-sm ${isValid ? 'text-emerald-700' : 'text-amber-800'}`}>
            Suma actual: {displayedSum}. La suma requerida es {expectedSum}.
          </p>
          {error && <p role="alert" className="text-sm text-red-700">{error}</p>}

          <div className="flex flex-col-reverse gap-2 pt-2 sm:flex-row sm:justify-end">
            <button type="button" onClick={onClose} className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50">Cancelar</button>
            <button type="submit" disabled={!isValid || isSubmitting} className="rounded-lg bg-sky-700 px-4 py-2 text-sm font-semibold text-white hover:bg-sky-800 disabled:cursor-not-allowed disabled:bg-slate-300">
              {isSubmitting ? 'Guardando...' : 'Guardar estrategia'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}

export default StrategyForm
