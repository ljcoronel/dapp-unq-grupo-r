package com.dappunq.dto;

public record PlayerResponse(
        Long externalId,
        String name,
        TeamResponse team,
        LeagueResponse league,
        Integer age,
        Integer heightCM,
        Integer weightKG,
        String position,
        Integer appearances,
        Integer minutesPlayed,
        Integer goals,
        Integer assists,
        Integer yellowCards,
        Integer redCards,
        Float avgShotsPerGame,
        Float passSuccessPercentage,
        Float aerialsWon,
        Integer timesManOfTheMatch
) {
}
