import { useEffect, useState } from 'react'
import ProfileMenu from '../components/ProfileMenu'
import PlayerLeagueTable from '../components/PlayerLeagueTable'
import { getUserFacingError } from '../api/client'
import { getPlayers, type Player } from '../api/players'

interface LeagueGroup {
  id: string
  name: string
  players: Player[]
}

const HomePage = () => {
  const [players, setPlayers] = useState<Player[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [expandedMap, setExpandedMap] = useState<Record<string, boolean>>({})

  useEffect(() => {
    const controller = new AbortController()

    void getPlayers(controller.signal)
      .then((loadedPlayers) => {
        if (!controller.signal.aborted) {
          setPlayers(loadedPlayers)
        }
      })
      .catch((requestError: unknown) => {
        if (!controller.signal.aborted) {
          setError(
            getUserFacingError(
              requestError,
              'No se pudo cargar el catálogo de jugadores. Por favor, intente nuevamente.',
            ),
          )
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setIsLoading(false)
        }
      })

    return () => controller.abort()
  }, [])

  const leagueGroups = players.reduce<LeagueGroup[]>((groups, player) => {
    const leagueName = player.league?.nombre || 'Liga sin nombre'
    const leagueId = player.league?.codigo || leagueName
    const existingGroup = groups.find((group) => group.id === leagueId)

    if (existingGroup) {
      existingGroup.players.push(player)
    } else {
      groups.push({ id: leagueId, name: leagueName, players: [player] })
    }

    return groups
  }, [])

  const handleToggleLeague = (leagueId: string) => {
    setExpandedMap((previousState) => ({
      ...previousState,
      [leagueId]: !previousState[leagueId],
    }))
  }

  const retryLoad = () => {
    setIsLoading(true)
    setError('')

    void getPlayers()
      .then(setPlayers)
      .catch((requestError: unknown) => {
        setError(
          getUserFacingError(
            requestError,
            'No se pudo cargar el catálogo de jugadores. Por favor, intente nuevamente.',
          ),
        )
      })
      .finally(() => setIsLoading(false))
  }

  return (
    <div className="min-h-screen bg-slate-100">
      <nav className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-4">
          <div className="text-lg font-semibold text-slate-900">Home</div>
          <ProfileMenu />
        </div>
      </nav>

      <main className="mx-auto max-w-5xl px-4 py-8">
        <header className="mb-8">
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            Catálogo de Jugadores por Liga
          </h1>
          <p className="mt-1 text-sm text-slate-600">
            Consulte los jugadores y estadísticas de las cinco principales ligas europeas.
          </p>
        </header>

        {isLoading && (
          <div className="flex flex-col items-center justify-center rounded-lg border border-slate-200 bg-white p-12 text-center shadow-sm">
            <div className="h-8 w-8 animate-spin rounded-full border-4 border-slate-300 border-t-slate-800" />
            <p className="mt-4 text-sm font-medium text-slate-600">
              Cargando catálogo de jugadores...
            </p>
          </div>
        )}

        {!isLoading && error && (
          <div className="rounded-lg border border-red-200 bg-red-50 p-6 text-center shadow-sm">
            <p className="text-sm font-semibold text-red-800">{error}</p>
            <button
              type="button"
              onClick={retryLoad}
              className="mt-4 inline-flex items-center rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2"
            >
              Reintentar
            </button>
          </div>
        )}

        {!isLoading && !error && players.length === 0 && (
          <div className="rounded-lg border border-slate-200 bg-white p-8 text-center shadow-sm">
            <p className="text-sm text-slate-600">
              No se encontraron ligas disponibles en este momento.
            </p>
          </div>
        )}

        {!isLoading && !error && players.length > 0 && (
          <div className="space-y-6">
            {leagueGroups.map((league) => {
              return (
                <PlayerLeagueTable
                  key={league.id}
                  tableId={`table-${league.id}`}
                  leagueName={league.name}
                  players={league.players}
                  isExpanded={expandedMap[league.id] ?? true}
                  onToggle={() => handleToggleLeague(league.id)}
                />
              )
            })}
          </div>
        )}
      </main>
    </div>
  )
}

export default HomePage
