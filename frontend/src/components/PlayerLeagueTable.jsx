const formatCellValue = (value) => {
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  return value
}

const COLUMNS = [
  { key: 'nombre', label: 'Nombre completo' },
  { key: 'seccion', label: 'Posición' },
  { key: 'equipo', label: 'Equipo' },
  { key: 'partidosJugados', label: 'Partidos jugados' },
  { key: 'goles', label: 'Goles' },
  { key: 'asistencias', label: 'Asistencias' },
  { key: 'penaltis', label: 'Penales' },
]

export const PlayerLeagueTable = ({
  leagueName,
  players = [],
  isExpanded = true,
  onToggle,
  tableId,
}) => {
  return (
    <section className="mb-6 overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
      <button
        type="button"
        onClick={onToggle}
        aria-expanded={isExpanded}
        aria-controls={tableId}
        className="flex w-full items-center justify-between bg-slate-50 px-5 py-4 text-left transition-colors duration-150 hover:bg-slate-100 focus:outline-none focus:ring-2 focus:ring-slate-400 focus:ring-offset-1"
      >
        <div className="flex items-center gap-2">
          <span className="text-lg font-semibold text-slate-900">{leagueName}</span>
          <span className="text-xs text-slate-500">
            ({players.length} {players.length === 1 ? 'jugador' : 'jugadores'})
          </span>
        </div>
        <div className="flex items-center text-sm font-medium text-slate-600">
          <span className="mr-2 text-xs text-slate-500">
            {isExpanded ? 'Ocultar' : 'Mostrar'}
          </span>
          <svg
            className={`h-5 w-5 transform text-slate-500 transition-transform duration-200 ${
              isExpanded ? 'rotate-180' : ''
            }`}
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            aria-hidden="true"
          >
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7" />
          </svg>
        </div>
      </button>

      {isExpanded && (
        <div id={tableId} className="overflow-x-auto border-t border-slate-200">
          {players.length === 0 ? (
            <div className="p-6 text-center text-sm text-slate-500">
              No hay jugadores registrados en esta liga.
            </div>
          ) : (
            <table className="min-w-full divide-y divide-slate-200 text-left text-sm text-slate-700">
              <thead className="bg-slate-50 text-xs font-semibold uppercase tracking-wider text-slate-600">
                <tr>
                  {COLUMNS.map((column) => (
                    <th key={column.key} scope="col" className="px-4 py-3">
                      {column.label}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200 bg-white">
                {players.map((player, index) => {
                  const rowKey = player?.id ?? `${leagueName}-${index}`
                  return (
                    <tr
                      key={rowKey}
                      className="transition-colors hover:bg-slate-50"
                    >
                      <td className="whitespace-nowrap px-4 py-3 font-medium text-slate-900">
                        {formatCellValue(player?.nombre)}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                        {formatCellValue(player?.seccion)}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                        {formatCellValue(player?.equipo)}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                        {formatCellValue(player?.partidosJugados)}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                        {formatCellValue(player?.goles)}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                        {formatCellValue(player?.asistencias)}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                        {formatCellValue(player?.penaltis)}
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          )}
        </div>
      )}
    </section>
  )
}

export default PlayerLeagueTable
