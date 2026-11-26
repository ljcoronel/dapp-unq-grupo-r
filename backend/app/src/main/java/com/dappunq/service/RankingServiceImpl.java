package com.dappunq.service;

import com.dappunq.model.ranking.RankingFiltros;
import com.dappunq.model.ranking.RankingPagina;
import com.dappunq.repository.RankingRepository;
import org.springframework.stereotype.Service;

@Service
public class RankingServiceImpl implements RankingService {
    private final RankingRepository rankingRepository;

    public RankingServiceImpl(RankingRepository rankingRepository) {
        this.rankingRepository = rankingRepository;
    }

    @Override
    public RankingPagina consultar(RankingFiltros filtros) {
        if (filtros == null) {
            throw new IllegalArgumentException("Los filtros del ranking son obligatorios");
        }
        return rankingRepository.buscar(filtros);
    }
}
