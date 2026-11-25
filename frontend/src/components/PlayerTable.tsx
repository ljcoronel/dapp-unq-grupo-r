import FeedbackState from './FeedbackState'
import type { Player, PlayerSort, PlayerSortColumn } from '../types/market'

interface PlayerTableProps {
  players: Player[]
  sort: PlayerSort
  onSortChange: (sort: PlayerSort) => void
  isLoading: boolean
  error: string
  onRetry: () => void
}

const columns: { key: PlayerSortColumn; label: string }[] = [
  { key: 'name', label: 'Nombre' },
  { key: 'team', label: 'Equipo' },
  { key: 'league', label: 'Liga' },
  { key: 'appearances', label: 'Apariciones' },
  { key: 'minutesPlayed', label: 'Minutos jugados' },
  { key: 'goals', label: 'Goles' },
  { key: 'assists', label: 'Asistencias' },
  { key: 'yellowCards', label: 'Tarjetas amarillas' },
  { key: 'passSuccessPercentage', label: '% pases exitosos' },
]

const formatNumber = (value: number | undefined, suffix = '') =>
  value === undefined ? '—' : `${value.toLocaleString('es-AR')}${suffix}`

const PlayerTable = ({
  players,
  sort,
  onSortChange,
  isLoading,
  error,
  onRetry,
}: PlayerTableProps) => {
  if (isLoading) {
    return <FeedbackState variant="loading" message="Cargando el catálogo de jugadores..." />
  }

  if (error) {
    return (
      <FeedbackState
        variant="error"
        message={error}
        action={{ label: 'Reintentar', onClick: onRetry }}
      />
    )
  }

  if (players.length === 0) {
    return <FeedbackState variant="empty" message="No hay jugadores que coincidan con los filtros." />
  }

  const handleSort = (column: PlayerSortColumn) => {
    onSortChange({
      column,
      direction: sort.column === column && sort.direction === 'asc' ? 'desc' : 'asc',
    })
  }

  return (
    <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
      <div className="overflow-x-auto">
        <table className="min-w-[900px] divide-y divide-slate-200 text-left text-sm">
          <thead className="bg-slate-50 text-xs font-semibold uppercase tracking-wide text-slate-600">
            <tr>
              {columns.map(({ key, label }) => (
                <th
                  key={key}
                  scope="col"
                  aria-sort={sort.column === key ? (sort.direction === 'asc' ? 'ascending' : 'descending') : 'none'}
                  className="whitespace-nowrap px-4 py-3"
                >
                  <button
                    type="button"
                    onClick={() => handleSort(key)}
                    className="inline-flex items-center gap-1 text-left hover:text-sky-800"
                  >
                    {label}
                    <span aria-hidden="true">
                      {sort.column === key ? (sort.direction === 'asc' ? '↑' : '↓') : '↕'}
                    </span>
                  </button>
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {players.map((player, index) => (
              <tr key={player.externalId ?? `${player.name ?? 'player'}-${index}`} className="hover:bg-slate-50">
                <td className="whitespace-nowrap px-4 py-3 font-medium text-slate-900">{player.name ?? '—'}</td>
                <td className="whitespace-nowrap px-4 py-3">{player.team?.name ?? '—'}</td>
                <td className="whitespace-nowrap px-4 py-3">{player.league?.nombre ?? '—'}</td>
                <td className="whitespace-nowrap px-4 py-3">{formatNumber(player.appearances)}</td>
                <td className="whitespace-nowrap px-4 py-3">{formatNumber(player.minutesPlayed)}</td>
                <td className="whitespace-nowrap px-4 py-3">{formatNumber(player.goals)}</td>
                <td className="whitespace-nowrap px-4 py-3">{formatNumber(player.assists)}</td>
                <td className="whitespace-nowrap px-4 py-3">{formatNumber(player.yellowCards)}</td>
                <td className="whitespace-nowrap px-4 py-3">
                  {formatNumber(player.passSuccessPercentage, '%')}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <p className="border-t border-slate-200 px-4 py-3 text-xs text-slate-500">
        {players.length} {players.length === 1 ? 'jugador' : 'jugadores'}
      </p>
    </div>
  )
}

export default PlayerTable
