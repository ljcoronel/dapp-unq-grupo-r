import type { Holding } from '../services/holdingService'
import type { Player } from '../types/market'

interface HoldingListProps {
  holdings: Holding[]
  players: Player[]
}

const formatCredits = (amount: number | undefined) =>
  amount === undefined ? '—' : `${amount.toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} créditos`

const HoldingList = ({ holdings, players }: HoldingListProps) => (
  <section className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
    <div className="border-b border-slate-200 px-4 py-4 sm:px-5">
      <h2 className="font-semibold text-slate-900">Tenencias</h2>
    </div>
    {holdings.length === 0 ? (
      <p className="p-5 text-sm text-slate-500">Todavía no tiene tokens en su portfolio.</p>
    ) : (
      <div className="overflow-x-auto">
        <table className="min-w-[600px] w-full text-left text-sm">
          <thead className="bg-slate-50 text-xs uppercase text-slate-500">
            <tr>
              <th className="px-4 py-3">Jugador</th>
              <th className="px-4 py-3">Cantidad</th>
              <th className="px-4 py-3">Valor unitario</th>
              <th className="px-4 py-3">Valor actual</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {holdings.map((holding, index) => (
              <tr key={holding.id ?? `${holding.jugadorId}-${index}`}>
                <td className="px-4 py-3 font-medium text-slate-900">
                  {players.find((player) => player.id === holding.jugadorId)?.name
                    ?? `Jugador ${holding.jugadorId ?? ''}`}
                </td>
                <td className="px-4 py-3">{holding.tokens?.toLocaleString('es-AR') ?? '—'}</td>
                <td className="px-4 py-3">{formatCredits(holding.valorUnitario)}</td>
                <td className="px-4 py-3 font-medium">{formatCredits(holding.valorTotal)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    )}
  </section>
)

export default HoldingList
