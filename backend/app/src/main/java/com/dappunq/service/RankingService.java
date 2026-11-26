package com.dappunq.service;

import com.dappunq.model.ranking.RankingFiltros;
import com.dappunq.model.ranking.RankingPagina;

public interface RankingService {
    RankingPagina consultar(RankingFiltros filtros);
}
