package com.dappunq.dto;

import com.dappunq.persistence.LeagueEntity;

public record LeagueResponse(
        String codigo,
        String nombre,
        String pais
) {
    public static LeagueResponse from(LeagueEntity league) {
        return new LeagueResponse(league.getCode(), league.getName(), league.getCountry());
    }
}
