package com.dappunq.dto.footballdata;

public record ScorerPayload(
        PlayerPayload player,
        TeamPayload team,
        Integer playedMatches,
        Integer goals,
        Integer assists,
        Integer penalties
) {
}
