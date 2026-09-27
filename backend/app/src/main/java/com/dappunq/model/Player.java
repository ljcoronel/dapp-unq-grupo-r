package com.dappunq.model;

import java.util.Objects;

public record Player(
        Integer id,
        String name,
        String section,
        String team,
        PlayerStatistics statistics
) {
    public Player {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El identificador del jugador debe ser positivo");
        }
        name = requireText(name, "El nombre del jugador es obligatorio");
        team = requireText(team, "El equipo del jugador es obligatorio");
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    public PlayerStatistics statisticsOrEmpty() {
        return Objects.requireNonNullElse(statistics, new PlayerStatistics(null, null, null, null));
    }
}
