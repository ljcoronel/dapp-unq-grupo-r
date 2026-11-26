package com.dappunq.model.ranking;

import java.util.List;

public record RankingPagina(List<RankingJugador> items, long total, int pagina, int limite) {
    public RankingPagina {
        items = items == null ? List.of() : List.copyOf(items);
        if (total < 0) {
            throw new IllegalArgumentException("El total no puede ser negativo");
        }
        if (pagina < 1) {
            throw new IllegalArgumentException("La pagina debe ser mayor o igual a 1");
        }
        if (limite < 1 || limite > 100) {
            throw new IllegalArgumentException("El limite debe estar entre 1 y 100");
        }
    }
}
