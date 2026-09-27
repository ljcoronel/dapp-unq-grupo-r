import { useEffect, useState } from 'react'
import ProfileMenu from '../components/ProfileMenu.jsx'
import PlayerLeagueTable from '../components/PlayerLeagueTable.jsx'
import { fetchPlayers } from '../services/playerService.js'

const LEAGUES = [
  { id: 'premier-league', name: 'Premier League (Inglaterra)' },
  { id: 'bundesliga', name: 'Bundesliga (Alemania)' },
  { id: 'primera-division', name: 'Primera División (España)' },
  { id: 'serie-a', name: 'Serie A (Italia)' },
  { id: 'ligue-1', name: 'Ligue 1 (Francia)' },
]

const getInitialExpansionState = () =>
  LEAGUES.reduce((accumulator, league) => {
    accumulator[league.id] = true
    return accumulator
  }, {})

const HomePage = () => {
  const [leaguesData, setLeaguesData] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [expandedMap, setExpandedMap] = useState(getInitialExpansionState)

  const loadData = async () => {
    setIsLoading(true)
    setError('')

    try {
      const data = await fetchPlayers()

      if (!Array.isArray(data)) {
        throw new Error('El formato de respuesta de los jugadores no es válido.')
      }

      setLeaguesData(data)
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

        if (isCancelled) {
          return
        }

        if (!Array.isArray(data)) {
          throw new Error('El formato de respuesta de los jugadores no es válido.')
        }

        setLeaguesData(data)
      } catch (requestError) {
        if (isCancelled) {
          return
        }

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

  const handleToggleLeague = (leagueId) => {
    setExpandedMap((previousState) => ({
      ...previousState,
      [leagueId]: !previousState[leagueId],
    }))
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
              onClick={loadData}
              className="mt-4 inline-flex items-center rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2"
            >
              Reintentar
            </button>
          </div>
        )}

        {!isLoading && !error && leaguesData.length === 0 && (
          <div className="rounded-lg border border-slate-200 bg-white p-8 text-center shadow-sm">
            <p className="text-sm text-slate-600">
              No se encontraron ligas disponibles en este momento.
            </p>
          </div>
        )}

        {!isLoading && !error && leaguesData.length > 0 && (
          <div className="space-y-6">
            {LEAGUES.map((league, index) => {
              const playersForLeague = Array.isArray(leaguesData[index])
                ? leaguesData[index]
                : []

              return (
                <PlayerLeagueTable
                  key={league.id}
                  tableId={`table-${league.id}`}
                  leagueName={league.name}
                  players={playersForLeague}
                  isExpanded={Boolean(expandedMap[league.id])}
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
