import type { StrategyState } from '../services/strategyService'

interface StrategyDisplayProps {
  strategy: StrategyState
}

const metricLabels: Record<string, string> = {
  goals: 'Goles',
  assists: 'Asistencias',
  passSuccessPercentage: 'Porcentaje de pases exitosos',
  aerialsWon: 'Duelos aéreos ganados',
  timesManOfTheMatch: 'Jugador del partido',
  minutesPlayed: 'Minutos jugados',
  appearances: 'Apariciones',
}

const StrategyDisplay = ({ strategy }: StrategyDisplayProps) => {
  const weights = strategy.actual?.pesos ?? strategy.pesos ?? {}
  const strategyName = strategy.estrategia === 'ESTRATEGIA_A' ? 'Estrategia A' : 'Estrategia B'

  return (
    <section className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
      <div className="border-b border-slate-200 px-5 py-4">
        <p className="text-sm text-slate-500">Estrategia actual</p>
        <h2 className="mt-1 text-lg font-semibold text-slate-900">{strategyName}</h2>
        {strategy.vigenteDesde && (
          <p className="mt-1 text-xs text-slate-500">
            Vigente desde {new Date(strategy.vigenteDesde).toLocaleString('es-AR')}
          </p>
        )}
      </div>
      <dl className="divide-y divide-slate-100">
        {Object.entries(weights).map(([metric, weight]) => (
          <div key={metric} className="flex items-center justify-between gap-4 px-5 py-3 text-sm">
            <dt className="text-slate-600">{metricLabels[metric] ?? metric}</dt>
            <dd className="font-semibold tabular-nums text-slate-900">
              {(weight).toLocaleString('es-AR')}%
            </dd>
          </div>
        ))}
        {Object.keys(weights).length === 0 && (
          <p className="px-5 py-4 text-sm text-slate-500">La estrategia no informa ponderaciones.</p>
        )}
      </dl>
    </section>
  )
}

export default StrategyDisplay
