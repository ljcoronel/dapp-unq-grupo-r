package com.dappunq.service;

import com.dappunq.dto.PlayerResponse;
import com.dappunq.dto.LeagueResponse;
import com.dappunq.dto.footballdata.ScorerPayload;
import com.dappunq.model.Player;
import com.dappunq.model.PlayerStatistics;
import com.dappunq.persistence.PlayerEntity;

import java.util.List;
import java.util.Objects;

public final class PlayerMapper {
    private PlayerMapper() {
    }

    public static List<Player> toPlayers(List<ScorerPayload> scorers) {
        if (scorers == null) {
            return List.of();
        }
        return scorers.stream()
                .map(PlayerMapper::toPlayerOrNull)
                .filter(Objects::nonNull)
                .toList();
    }

    public static Player toPlayer(ScorerPayload scorer) {
        Player player = toPlayerOrNull(scorer);
        if (player == null) {
            throw new IllegalArgumentException("El registro externo del jugador no es válido");
        }
        return player;
    }

    private static Player toPlayerOrNull(ScorerPayload scorer) {
        if (scorer == null || scorer.player() == null || scorer.team() == null) {
            return null;
        }
        try {
            var payload = scorer.player();
            return new Player(payload.id(), payload.name(), normalize(scorer.player().section()),
                    scorer.team().name(), new PlayerStatistics(
                    scorer.playedMatches(), scorer.goals(), scorer.assists(), scorer.penalties()));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public static PlayerEntity toEntity(Player player) {
        var statistics = player.statisticsOrEmpty();
        return new PlayerEntity(player.id(), player.name(), player.section(), player.team(),
                statistics.playedMatches(), statistics.goals(), statistics.assists(), statistics.penalties());
    }

    public static PlayerResponse toResponse(PlayerEntity entity) {
        return new PlayerResponse(entity.getId(), entity.getName(), entity.getSection(), entity.getTeam(),
                LeagueResponse.from(entity.getLeague()),
                entity.getPlayedMatches(), entity.getGoals(), entity.getAssists(), entity.getPenalties());
    }

    public static Player toDomain(PlayerEntity entity) {
        return new Player(entity.getId(), entity.getName(), entity.getSection(), entity.getTeam(),
                new PlayerStatistics(entity.getPlayedMatches(), entity.getGoals(),
                        entity.getAssists(), entity.getPenalties()));
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
