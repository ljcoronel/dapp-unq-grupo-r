import { useMemo } from 'react'
import {
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
} from 'recharts'

import type { Holding } from '../services/holdingService'
import type { Player } from '../types/market'
import FeedbackState from './FeedbackState'

interface PortfolioPieChartProps {
  holdings: Holding[]
  players: Player[]
}

const colors = ['#0369a1', '#0d9488', '#4f46e5', '#ca8a04', '#e11d48', '#7c3aed', '#059669', '#ea580c', '#2563eb', '#65a30d', '#64748b']

const PortfolioPieChart = ({ holdings, players }: PortfolioPieChartProps) => {
  const data = useMemo(() => {
    const valuedHoldings = holdings
      .map((holding) => ({
        name: players.find((player) => player.externalId === holding.jugadorId)?.name
          ?? `Jugador ${holding.jugadorId ?? ''}`,
        value: holding.valorTotal ?? 0,
      }))
      .filter((holding) => Number.isFinite(holding.value) && holding.value > 0)
      .sort((left, right) => right.value - left.value)

    if (valuedHoldings.length <= 10) {
      return valuedHoldings
    }

    const topHoldings = valuedHoldings.slice(0, 10)
    const otherValue = valuedHoldings.slice(10).reduce((total, holding) => total + holding.value, 0)
    return [...topHoldings, { name: 'Otros', value: otherValue }]
  }, [holdings, players])

  if (data.length === 0) {
    return <FeedbackState variant="empty" message="No hay valores disponibles para representar en el gráfico." />
  }

  return (
    <section className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm sm:p-5">
      <h2 className="font-semibold text-slate-900">Distribución del portfolio</h2>
      <div className="mt-4 h-72 w-full min-w-0">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={data}
              dataKey="value"
              nameKey="name"
              cx="50%"
              cy="48%"
              outerRadius="72%"
              label={({ name, percent }) =>
                `${name ?? ''} ${((percent ?? 0) * 100).toLocaleString('es-AR', { maximumFractionDigits: 1 })}%`
              }
            >
              {data.map((entry, index) => (
                <Cell key={entry.name} fill={colors[index % colors.length]} />
              ))}
            </Pie>
            <Tooltip formatter={(value) => `${Number(value).toLocaleString('es-AR', { maximumFractionDigits: 2 })} créditos`} />
            <Legend />
          </PieChart>
        </ResponsiveContainer>
      </div>
    </section>
  )
}

export default PortfolioPieChart
