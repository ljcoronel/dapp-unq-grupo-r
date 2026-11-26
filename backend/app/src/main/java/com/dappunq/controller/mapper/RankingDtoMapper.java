package com.dappunq.controller.mapper;

import com.dappunq.dto.ranking.RankingPageResponse;
import com.dappunq.dto.ranking.RankingPlayerResponse;
import com.dappunq.model.ranking.RankingPagina;
import org.springframework.stereotype.Component;

@Component
public class RankingDtoMapper {
    public RankingPageResponse toResponse(RankingPagina pagina) {
        var items = pagina.items().stream()
            .map(jugador -> new RankingPlayerResponse(
                jugador.posicion(),
                jugador.playerId(),
                jugador.nombre(),
                jugador.equipo(),
                jugador.liga(),
                jugador.posicionEnCancha(),
                jugador.valorRepresentativo(),
                jugador.origen()
            ))
            .toList();
        return new RankingPageResponse(items, pagina.total(), pagina.pagina(), pagina.limite());
    }
}
