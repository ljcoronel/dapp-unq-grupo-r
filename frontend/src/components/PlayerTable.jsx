import { ErrorState, LoadingState } from './FeedbackState.jsx'

const COLUMNS = [
  { key: 'name', label: 'Nombre', isNumeric: false },
  { key: 'team', label: 'Equipo', isNumeric: false },
  { key: 'league', label: 'Liga', isNumeric: false },
  { key: 'appearances', label: 'Apariciones', isNumeric: true },
  { key: 'minutesPlayed', label: 'Minutos jugados', isNumeric: true },
  { key: 'goals', label: 'Goles', isNumeric: true },
  { key: 'assists', label: 'Asistencias', isNumeric: true },
  { key: 'yellowCards', label: 'Tarjetas amarillas', isNumeric: true },
  { key: 'passSuccessPercentage', label: '% Pases exitosos', isNumeric: true },
]

export default function PlayerTable({
  players = [],
  sort = { column: null, direction: 'asc' },
  onSortChange,
  isLoading = false,
  error = '',
  onRetry,
}) {
  const handleSort = (columnKey) => {
    if (!onSortChange) return

    if (sort.column === columnKey) {
      onSortChange({
        column: columnKey,
        direction: sort.direction === 'asc' ? 'desc' : 'asc',
      })
    } else {
      onSortChange({
        column: columnKey,
        direction: 'asc',
      })
    }
  }

  if (isLoading) {
    return <LoadingState message="Cargando catálogo de jugadores..." />
  }

  if (error) {
    return (
      <ErrorState
        message={error}
        onRetry={onRetry}
        retryLabel="Reintentar carga"
      />
    )
  }

  return (
    <section
      aria-label="Catálogo de jugadores"
      className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm"
    >
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-slate-200 text-left text-sm text-slate-700">
          <thead className="bg-slate-50 text-xs font-semibold uppercase tracking-wider text-slate-600">
            <tr>
              {COLUMNS.map((column) => {
                const isSorted = sort.column === column.key
                const sortDirection = isSorted ? sort.direction : null
                const ariaSort = isSorted
                  ? sortDirection === 'asc'
                    ? 'ascending'
                    : 'descending'
                  : 'none'

                return (
                  <th
                    key={column.key}
                    scope="col"
                    aria-sort={ariaSort}
                    className={`px-4 py-3 select-none ${
                      column.isNumeric ? 'text-right' : 'text-left'
                    }`}
                  >
                    <button
                      type="button"
                      onClick={() => handleSort(column.key)}
                      className={`inline-flex items-center gap-1 font-semibold transition-colors hover:text-slate-950 focus:outline-none focus:underline ${
                        isSorted ? 'text-sky-700' : 'text-slate-700'
                      }`}
                      aria-label={`Ordenar por ${column.label}`}
                    >
                      <span>{column.label}</span>
                      <span className="text-xs" aria-hidden="true">
                        {isSorted ? (
                          sortDirection === 'asc' ? (
                            '▲'
                          ) : (
                            '▼'
                          )
                        ) : (
                          <span className="text-slate-400 opacity-60">⇅</span>
                        )}
                      </span>
                    </button>
                  </th>
                )
              })}
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200 bg-white">
            {players.length === 0 ? (
              <tr>
                <td
                  colSpan={COLUMNS.length}
                  className="px-6 py-12 text-center text-sm text-slate-500"
                >
                  No se encontraron jugadores que coincidan con los filtros seleccionados.
                </td>
              </tr>
            ) : (
              players.map((player, index) => (
                <tr
                  key={player.id ?? `${player.name}-${index}`}
                  className="transition-colors hover:bg-slate-50"
                >
                  <td className="whitespace-nowrap px-4 py-3 font-medium text-slate-900">
                    {player.name || '-'}
                  </td>
                  <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                    {player.team || '-'}
                  </td>
                  <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                    {player.league || '-'}
                  </td>
                  <td className="whitespace-nowrap px-4 py-3 text-right text-slate-700 font-mono text-xs">
                    {player.appearances ?? 0}
                  </td>
                  <td className="whitespace-nowrap px-4 py-3 text-right text-slate-700 font-mono text-xs">
                    {player.minutesPlayed ?? 0}
                  </td>
                  <td className="whitespace-nowrap px-4 py-3 text-right text-slate-700 font-mono text-xs font-semibold">
                    {player.goals ?? 0}
                  </td>
                  <td className="whitespace-nowrap px-4 py-3 text-right text-slate-700 font-mono text-xs">
                    {player.assists ?? 0}
                  </td>
                  <td className="whitespace-nowrap px-4 py-3 text-right text-slate-700 font-mono text-xs">
                    {player.yellowCards ?? 0}
                  </td>
                  <td className="whitespace-nowrap px-4 py-3 text-right text-slate-700 font-mono text-xs">
                    {typeof player.passSuccessPercentage === 'number'
                      ? `${player.passSuccessPercentage.toFixed(1)}%`
                      : '-'}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </section>
  )
}
