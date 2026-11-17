/**
 * Normaliza la respuesta del endpoint de jugadores para presentar una lista plana y uniforme.
 * Maneja tanto arrays planos como arrays anidados de jugadores agrupados por liga.
 *
 * @param {Array<unknown>} data
 * @returns {Array<Object>}
 */
export function normalizePlayers(data) {
  if (!Array.isArray(data)) {
    return []
  }

  // Si la respuesta es una lista de listas (ej. agrupada por liga), la aplanamos
  const rawList = Array.isArray(data[0]) ? data.flat() : data

  return rawList
    .filter((item) => item && typeof item === 'object')
    .map((p, index) => {
      const id = p.externalId ?? p.id ?? index
      const name = (p.name || p.nombre || '').trim()
      const team = (p.team?.name || p.equipo || '').trim()
      const league = (p.league?.nombre || p.liga || '').trim()
      const nationality = (
        p.league?.pais ||
        p.nacionalidad ||
        p.pais ||
        ''
      ).trim()

      return {
        id,
        name,
        team,
        league,
        nationality,
        appearances: Number(
          p.appearances ?? p.partidosJugados ?? p.apariciones ?? 0,
        ),
        minutesPlayed: Number(p.minutesPlayed ?? p.minutosJugados ?? 0),
        goals: Number(p.goals ?? p.goles ?? 0),
        assists: Number(p.assists ?? p.asistencias ?? 0),
        yellowCards: Number(p.yellowCards ?? p.tarjetasAmarillas ?? 0),
        passSuccessPercentage: Number(
          p.passSuccessPercentage ?? p.porcentajePasesExitosos ?? 0,
        ),
        raw: p,
      }
    })
}
