package com.dappunq.dto;

import com.dappunq.model.Player;

public record PlayerResponse(
        Integer id,
        String nombre,
        String seccion,
        String equipo,
        LeagueResponse liga,
        Integer partidosJugados,
        Integer goles,
        Integer asistencias,
        Integer penaltis
) {
    public static PlayerResponse from(Player player) {
        var statistics = player.statisticsOrEmpty();
        return new PlayerResponse(player.id(), player.name(), player.section(), player.team(), null,
                statistics.playedMatches(), statistics.goals(), statistics.assists(), statistics.penalties());
    }
}
