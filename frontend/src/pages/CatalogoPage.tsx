import { useCallback, useEffect, useMemo, useState } from 'react'

import { getUserFacingError } from '../api/client'
import PlayerFilters from '../components/PlayerFilters'
import PlayerTable from '../components/PlayerTable'
import { getPlayers } from '../services/playerService'
import type { Player, PlayerFilters as Filters, PlayerSort } from '../types/market'

const initialFilters: Filters = { league: '', team: '', nationality: '' }
const initialSort: PlayerSort = { column: null, direction: 'asc' }

const CatalogoPage = () => {
  const [players, setPlayers] = useState<Player[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [filters, setFilters] = useState(initialFilters)
  const [sort, setSort] = useState(initialSort)

  const loadPlayers = useCallback(async () => {
    setIsLoading(true)
    setError('')
    try {
      setPlayers(await getPlayers())
    } catch (requestError) {
      setError(getUserFacingError(requestError, 'No se pudo cargar el catálogo. Intente nuevamente.'))
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    const controller = new AbortController()
    void getPlayers(controller.signal)
      .then((response) => {
        if (!controller.signal.aborted) {
          setPlayers(response)
        }
      })
      .catch((requestError: unknown) => {
        if (!controller.signal.aborted) {
          setError(getUserFacingError(requestError, 'No se pudo cargar el catálogo. Intente nuevamente.'))
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setIsLoading(false)
        }
      })
    return () => controller.abort()
  }, [])

  const options = useMemo(() => ({
    leagues: [...new Set(players.map((player) => player.league?.nombre).filter((value): value is string => Boolean(value)))].sort(),
    teams: [...new Set(players.map((player) => player.team?.name).filter((value): value is string => Boolean(value)))].sort(),
    nationalities: [...new Set(players.map((player) => player.league?.pais).filter((value): value is string => Boolean(value)))].sort(),
  }), [players])

  const visiblePlayers = useMemo(() => {
    const filtered = players.filter((player) =>
      (!filters.league || player.league?.nombre === filters.league)
      && (!filters.team || player.team?.name === filters.team)
      && (!filters.nationality || player.league?.pais === filters.nationality),
    )

    const sortColumn = sort.column
    if (!sortColumn) {
      return filtered
    }

    return [...filtered].sort((left, right) => {
      const leftValue = sortColumn === 'team'
        ? left.team?.name ?? ''
        : sortColumn === 'league'
          ? left.league?.nombre ?? ''
          : left[sortColumn] ?? ''
      const rightValue = sortColumn === 'team'
        ? right.team?.name ?? ''
        : sortColumn === 'league'
          ? right.league?.nombre ?? ''
          : right[sortColumn] ?? ''
      const comparison = typeof leftValue === 'number' && typeof rightValue === 'number'
        ? leftValue - rightValue
        : String(leftValue).localeCompare(String(rightValue), 'es', { sensitivity: 'base' })

      return sort.direction === 'asc' ? comparison : -comparison
    })
  }, [players, filters, sort])

  return (
    <section className="space-y-6">
      <header>
        <p className="text-sm font-semibold uppercase tracking-wide text-sky-700">Mercado</p>
        <h1 className="mt-1 text-2xl font-bold tracking-tight text-slate-900">Catálogo de jugadores</h1>
        <p className="mt-1 text-sm text-slate-600">
          Explore y ordene jugadores de todas las ligas disponibles.
        </p>
      </header>
      <PlayerFilters filters={filters} options={options} onChange={setFilters} />
      <PlayerTable
        players={visiblePlayers}
        sort={sort}
        onSortChange={setSort}
        isLoading={isLoading}
        error={error}
        onRetry={() => void loadPlayers()}
      />
    </section>
  )
}

export default CatalogoPage
