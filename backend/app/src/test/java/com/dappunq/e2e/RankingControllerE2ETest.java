package com.dappunq.e2e;

import com.dappunq.controller.PlayerControllerRest;
import com.dappunq.controller.mapper.RankingDtoMapper;
import com.dappunq.controller.mapper.ValuationDtoMapper;
import com.dappunq.exception.GlobalExceptionHandler;
import com.dappunq.model.ranking.RankingFiltros;
import com.dappunq.model.ranking.RankingJugador;
import com.dappunq.model.ranking.RankingPagina;
import com.dappunq.service.PlayerService;
import com.dappunq.service.RankingService;
import com.dappunq.service.ValuationService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RankingControllerE2ETest {
    private org.springframework.test.web.servlet.MockMvc mockMvc(RankingService rankingService) {
        return MockMvcBuilders.standaloneSetup(new PlayerControllerRest(
                mock(PlayerService.class),
                mock(ValuationService.class),
                mock(ValuationDtoMapper.class),
                rankingService,
                new RankingDtoMapper()))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void devuelveRankingFiltradoYPaginado() throws Exception {
        var rankingService = mock(RankingService.class);
        var jugador = new RankingJugador(1, 7L, "Jugador", "Arsenal", "Premier League",
            "Forward", BigDecimal.valueOf(12.50), "ESTRATEGIA_A");
        when(rankingService.consultar(any(RankingFiltros.class)))
            .thenReturn(new RankingPagina(List.of(jugador), 1, 1, 20));

        mockMvc(rankingService).perform(get("/players/ranking")
                .param("league", "Premier League")
                .param("team", "Arsenal")
                .param("position", "Forward"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].position").value(1))
            .andExpect(jsonPath("$.items[0].origin").value("ESTRATEGIA_A"))
            .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void rechazaParametrosInvalidos() throws Exception {
        mockMvc(mock(RankingService.class)).perform(get("/players/ranking?page=0&limit=101"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void devuelveErrorUniformeAnteFalloDeDatos() throws Exception {
        var rankingService = mock(RankingService.class);
        when(rankingService.consultar(any(RankingFiltros.class)))
            .thenThrow(new DataAccessResourceFailureException("base no disponible"));

        mockMvc(rankingService).perform(get("/players/ranking"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.mensaje").value("Error interno al consultar el ranking"));
    }
}
