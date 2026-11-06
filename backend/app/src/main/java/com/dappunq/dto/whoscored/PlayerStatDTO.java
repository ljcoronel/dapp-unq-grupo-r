package com.dappunq.dto.whoscored;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlayerStatDTO(
    Long playerId,
    String name,
    Long teamId,
    String teamName,
    Long tournamentId,
    String tournamentName,
    String tournamentRegionName,
    Integer age,
    Integer height,
    Integer weight,
    String positionText,
    Integer apps,
    Integer minsPlayed,
    Integer goal,
    Integer assistTotal,
    Float yellowCard,
    Float redCard,
    Float shotsPerGame,
    Float passSuccess,
    Float aerialWonPerGame,
    Integer manOfTheMatch
) {
}
