import type { Transaction } from '../services/orderService'
import type { Player } from '../types/market'
import { isBuyOperation } from '../utils/orderValidation'

interface TransactionHistoryProps {
  transactions: Transaction[]
  players: Player[]
}

const TransactionHistory = ({ transactions, players }: TransactionHistoryProps) => (
  <section className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
    <div className="border-b border-slate-200 px-4 py-4 sm:px-5">
      <h2 className="font-semibold text-slate-900">Historial de transacciones</h2>
    </div>
    {transactions.length === 0 ? (
      <p className="p-5 text-sm text-slate-500">Todavía no hay transacciones concretadas.</p>
    ) : (
      <div className="overflow-x-auto">
        <table className="min-w-[700px] w-full text-left text-sm">
          <thead className="bg-slate-50 text-xs uppercase text-slate-500">
            <tr>
              <th className="px-4 py-3">Operación</th>
              <th className="px-4 py-3">Jugador</th>
              <th className="px-4 py-3">Cantidad</th>
              <th className="px-4 py-3">Total concretado</th>
              <th className="px-4 py-3">Fecha</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {transactions.map((transaction, index) => {
              const isBuy = isBuyOperation(transaction.operation)
              const total = transaction.settledPrice !== null && transaction.settledPrice !== undefined
                ? (transaction.quantity ?? 0) * transaction.settledPrice
                : null
              return (
                <tr key={transaction.orderId ?? index}>
                  <td className={`px-4 py-3 font-semibold ${isBuy ? 'text-emerald-700' : 'text-rose-700'}`}>
                    {isBuy ? 'Compra' : 'Venta'}
                  </td>
                  <td className="px-4 py-3">
                    {players.find((player) => player.externalId === transaction.playerId)?.name
                      ?? `Jugador ${transaction.playerId ?? ''}`}
                  </td>
                  <td className="px-4 py-3">{transaction.quantity?.toLocaleString('es-AR') ?? '—'}</td>
                  <td className="px-4 py-3">
                    {total === null
                      ? '—'
                      : `${total.toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} créditos`}
                  </td>
                  <td className="px-4 py-3">
                    {transaction.executedAt
                      ? new Date(transaction.executedAt).toLocaleString('es-AR')
                      : transaction.issuedAt
                        ? new Date(transaction.issuedAt).toLocaleString('es-AR')
                        : '—'}
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

export default TransactionHistory
