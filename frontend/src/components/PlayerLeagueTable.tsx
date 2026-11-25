import type { components } from '../api/schema'

const formatCellValue = (value: string | number | null | undefined) => {
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  return value
}

type Player = components['schemas']['PlayerResponse']

const COLUMNS = [
  { key: 'name', label: 'Nombre completo' },
  { key: 'position', label: 'Posición' },
  { key: 'team', label: 'Equipo' },
  { key: 'appearances', label: 'Partidos jugados' },
  { key: 'goals', label: 'Goles' },
  { key: 'assists', label: 'Asistencias' },
  { key: 'yellowCards', label: 'Amarillas' },
  { key: 'redCards', label: 'Rojas' },
]

interface PlayerLeagueTableProps {
  leagueName: string
  players: Player[]
  isExpanded: boolean
  onToggle: () => void
  tableId: string
}

const PlayerLeagueTable = ({
  leagueName,
  players,
  isExpanded,
  onToggle,
  tableId,
}: PlayerLeagueTableProps) => {
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
                  const rowKey = player.externalId ?? `${leagueName}-${index}`
                  return (
                    <tr
                      key={rowKey}
                      className="transition-colors hover:bg-slate-50"
                    >
                      <td className="whitespace-nowrap px-4 py-3 font-medium text-slate-900">
                        {formatCellValue(player.name)}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                        {formatCellValue(player.position)}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                        {formatCellValue(player.team?.name)}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                        {formatCellValue(player.appearances)}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                        {formatCellValue(player.goals)}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                        {formatCellValue(player.assists)}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                        {formatCellValue(player.yellowCards)}
                      </td>
                      <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                        {formatCellValue(player.redCards)}
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
