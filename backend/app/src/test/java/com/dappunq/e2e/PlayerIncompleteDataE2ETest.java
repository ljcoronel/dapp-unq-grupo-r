package com.dappunq.e2e;

import com.dappunq.controller.PlayerControllerRest;
import com.dappunq.dto.LeagueResponse;
import com.dappunq.dto.PlayerResponse;
import com.dappunq.dto.TeamResponse;
import com.dappunq.exception.CatalogUnavailableException;
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

class PlayerIncompleteDataE2ETest {
    @Test
    void serializesNullStatisticsWithoutReplacingZero() throws Exception {
        var readService = mock(PlayerCatalogReadService.class);
        var mockMvc = MockMvcBuilders.standaloneSetup(new PlayerControllerRest(readService))
                .setControllerAdvice(new com.dappunq.exception.GlobalExceptionHandler())
                .build();
        when(readService.findCatalog()).thenReturn(List.of(new PlayerResponse(
                42L,
                "Jugador",
                new TeamResponse(99L, "Equipo"),
                new LeagueResponse("PL", "Premier League", "Inglaterra"),
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                null,
                0,
                null,
                null,
                null,
                null,
                null
        )));

        mockMvc.perform(get("/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Jugador"))
                .andExpect(jsonPath("$[0].age").doesNotExist())
                .andExpect(jsonPath("$[0].goals").value(0))
                .andExpect(jsonPath("$[0].assists").doesNotExist())
                .andExpect(jsonPath("$[0].yellowCards").value(0));
    }

    @Test
    void returnsUnavailableCatalogErrorWithoutPartialPayload() throws Exception {
        var readService = mock(PlayerCatalogReadService.class);
        var mockMvc = MockMvcBuilders.standaloneSetup(new PlayerControllerRest(readService))
                .setControllerAdvice(new com.dappunq.exception.GlobalExceptionHandler())
                .build();
        when(readService.findCatalog()).thenThrow(
                new CatalogUnavailableException("fallo interno", new RuntimeException()));

        mockMvc.perform(get("/players"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.mensaje").value("El catálogo de jugadores no está disponible"));
    }
}
