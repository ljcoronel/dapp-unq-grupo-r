import type { PlayerFilters as Filters } from '../types/market'

interface PlayerFiltersProps {
  filters: Filters
  options: {
    leagues: string[]
    teams: string[]
    nationalities: string[]
  }
  onChange: (filters: Filters) => void
}

const PlayerFilters = ({ filters, options, onChange }: PlayerFiltersProps) => {
  const controls = [
    { key: 'league' as const, label: 'Liga', values: options.leagues },
    { key: 'team' as const, label: 'Equipo', values: options.teams },
    { key: 'nationality' as const, label: 'País', values: options.nationalities },
  ]

  return (
    <div className="grid gap-4 rounded-xl border border-slate-200 bg-white p-4 shadow-sm sm:grid-cols-2 lg:grid-cols-4">
      {controls.map(({ key, label, values }) => (
        <label key={key} className="block text-sm font-medium text-slate-700">
          {label}
          <select
            value={filters[key]}
            onChange={(event) => onChange({ ...filters, [key]: event.target.value })}
            className="mt-1.5 w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm"
          >
            <option value="">Todas</option>
            {values.map((value) => (
              <option key={value} value={value}>{value}</option>
            ))}
          </select>
        </label>
      ))}
      <div className="flex items-end">
        <button
          type="button"
          onClick={() => onChange({ league: '', team: '', nationality: '' })}
          className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
        >
          Limpiar filtros
        </button>
      </div>
    </div>
  )
}

export default PlayerFilters
