package com.dappunq.dto.footballdata;

import java.util.List;

public record FootballDataResponse(CompetitionPayload competition, List<ScorerPayload> scorers) {
    public FootballDataResponse {
        scorers = scorers == null ? List.of() : List.copyOf(scorers);
    }
}
