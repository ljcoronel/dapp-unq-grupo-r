package com.dappunq.repository;

import com.dappunq.model.ranking.RankingFiltros;
import com.dappunq.model.ranking.RankingPagina;

public interface RankingRepository {
    RankingPagina buscar(RankingFiltros filtros);
}
