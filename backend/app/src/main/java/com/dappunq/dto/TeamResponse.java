package com.dappunq.dto;

import com.dappunq.persistence.TeamEntity;

public record TeamResponse(
        Long externalId,
        String name
) {
    public static TeamResponse from(TeamEntity team) {
        if (team == null) {
            return null;
        }
        return new TeamResponse(team.getExternalId(), team.getName());
    }
}
