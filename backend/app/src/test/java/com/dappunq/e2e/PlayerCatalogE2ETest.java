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

class PlayerCatalogE2ETest {
    @Test
    void returnsFlatPlayerListWithTeamAndLeagueData() throws Exception {
        var readService = mock(PlayerCatalogReadService.class);
        var mockMvc = MockMvcBuilders.standaloneSetup(new PlayerControllerRest(readService))
                .setControllerAdvice(new com.dappunq.exception.GlobalExceptionHandler())
                .build();

        var first = new PlayerResponse(
                7L,
                "Jugador",
                new TeamResponse(12L, "Equipo"),
                new LeagueResponse("PL", "Premier League", "Inglaterra"),
                24,
                180,
                75,
                "Delantero",
                12,
                900,
                3,
                2,
                1,
                0,
                2.5f,
                81.5f,
                4.1f,
                1
        );
        var second = new PlayerResponse(
                42L,
                "Jugador 2",
                new TeamResponse(16L, "Equipo 2"),
                new LeagueResponse("SA", "Serie A", "Italia"),
                27,
                182,
                78,
                "Mediocampista",
                20,
                1500,
                5,
                4,
                0,
                1,
                1.2f,
                76.0f,
                2.0f,
                0
        );

        when(readService.findCatalog()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].externalId").value(7))
                .andExpect(jsonPath("$[0].name").value("Jugador"))
                .andExpect(jsonPath("$[0].team.externalId").value(12))
                .andExpect(jsonPath("$[0].team.name").value("Equipo"))
                .andExpect(jsonPath("$[0].league.codigo").value("PL"))
                .andExpect(jsonPath("$[0].league.nombre").value("Premier League"))
                .andExpect(jsonPath("$[0].goals").value(3))
                .andExpect(jsonPath("$[1].externalId").value(42))
                .andExpect(jsonPath("$[1].league.codigo").value("SA"));
    }
}
