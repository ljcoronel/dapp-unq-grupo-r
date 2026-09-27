package com.dappunq.e2e;

import com.dappunq.controller.PlayerControllerRest;
import com.dappunq.dto.LeagueResponse;
import com.dappunq.dto.PlayerResponse;
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
    void preservesFiveIndependentLeaguePositionsIncludingEmptyArrays() throws Exception {
        var readService = mock(PlayerCatalogReadService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PlayerControllerRest(readService)).build();
        var premierLeaguePlayer = new PlayerResponse(
                7, "Jugador PL", "Delantero", "Equipo PL",
                new LeagueResponse("PL", "Premier League", "Inglaterra"),
                1, 1, null, null);
        var serieAPlayer = new PlayerResponse(
                42, "Jugador SA", "Delantero", "Equipo SA",
                new LeagueResponse("SA", "Serie A", "Italia"),
                2, 0, 1, null);

        when(readService.findCatalog()).thenReturn(List.of(
                List.of(premierLeaguePlayer),
                List.of(),
                List.of(),
                List.of(serieAPlayer),
                List.of()));

        mockMvc.perform(get("/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0][0].liga.codigo").value("PL"))
                .andExpect(jsonPath("$[1]").isArray())
                .andExpect(jsonPath("$[1]").isEmpty())
                .andExpect(jsonPath("$[2]").isArray())
                .andExpect(jsonPath("$[2]").isEmpty())
                .andExpect(jsonPath("$[3][0].liga.codigo").value("SA"))
                .andExpect(jsonPath("$[4]").isArray())
                .andExpect(jsonPath("$[4]").isEmpty());
    }
}
