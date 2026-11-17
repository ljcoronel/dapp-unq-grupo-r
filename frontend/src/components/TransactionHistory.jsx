export default function TransactionHistory({
  transactions = [],
  players = [],
}) {
  const playerMap = new Map()
  players.forEach((p) => {
    playerMap.set(Number(p.id), p.name || p.nombre || `Jugador #${p.id}`)
  })

  const formatDate = (dateValue) => {
    if (!dateValue) return '-'
    try {
      const date = new Date(dateValue)
      if (Number.isNaN(date.getTime())) return String(dateValue)
      return date.toLocaleString('es-AR', {
        dateStyle: 'short',
        timeStyle: 'short',
      })
    } catch {
      return String(dateValue)
    }
  }

  return (
    <section
      aria-label="Historial de transacciones"
      className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm"
    >
      <div className="flex items-center justify-between border-b border-slate-100 bg-slate-50 px-4 py-3">
        <div className="flex items-center gap-2">
          <span className="inline-block h-2.5 w-2.5 rounded-full bg-slate-600" aria-hidden="true" />
          <h2 className="text-sm font-bold uppercase tracking-wider text-slate-800">
            Historial de Transacciones Concretadas
          </h2>
          <span className="rounded-full bg-slate-200 px-2 py-0.5 text-xs font-semibold text-slate-700">
            {transactions.length}
          </span>
        </div>
      </div>

      <div className="overflow-x-auto">
        {transactions.length === 0 ? (
          <div className="p-8 text-center text-sm text-slate-500">
            No registra transacciones ejecutadas hasta el momento.
          </div>
        ) : (
          <table className="min-w-full divide-y divide-slate-200 text-left text-sm text-slate-700">
            <thead className="bg-slate-50/75 text-xs font-semibold uppercase tracking-wider text-slate-600">
              <tr>
                <th scope="col" className="px-4 py-3">
                  Fecha
                </th>
                <th scope="col" className="px-4 py-3">
                  Operación
                </th>
                <th scope="col" className="px-4 py-3">
                  Jugador
                </th>
                <th scope="col" className="px-4 py-3 text-right">
                  Cantidad
                </th>
                <th scope="col" className="px-4 py-3 text-right">
                  Precio Total Liquidado
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200 bg-white">
              {transactions.map((tx, index) => {
                const op = (tx.operation || tx.tipoOperacion || '').toUpperCase()
                const isBuy = op === 'BUY'
                const isSell = op === 'SELL'
                const playerId = tx.playerId ?? tx.jugadorId
                const playerName = playerMap.get(Number(playerId)) || `Jugador #${playerId}`
                const quantity = tx.quantity ?? tx.cantidad ?? 0
                const settledPrice = Number(
                  tx.settledPrice ?? tx.precioLiquidado ?? tx.precioTotal ?? 0,
                )
                const dateRaw = tx.executedAt || tx.issuedAt || tx.fechaCreacion || tx.fecha

                return (
                  <tr
                    key={tx.orderId ?? tx.id ?? index}
                    className="transition-colors hover:bg-slate-50"
                  >
                    <td className="whitespace-nowrap px-4 py-3 text-xs font-mono text-slate-600">
                      {formatDate(dateRaw)}
                    </td>
                    <td className="whitespace-nowrap px-4 py-3">
                      <span
                        className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-bold ${
                          isBuy
                            ? 'bg-emerald-100 text-emerald-800 border border-emerald-200'
                            : isSell
                              ? 'bg-rose-100 text-rose-800 border border-rose-200'
                              : 'bg-slate-100 text-slate-700 border border-slate-200'
                        }`}
                      >
                        {isBuy ? 'Compra' : isSell ? 'Venta' : op}
                      </span>
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 font-semibold text-slate-900">
                      {playerName}
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 text-right font-mono text-xs text-slate-700">
                      {quantity} tokens
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 text-right font-mono text-xs font-bold text-slate-900">
                      ${settledPrice.toFixed(2)}
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        )}
      </div>
    </section>
  )
}
