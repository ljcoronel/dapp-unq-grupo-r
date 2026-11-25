import type { components } from '../api/schema'

export type Player = components['schemas']['PlayerResponse']
export type PlayerSortColumn =
  | 'name'
  | 'team'
  | 'league'
  | 'appearances'
  | 'minutesPlayed'
  | 'goals'
  | 'assists'
  | 'yellowCards'
  | 'passSuccessPercentage'

export interface PlayerFilters {
  league: string
  team: string
  nationality: string
}

export interface PlayerSort {
  column: PlayerSortColumn | null
  direction: 'asc' | 'desc'
}
