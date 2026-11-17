export default function PlayerFilters({
  filters = { liga: '', equipo: '', nacionalidad: '' },
  options = { ligas: [], equipos: [], nacionalidades: [] },
  onChange,
}) {
  const handleSelectChange = (key, value) => {
    if (onChange) {
      onChange({
        ...filters,
        [key]: value,
      })
    }
  }

  const handleClear = () => {
    if (onChange) {
      onChange({
        liga: '',
        equipo: '',
        nacionalidad: '',
      })
    }
  }

  const hasActiveFilters = Boolean(
    filters.liga || filters.equipo || filters.nacionalidad,
  )

  return (
    <section
      aria-label="Filtros del catálogo"
      className="mb-6 rounded-lg border border-slate-200 bg-white p-4 shadow-sm"
    >
      <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-100 pb-3">
        <h2 className="text-sm font-semibold uppercase tracking-wider text-slate-700">
          Filtros de Búsqueda
        </h2>
        {hasActiveFilters && (
          <button
            type="button"
            onClick={handleClear}
            className="text-xs font-medium text-sky-600 transition-colors hover:text-sky-800 focus:outline-none focus:underline"
          >
            Limpiar filtros
          </button>
        )}
      </div>

      <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-3">
        {/* Filtro Liga */}
        <div>
          <label
            htmlFor="filter-liga"
            className="block text-xs font-medium text-slate-700 mb-1"
          >
            Liga
          </label>
          <select
            id="filter-liga"
            value={filters.liga || ''}
            onChange={(e) => handleSelectChange('liga', e.target.value)}
            className="w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 shadow-sm transition focus:border-sky-500 focus:outline-none focus:ring-1 focus:ring-sky-500"
          >
            <option value="">Todas las ligas</option>
            {options.ligas.map((liga) => (
              <option key={liga} value={liga}>
                {liga}
              </option>
            ))}
          </select>
        </div>

        {/* Filtro Equipo */}
        <div>
          <label
            htmlFor="filter-equipo"
            className="block text-xs font-medium text-slate-700 mb-1"
          >
            Equipo
          </label>
          <select
            id="filter-equipo"
            value={filters.equipo || ''}
            onChange={(e) => handleSelectChange('equipo', e.target.value)}
            className="w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 shadow-sm transition focus:border-sky-500 focus:outline-none focus:ring-1 focus:ring-sky-500"
          >
            <option value="">Todos los equipos</option>
            {options.equipos.map((equipo) => (
              <option key={equipo} value={equipo}>
                {equipo}
              </option>
            ))}
          </select>
        </div>

        {/* Filtro Nacionalidad */}
        <div>
          <label
            htmlFor="filter-nacionalidad"
            className="block text-xs font-medium text-slate-700 mb-1"
          >
            Nacionalidad
          </label>
          <select
            id="filter-nacionalidad"
            value={filters.nacionalidad || ''}
            onChange={(e) => handleSelectChange('nacionalidad', e.target.value)}
            className="w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 shadow-sm transition focus:border-sky-500 focus:outline-none focus:ring-1 focus:ring-sky-500"
          >
            <option value="">Todas las nacionalidades</option>
            {options.nacionalidades.map((nacionalidad) => (
              <option key={nacionalidad} value={nacionalidad}>
                {nacionalidad}
              </option>
            ))}
          </select>
        </div>
      </div>
    </section>
  )
}
