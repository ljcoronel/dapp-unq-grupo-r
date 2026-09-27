package com.dappunq.model;

public record PlayerStatistics(
        Integer playedMatches,
        Integer goals,
        Integer assists,
        Integer penalties
) {
    public PlayerStatistics {
        if (playedMatches != null && playedMatches < 0
                || goals != null && goals < 0
                || assists != null && assists < 0
                || penalties != null && penalties < 0) {
            throw new IllegalArgumentException("Las estadísticas no pueden ser negativas");
        }
    }
}
