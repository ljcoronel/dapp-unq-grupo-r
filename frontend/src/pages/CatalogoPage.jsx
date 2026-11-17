import { useEffect, useMemo, useState } from 'react'

import PlayerFilters from '../components/PlayerFilters.jsx'
import PlayerTable from '../components/PlayerTable.jsx'
import { fetchPlayers } from '../services/playerService.js'
import { normalizePlayers } from '../utils/playerUtils.js'

export default function CatalogoPage() {
  const [rawPlayers, setRawPlayers] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [filters, setFilters] = useState({
    liga: '',
    equipo: '',
    nacionalidad: '',
  })
  const [sort, setSort] = useState({
    column: 'name',
    direction: 'asc',
  })

  const loadPlayers = async () => {
    setIsLoading(true)
    setError('')

    try {
      const data = await fetchPlayers()
      const normalized = normalizePlayers(data)
      setRawPlayers(normalized)
    } catch (requestError) {
      const message =
        requestError.response?.data?.mensaje ||
        requestError.response?.data?.message ||
        'No se pudo cargar el catálogo de jugadores. Por favor, intente nuevamente.'
      setError(message)
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    let isCancelled = false

    const initialFetch = async () => {
      try {
        const data = await fetchPlayers()
        if (isCancelled) return
        const normalized = normalizePlayers(data)
        setRawPlayers(normalized)
      } catch (requestError) {
        if (isCancelled) return
        const message =
          requestError.response?.data?.mensaje ||
          requestError.response?.data?.message ||
          'No se pudo cargar el catálogo de jugadores. Por favor, intente nuevamente.'
        setError(message)
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    initialFetch()

    return () => {
      isCancelled = true
    }
  }, [])

  // Extraer opciones únicas para los filtros (T022)
  const filterOptions = useMemo(() => {
    const ligasSet = new Set()
    const equiposSet = new Set()
    const nacionalidadesSet = new Set()

    rawPlayers.forEach((player) => {
      if (player.league) ligasSet.add(player.league)
      if (player.team) equiposSet.add(player.team)
      if (player.nationality) nacionalidadesSet.add(player.nationality)
    })

    return {
      ligas: Array.from(ligasSet).sort((a, b) =>
        a.localeCompare(b, 'es', { sensitivity: 'base' }),
      ),
      equipos: Array.from(equiposSet).sort((a, b) =>
        a.localeCompare(b, 'es', { sensitivity: 'base' }),
      ),
      nacionalidades: Array.from(nacionalidadesSet).sort((a, b) =>
        a.localeCompare(b, 'es', { sensitivity: 'base' }),
      ),
    }
  }, [rawPlayers])

  // Filtrado y ordenamiento combinados client-side (T023)
  const processedPlayers = useMemo(() => {
    // 1. Filtrar
    const filtered = rawPlayers.filter((player) => {
      if (filters.liga && player.league !== filters.liga) {
        return false
      }
      if (filters.equipo && player.team !== filters.equipo) {
        return false
      }
      if (filters.nacionalidad && player.nationality !== filters.nacionalidad) {
        return false
      }
      return true
    })

    // 2. Ordenar
    if (!sort.column) {
      return filtered
    }

    const { column, direction } = sort
    const factor = direction === 'asc' ? 1 : -1

    return [...filtered].sort((a, b) => {
      const valA = a[column]
      const valB = b[column]

      if (typeof valA === 'number' && typeof valB === 'number') {
        return (valA - valB) * factor
      }

      const strA = (valA ?? '').toString()
      const strB = (valB ?? '').toString()
      return strA.localeCompare(strB, 'es', { sensitivity: 'base' }) * factor
    })
  }, [rawPlayers, filters, sort])

  return (
    <div className="mx-auto max-w-7xl">
      <header className="mb-6">
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">
          Catálogo Unificado de Jugadores
        </h1>
        <p className="mt-1 text-sm text-slate-600">
          Explore las estadísticas completas de los jugadores disponibles, aplique
          filtros combinados y ordene por cualquiera de las métricas.
        </p>
      </header>

      {/* Controles de filtro */}
      <PlayerFilters
        filters={filters}
        options={filterOptions}
        onChange={setFilters}
      />

      {/* Tabla unificada de 9 columnas */}
      <PlayerTable
        players={processedPlayers}
        sort={sort}
        onSortChange={setSort}
        isLoading={isLoading}
        error={error}
        onRetry={loadPlayers}
      />
    </div>
  )
}
