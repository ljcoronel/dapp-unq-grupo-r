package com.dappunq.model.valuation;

/**
 * Objeto de valor de dominio que representa las metricas de un jugador en el momento
 * del calculo de valuacion. No contiene anotaciones JPA ni dependencias de framework.
 */
public class PlayerSnapshot {

    private final Long jugadorId;
    private final String position;
    private final Integer goals;
    private final Integer assists;
    private final Float passSuccessPercentage;
    private final Float aerialsWon;
    private final Integer timesManOfTheMatch;
    private final Integer minutesPlayed;
    private final Integer appearances;

    public PlayerSnapshot(Long jugadorId, String position,
                          Integer goals, Integer assists,
                          Float passSuccessPercentage, Float aerialsWon,
                          Integer timesManOfTheMatch,
                          Integer minutesPlayed, Integer appearances) {
        this.jugadorId = jugadorId;
        this.position = position;
        this.goals = goals;
        this.assists = assists;
        this.passSuccessPercentage = passSuccessPercentage;
        this.aerialsWon = aerialsWon;
        this.timesManOfTheMatch = timesManOfTheMatch;
        this.minutesPlayed = minutesPlayed;
        this.appearances = appearances;
    }

    public Long getJugadorId() { return jugadorId; }
    public String getPosition() { return position; }
    public Integer getGoals() { return goals; }
    public Integer getAssists() { return assists; }
    public Float getPassSuccessPercentage() { return passSuccessPercentage; }
    public Float getAerialsWon() { return aerialsWon; }
    public Integer getTimesManOfTheMatch() { return timesManOfTheMatch; }
    public Integer getMinutesPlayed() { return minutesPlayed; }
    public Integer getAppearances() { return appearances; }
}
