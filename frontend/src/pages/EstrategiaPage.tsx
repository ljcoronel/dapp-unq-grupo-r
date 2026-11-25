import { useCallback, useEffect, useState } from 'react'

import { getUserFacingError } from '../api/client'
import FeedbackState from '../components/FeedbackState'
import StrategyDisplay from '../components/StrategyDisplay'
import StrategyForm from '../components/StrategyForm'
import { useAuth } from '../context/AuthContext'
import { configureStrategy, getStrategy, type StrategyRequest, type StrategyState } from '../services/strategyService'

const EstrategiaPage = () => {
  const { userId } = useAuth()
  const [strategy, setStrategy] = useState<StrategyState | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isSaving, setIsSaving] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [isFormOpen, setIsFormOpen] = useState(false)

  const loadStrategy = useCallback(async () => {
    if (userId === null) {
      return
    }
    try {
      setStrategy(await getStrategy(userId))
    } catch (requestError) {
      setError(getUserFacingError(requestError, 'No se pudo cargar la estrategia.'))
    } finally {
      setIsLoading(false)
    }
  }, [userId])

  useEffect(() => {
    if (userId === null) {
      return
    }
    let active = true
    void getStrategy(userId)
      .then((loadedStrategy) => {
        if (active) {
          setStrategy(loadedStrategy)
        }
      })
      .catch((requestError: unknown) => {
        if (active) {
          setError(getUserFacingError(requestError, 'No se pudo cargar la estrategia.'))
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

  const handleConfirm = async (request: StrategyRequest) => {
    if (userId === null) {
      throw new Error('No se pudo identificar al usuario en la sesión.')
    }
    setIsSaving(true)
    setNotice('')
    try {
      setStrategy(await configureStrategy(userId, request))
      setNotice('La estrategia se guardó correctamente.')
    } finally {
      setIsSaving(false)
    }
  }

  if (userId === null) {
    return <FeedbackState variant="error" message="No se pudo identificar al usuario en la sesión. Inicie sesión nuevamente." />
  }
  if (isLoading) {
    return <FeedbackState variant="loading" message="Cargando la estrategia..." />
  }
  if (error) {
    return <FeedbackState variant="error" message={error} action={{ label: 'Reintentar', onClick: () => void loadStrategy() }} />
  }

  return (
    <section className="space-y-6">
      <header className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-sm font-semibold uppercase tracking-wide text-sky-700">Preferencias</p>
          <h1 className="mt-1 text-2xl font-bold tracking-tight text-slate-900">Estrategia de valuación</h1>
          <p className="mt-1 text-sm text-slate-600">Configure cómo se calcula el valor de los tokens.</p>
        </div>
        <button type="button" disabled={isSaving} onClick={() => setIsFormOpen(true)} className="rounded-lg bg-sky-700 px-4 py-2.5 text-sm font-semibold text-white hover:bg-sky-800 disabled:opacity-60">
          {strategy?.estrategia ? 'Cambiar estrategia' : 'Crear estrategia'}
        </button>
      </header>
      {notice && <p role="status" className="rounded-lg border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-800">{notice}</p>}
      {strategy?.estrategia ? (
        <StrategyDisplay strategy={strategy} />
      ) : (
        <FeedbackState
          variant="info"
          title="Todavía no tiene una estrategia"
          message="Cree una estrategia de valuación antes de emitir órdenes de compra o venta."
        />
      )}
      {userId !== null && isFormOpen && (
        <StrategyForm
          initialStrategy={strategy}
          onConfirm={handleConfirm}
          onClose={() => setIsFormOpen(false)}
        />
      )}
    </section>
  )
}

export default EstrategiaPage
