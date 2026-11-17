export default function OrderList({
  orders = [],
  type = 'BUY',
  players = [],
  onCancel,
  cancellingOrderId = null,
}) {
  const isBuy = type === 'BUY'
  const title = isBuy ? 'Órdenes Activas de Compra' : 'Órdenes Activas de Venta'
  const emptyText = isBuy
    ? 'No tiene órdenes de compra activas en este momento.'
    : 'No tiene órdenes de venta activas en este momento.'

  const playerMap = new Map()
  players.forEach((p) => {
    playerMap.set(Number(p.id), p.name || p.nombre || `Jugador #${p.id}`)
  })

  return (
    <section
      aria-label={title}
      className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm"
    >
      <div className="flex items-center justify-between border-b border-slate-100 bg-slate-50 px-4 py-3">
        <div className="flex items-center gap-2">
          <span
            className={`inline-block h-2.5 w-2.5 rounded-full ${
              isBuy ? 'bg-emerald-500' : 'bg-rose-500'
            }`}
            aria-hidden="true"
          />
          <h2 className="text-sm font-bold uppercase tracking-wider text-slate-800">
            {title}
          </h2>
          <span className="rounded-full bg-slate-200 px-2 py-0.5 text-xs font-semibold text-slate-700">
            {orders.length}
          </span>
        </div>
      </div>

      <div className="overflow-x-auto">
        {orders.length === 0 ? (
          <div className="p-8 text-center text-sm text-slate-500">
            {emptyText}
          </div>
        ) : (
          <table className="min-w-full divide-y divide-slate-200 text-left text-sm text-slate-700">
            <thead className="bg-slate-50/75 text-xs font-semibold uppercase tracking-wider text-slate-600">
              <tr>
                <th scope="col" className="px-4 py-3">
                  Jugador
                </th>
                <th scope="col" className="px-4 py-3 text-right">
                  Cantidad
                </th>
                <th scope="col" className="px-4 py-3 text-right">
                  Precio Unitario
                </th>
                <th scope="col" className="px-4 py-3 text-right">
                  Tolerancia
                </th>
                <th scope="col" className="px-4 py-3 text-right">
                  Total Estimado
                </th>
                <th scope="col" className="px-4 py-3 text-center">
                  Acción
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200 bg-white">
              {orders.map((order) => {
                const playerId = order.jugadorId ?? order.playerId
                const playerName = playerMap.get(Number(playerId)) || `Jugador #${playerId}`
                const quantity = order.cantidad ?? order.quantity ?? 0
                const price = Number(order.precio ?? order.price ?? 0)
                const tolerance = Number(order.tolerancia ?? order.tolerance ?? 0)
                const baseTotal = quantity * price
                const totalWithTolerance = isBuy
                  ? baseTotal + tolerance
                  : Math.max(0, baseTotal - quantity * tolerance)

                const isCancelling = cancellingOrderId === order.id

                return (
                  <tr
                    key={order.id}
                    className="transition-colors hover:bg-slate-50"
                  >
                    <td className="whitespace-nowrap px-4 py-3 font-semibold text-slate-900">
                      {playerName}
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 text-right font-mono text-xs text-slate-700">
                      {quantity} tokens
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 text-right font-mono text-xs text-slate-700">
                      ${price.toFixed(2)}
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 text-right font-mono text-xs text-slate-500">
                      ±${tolerance.toFixed(2)}
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 text-right font-mono text-xs font-bold text-slate-900">
                      ${totalWithTolerance.toFixed(2)}
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 text-center">
                      <button
                        type="button"
                        onClick={() => onCancel && onCancel(order.id)}
                        disabled={isCancelling}
                        className="rounded-lg border border-red-200 bg-red-50 px-3 py-1 text-xs font-semibold text-red-700 transition hover:bg-red-100 hover:text-red-800 focus:outline-none focus:ring-2 focus:ring-red-400 disabled:opacity-50 disabled:cursor-not-allowed"
                        aria-label={`Cancelar orden de ${isBuy ? 'compra' : 'venta'} de ${playerName}`}
                      >
                        {isCancelling ? 'Cancelando...' : 'Cancelar'}
                      </button>
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
