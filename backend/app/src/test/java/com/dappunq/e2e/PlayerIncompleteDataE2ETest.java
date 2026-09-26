package com.dappunq.e2e;

import com.dappunq.controller.PlayerControllerRest;
import com.dappunq.dto.PlayerResponse;
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
        when(readService.findCatalog()).thenReturn(List.of(
                List.of(new PlayerResponse(42, "Jugador", null, "Equipo", null,
                        null, 0, null, 0)),
                List.of(), List.of(), List.of(), List.of()));

        mockMvc.perform(get("/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0][0].nombre").value("Jugador"))
                .andExpect(jsonPath("$[0][0].partidosJugados").doesNotExist())
                .andExpect(jsonPath("$[0][0].goles").value(0))
                .andExpect(jsonPath("$[0][0].asistencias").doesNotExist())
                .andExpect(jsonPath("$[0][0].penaltis").value(0));
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
