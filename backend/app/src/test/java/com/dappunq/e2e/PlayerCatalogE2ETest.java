package com.dappunq.e2e;

import com.dappunq.dto.PlayerResponse;
import com.dappunq.dto.LeagueResponse;
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

class PlayerCatalogE2ETest {
    @Test
    void returnsFiveOrderedListsIncludingEmptyLeagues() throws Exception {
        var readService = mock(PlayerCatalogReadService.class);
        var mockMvc = MockMvcBuilders.standaloneSetup(
                        new com.dappunq.controller.PlayerControllerRest(readService))
                .build();
        var player = new PlayerResponse(7, "Jugador", "Defensa", "Equipo",
                new LeagueResponse("PL", "Premier League", "Inglaterra"), 1, 0, null, null);
        when(readService.findCatalog()).thenReturn(List.of(
                List.of(player), List.of(), List.of(), List.of(), List.of()));

        mockMvc.perform(get("/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0][0].id").value(7))
                .andExpect(jsonPath("$[0][0].nombre").value("Jugador"))
                .andExpect(jsonPath("$[0][0].liga.codigo").value("PL"))
                .andExpect(jsonPath("$[0][0].liga.nombre").value("Premier League"))
                .andExpect(jsonPath("$[0][0].goles").value(0))
                .andExpect(jsonPath("$[0][0].asistencias").doesNotExist())
                .andExpect(jsonPath("$[1]").isEmpty())
                .andExpect(jsonPath("$[4]").isEmpty());
    }

    @Test
    void returnsPlayerIncludingItsLeague() throws Exception {
        var readService = mock(PlayerCatalogReadService.class);
        var mockMvc = MockMvcBuilders.standaloneSetup(
                        new com.dappunq.controller.PlayerControllerRest(readService))
                .build();
        when(readService.findPlayer(7)).thenReturn(new PlayerResponse(7, "Jugador", "Defensa", "Equipo",
                new LeagueResponse("SA", "Serie A", "Italia"), 1, 2, 0, null));

        mockMvc.perform(get("/players/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.liga.codigo").value("SA"))
                .andExpect(jsonPath("$.liga.nombre").value("Serie A"));
    }
}
