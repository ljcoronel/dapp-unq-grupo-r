package com.dappunq.dto;

import com.dappunq.persistence.NewLeagueEntity;

public record LeagueResponse(
        String codigo,
        String nombre,
        String pais
) {
    public static LeagueResponse from(NewLeagueEntity league) {
        return new LeagueResponse(String.valueOf(league.getExternalId()), league.getName(), league.getCountry());
    }
}
