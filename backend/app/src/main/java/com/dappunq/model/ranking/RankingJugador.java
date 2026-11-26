package com.dappunq.model.ranking;

import java.math.BigDecimal;
import java.util.Set;

public record RankingJugador(int posicion, long playerId, String nombre, String equipo, String liga,
                             String posicionEnCancha, BigDecimal valorRepresentativo, String origen) {
    private static final Set<String> ORIGINES = Set.of("ESTRATEGIA_A", "ESTRATEGIA_B", "Cotizacion base");

    public RankingJugador {
        if (posicion < 1) {
            throw new IllegalArgumentException("La posicion debe ser mayor o igual a 1");
        }
        if (playerId < 1) {
            throw new IllegalArgumentException("El identificador del jugador debe ser positivo");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del jugador es obligatorio");
        }
        if (valorRepresentativo == null || valorRepresentativo.signum() < 0) {
            throw new IllegalArgumentException("El valor representativo no puede ser negativo");
        }
        if (!ORIGINES.contains(origen)) {
            throw new IllegalArgumentException("El origen del ranking no es valido");
        }
    }
}
