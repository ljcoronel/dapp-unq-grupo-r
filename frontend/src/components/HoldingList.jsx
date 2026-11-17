export default function HoldingList({ holdings = [], players = [] }) {
  const playerMap = new Map()
  players.forEach((p) => {
    playerMap.set(Number(p.id), p.name || p.nombre || `Jugador #${p.id}`)
  })

  const totalTokens = holdings.reduce((sum, h) => sum + (h.tokens || 0), 0)
  const totalValuation = holdings.reduce((sum, h) => {
    const valTotal =
      h.valorTotal !== null && h.valorTotal !== undefined
        ? Number(h.valorTotal)
        : Number(h.tokens || 0) * Number(h.valorUnitario || 0)
    return sum + (Number.isFinite(valTotal) ? valTotal : 0)
  }, 0)

  return (
    <section
      aria-label="Tenencias de tokens"
      className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm"
    >
      <div className="flex flex-wrap items-center justify-between border-b border-slate-100 bg-slate-50 px-4 py-3 gap-2">
        <div className="flex items-center gap-2">
          <span className="inline-block h-2.5 w-2.5 rounded-full bg-sky-500" aria-hidden="true" />
          <h2 className="text-sm font-bold uppercase tracking-wider text-slate-800">
            Tenencias Actuales
          </h2>
          <span className="rounded-full bg-slate-200 px-2 py-0.5 text-xs font-semibold text-slate-700">
            {holdings.length}
          </span>
        </div>

        {holdings.length > 0 && (
          <div className="text-xs text-slate-600 font-medium">
            Valor total:{' '}
            <strong className="text-sky-700 font-mono text-sm font-bold">
              ${totalValuation.toFixed(2)}
            </strong>
          </div>
        )}
      </div>

      <div className="overflow-x-auto">
        {holdings.length === 0 ? (
          <div className="p-8 text-center text-sm text-slate-500">
            No posee tenencias en su portfolio actualmente.
          </div>
        ) : (
          <table className="min-w-full divide-y divide-slate-200 text-left text-sm text-slate-700">
            <thead className="bg-slate-50/75 text-xs font-semibold uppercase tracking-wider text-slate-600">
              <tr>
                <th scope="col" className="px-4 py-3">
                  Jugador
                </th>
                <th scope="col" className="px-4 py-3 text-right">
                  Cantidad de Tokens
                </th>
                <th scope="col" className="px-4 py-3 text-right">
                  Valor Unitario Actual
                </th>
                <th scope="col" className="px-4 py-3 text-right">
                  Valor Total
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200 bg-white">
              {holdings.map((holding) => {
                const playerId = holding.jugadorId ?? holding.playerId
                const playerName = playerMap.get(Number(playerId)) || `Jugador #${playerId}`
                const tokens = holding.tokens || 0

                let unitPrice = null
                if (holding.valorUnitario !== null && holding.valorUnitario !== undefined) {
                  unitPrice = Number(holding.valorUnitario)
                } else if (
                  holding.valorTotal !== null &&
                  holding.valorTotal !== undefined &&
                  tokens > 0
                ) {
                  unitPrice = Number(holding.valorTotal) / tokens
                }

                let totalValue = 0
                if (holding.valorTotal !== null && holding.valorTotal !== undefined) {
                  totalValue = Number(holding.valorTotal)
                } else if (unitPrice !== null) {
                  totalValue = tokens * unitPrice
                }

                return (
                  <tr
                    key={holding.id ?? `${playerId}-${tokens}`}
                    className="transition-colors hover:bg-slate-50"
                  >
                    <td className="whitespace-nowrap px-4 py-3 font-semibold text-slate-900">
                      {playerName}
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 text-right font-mono text-xs text-slate-700">
                      {tokens} tokens
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 text-right font-mono text-xs text-slate-700">
                      {unitPrice !== null ? `$${unitPrice.toFixed(2)}` : '-'}
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 text-right font-mono text-xs font-bold text-slate-900">
                      ${totalValue.toFixed(2)}
                    </td>
                  </tr>
                )
              })}
            </tbody>
            <tfoot className="bg-slate-50 border-t border-slate-200 text-xs font-bold text-slate-800">
              <tr>
                <td className="px-4 py-3 uppercase">Total Portfolio</td>
                <td className="px-4 py-3 text-right font-mono">
                  {totalTokens} tokens
                </td>
                <td className="px-4 py-3 text-right">-</td>
                <td className="px-4 py-3 text-right font-mono text-sm text-sky-700">
                  ${totalValuation.toFixed(2)}
                </td>
              </tr>
            </tfoot>
          </table>
        )}
      </div>
    </section>
  )
}
