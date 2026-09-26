package com.dappunq.e2e;

import com.dappunq.controller.PlayerControllerRest;
import com.dappunq.dto.LeagueResponse;
import com.dappunq.dto.PlayerResponse;
import com.dappunq.exception.PlayerNotFoundException;
import com.dappunq.exception.InvalidPlayerIdException;
import com.dappunq.service.PlayerCatalogReadService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlayerByIdE2ETest {
    @Test
    void returnsPersistedPlayerIdAndFieldsWithoutAuthorization() throws Exception {
        var readService = mock(PlayerCatalogReadService.class);
        var mockMvc = MockMvcBuilders.standaloneSetup(new PlayerControllerRest(readService))
                .setControllerAdvice(new com.dappunq.exception.GlobalExceptionHandler())
                .build();
        when(readService.findPlayer(42)).thenReturn(new PlayerResponse(42, "Jugador",
                "Delantero", "Equipo", new LeagueResponse("PL", "Premier League", "Inglaterra"),
                10, 3, 1, 0));

        mockMvc.perform(get("/players/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.nombre").value("Jugador"))
                .andExpect(jsonPath("$.goles").value(3));
    }

    @Test
    void returnsNotFoundForUnknownPlayer() throws Exception {
        var readService = mock(PlayerCatalogReadService.class);
        var mockMvc = MockMvcBuilders.standaloneSetup(new PlayerControllerRest(readService))
                .setControllerAdvice(new com.dappunq.exception.GlobalExceptionHandler())
                .build();
        when(readService.findPlayer(999)).thenThrow(new PlayerNotFoundException(999));

        mockMvc.perform(get("/players/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe un jugador con el identificador 999"));
    }

    @Test
    void rejectsNonPositiveAndNonNumericIds() throws Exception {
        var readService = mock(PlayerCatalogReadService.class);
        var mockMvc = MockMvcBuilders.standaloneSetup(new PlayerControllerRest(readService))
                .setControllerAdvice(new com.dappunq.exception.GlobalExceptionHandler())
                .build();
        when(readService.findPlayer(0)).thenThrow(
                new InvalidPlayerIdException("El identificador del jugador debe ser positivo"));

        mockMvc.perform(get("/players/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El identificador del jugador debe ser positivo"));
        mockMvc.perform(get("/players/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(
                        "El identificador del jugador debe ser un número entero positivo"));
    }
}
