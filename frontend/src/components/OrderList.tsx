import type { PendingOrder } from '../services/orderService'
import type { Player } from '../types/market'

interface OrderListProps {
  orders: PendingOrder[]
  type: 'BUY' | 'SELL'
  players: Player[]
  onCancel: (orderId: number) => void
}

const formatNumber = (value: number | undefined) =>
  value === undefined ? '—' : value.toLocaleString('es-AR', { maximumFractionDigits: 2 })

const OrderList = ({ orders, type, players, onCancel }: OrderListProps) => (
  <section className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
    <div className="border-b border-slate-200 px-4 py-4 sm:px-5">
      <h2 className="font-semibold text-slate-900">Órdenes de {type === 'BUY' ? 'compra' : 'venta'}</h2>
    </div>
    {orders.length === 0 ? (
      <p className="p-5 text-sm text-slate-500">
        No hay órdenes de {type === 'BUY' ? 'compra' : 'venta'} pendientes.
      </p>
    ) : (
      <div className="overflow-x-auto">
        <table className="min-w-[700px] w-full text-left text-sm">
          <thead className="bg-slate-50 text-xs uppercase text-slate-500">
            <tr>
              <th className="px-4 py-3">Jugador</th>
              <th className="px-4 py-3">Cantidad</th>
              <th className="px-4 py-3">Precio unitario</th>
              <th className="px-4 py-3">Tolerancia</th>
              <th className="px-4 py-3">Emitida</th>
              <th className="px-4 py-3"><span className="sr-only">Acciones</span></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {orders.map((order, index) => {
              const orderId = order.orderId
              return (
                <tr key={orderId ?? `${type}-${index}`}>
                  <td className="px-4 py-3 font-medium text-slate-900">
                    {players.find((player) => player.externalId === order.playerId)?.name ?? `Jugador ${order.playerId ?? ''}`}
                  </td>
                  <td className="px-4 py-3">{formatNumber(order.quantity)}</td>
                  <td className="px-4 py-3">{formatNumber(order.price)}</td>
                  <td className="px-4 py-3">{formatNumber(order.tolerance)}</td>
                  <td className="px-4 py-3">
                    {order.issuedAt ? new Date(order.issuedAt).toLocaleString('es-AR') : '—'}
                  </td>
                  <td className="px-4 py-3 text-right">
                    {orderId !== undefined && (
                      <button
                        type="button"
                        onClick={() => onCancel(orderId)}
                        className="rounded-md border border-red-200 px-3 py-1.5 text-xs font-semibold text-red-700 hover:bg-red-50"
                      >
                        Cancelar
                      </button>
                    )}
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
    )}
  </section>
)

export default OrderList
