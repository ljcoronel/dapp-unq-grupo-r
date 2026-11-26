package com.dappunq.service;

import com.dappunq.model.ranking.RankingFiltros;
import com.dappunq.model.ranking.RankingPagina;
import com.dappunq.repository.RankingRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RankingServiceImplTest {
    @Test
    void delegaSinCachearLaConsulta() {
        var repository = mock(RankingRepository.class);
        var filtros = new RankingFiltros(null, null, null, 1, 20);
        var primera = new RankingPagina(List.of(), 0, 1, 20);
        var segunda = new RankingPagina(List.of(), 0, 1, 20);
        when(repository.buscar(filtros)).thenReturn(primera, segunda);
        var service = new RankingServiceImpl(repository);

        assertSame(primera, service.consultar(filtros));
        assertSame(segunda, service.consultar(filtros));
        verify(repository, org.mockito.Mockito.times(2)).buscar(filtros);
    }

    @Test
    void rechazaFiltrosNulos() {
        var service = new RankingServiceImpl(mock(RankingRepository.class));
        assertThrows(IllegalArgumentException.class, () -> service.consultar(null));
    }
}
