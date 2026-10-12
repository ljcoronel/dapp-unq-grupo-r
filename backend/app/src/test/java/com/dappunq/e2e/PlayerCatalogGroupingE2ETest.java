package com.dappunq.e2e;

import com.dappunq.controller.PlayerControllerRest;
import com.dappunq.dto.LeagueResponse;
import com.dappunq.dto.PlayerResponse;
import com.dappunq.dto.TeamResponse;
import com.dappunq.service.PlayerCatalogReadService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlayerCatalogGroupingE2ETest {
    @Test
    void returnsPlayersInOneFlatArrayWithoutLeagueGrouping() throws Exception {
        var readService = mock(PlayerCatalogReadService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PlayerControllerRest(readService))
                .setControllerAdvice(new com.dappunq.exception.GlobalExceptionHandler())
                .build();
        var premierLeaguePlayer = new PlayerResponse(
                7L,
                "Jugador PL",
                new TeamResponse(17L, "Equipo PL"),
                new LeagueResponse("PL", "Premier League", "Inglaterra"),
                1,
                null,
                null,
                "Delantero",
                1,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        var serieAPlayer = new PlayerResponse(
                42L,
                "Jugador SA",
                new TeamResponse(18L, "Equipo SA"),
                new LeagueResponse("SA", "Serie A", "Italia"),
                2,
                null,
                null,
                "Defensa",
                2,
                null,
                0,
                1,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(readService.findCatalog()).thenReturn(List.of(premierLeaguePlayer, serieAPlayer));

        mockMvc.perform(get("/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].league.codigo").value("PL"))
                .andExpect(jsonPath("$[0].team.name").value("Equipo PL"))
                .andExpect(jsonPath("$[1].league.codigo").value("SA"))
                .andExpect(jsonPath("$[1].team.name").value("Equipo SA"));
    }
}
