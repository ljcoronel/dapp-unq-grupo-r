package com.dappunq.e2e;

import com.dappunq.controller.PlayerControllerRest;
import com.dappunq.controller.mapper.ValuationDtoMapper;
import com.dappunq.dto.LeagueResponse;
import com.dappunq.dto.PlayerResponse;
import com.dappunq.dto.TeamResponse;
import com.dappunq.exception.CatalogUnavailableException;
import com.dappunq.exception.InvalidPlayerIdException;
import com.dappunq.exception.PlayerNotFoundException;
import com.dappunq.service.HoldingsValuationService;
import com.dappunq.service.PlayerServiceImpl;
import com.dappunq.service.ValuationService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlayerControllerE2ETest {

    @Test
    void returnsFlatPlayerListWithTeamAndLeagueData() throws Exception {
        var playerService = mock(PlayerServiceImpl.class);
        var valuationService = mock(ValuationService.class);
        var mapper = mock(ValuationDtoMapper.class);
        var mockMvc = MockMvcBuilders.standaloneSetup(new PlayerControllerRest(playerService, valuationService, mapper))
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

        when(playerService.getAll()).thenReturn(List.of(first, second));

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

    @Test
    void returnsPlayersInOneFlatArrayWithoutLeagueGrouping() throws Exception {
        var playerService = mock(PlayerServiceImpl.class);
        var valuationService = mock(ValuationService.class);
        var mapper = mock(ValuationDtoMapper.class);
        var mockMvc = MockMvcBuilders.standaloneSetup(new PlayerControllerRest(playerService, valuationService, mapper))
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

        when(playerService.getAll()).thenReturn(List.of(premierLeaguePlayer, serieAPlayer));

        mockMvc.perform(get("/players"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].league.codigo").value("PL"))
            .andExpect(jsonPath("$[0].team.name").value("Equipo PL"))
            .andExpect(jsonPath("$[1].league.codigo").value("SA"))
            .andExpect(jsonPath("$[1].team.name").value("Equipo SA"));
    }

    @Test
    void serializesNullStatisticsWithoutReplacingZero() throws Exception {
        var playerService = mock(PlayerServiceImpl.class);
        var valuationService = mock(ValuationService.class);
        var mapper = mock(ValuationDtoMapper.class);
        var mockMvc = MockMvcBuilders.standaloneSetup(new PlayerControllerRest(playerService, valuationService, mapper))
            .setControllerAdvice(new com.dappunq.exception.GlobalExceptionHandler())
            .build();
        when(playerService.getAll()).thenReturn(List.of(new PlayerResponse(
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
        var playerService = mock(PlayerServiceImpl.class);
        var valuationService = mock(ValuationService.class);
        var mapper = mock(ValuationDtoMapper.class);
        var mockMvc = MockMvcBuilders.standaloneSetup(new PlayerControllerRest(playerService, valuationService, mapper))
            .setControllerAdvice(new com.dappunq.exception.GlobalExceptionHandler())
            .build();
        when(playerService.getAll()).thenThrow(
            new CatalogUnavailableException("fallo interno", new RuntimeException()));

        mockMvc.perform(get("/players"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.mensaje").value("El catálogo de jugadores no está disponible"));
    }

    @Test
    void returnsPersistedPlayerIdAndFieldsWithoutAuthorization() throws Exception {
        var playerService = mock(PlayerServiceImpl.class);
        var valuationService = mock(ValuationService.class);
        var mapper = mock(ValuationDtoMapper.class);
        var mockMvc = MockMvcBuilders.standaloneSetup(new PlayerControllerRest(playerService, valuationService, mapper))
            .setControllerAdvice(new com.dappunq.exception.GlobalExceptionHandler())
            .build();
        when(playerService.findPlayer(42L)).thenReturn(new PlayerResponse(
                42L,
                "Jugador",
                new TeamResponse(7L, "Equipo"),
                new LeagueResponse("PL", "Premier League", "Inglaterra"),
                10,
                180,
                75,
                "Delantero",
                12,
                800,
                3,
                1,
                0,
                2,
                1.5f,
                81.0f,
                3.2f,
                1
        ));

        mockMvc.perform(get("/players/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value(42))
                .andExpect(jsonPath("$.name").value("Jugador"))
                .andExpect(jsonPath("$.goals").value(3));
    }

    @Test
    void returnsNotFoundForUnknownPlayer() throws Exception {
        var playerService = mock(PlayerServiceImpl.class);
        var valuationService = mock(ValuationService.class);
        var mapper = mock(ValuationDtoMapper.class);
        var mockMvc = MockMvcBuilders.standaloneSetup(new PlayerControllerRest(playerService, valuationService, mapper))
            .setControllerAdvice(new com.dappunq.exception.GlobalExceptionHandler())
            .build();        when(playerService.findPlayer(999L)).thenThrow(new PlayerNotFoundException(999L));

        mockMvc.perform(get("/players/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe un jugador con el identificador 999"));
    }

    @Test
    void rejectsNonPositiveAndNonNumericIds() throws Exception {
        var playerService = mock(PlayerServiceImpl.class);
        var valuationService = mock(ValuationService.class);
        var mapper = mock(ValuationDtoMapper.class);
        var mockMvc = MockMvcBuilders.standaloneSetup(new PlayerControllerRest(playerService, valuationService, mapper))
            .setControllerAdvice(new com.dappunq.exception.GlobalExceptionHandler())
            .build();
        when(playerService.findPlayer(0L)).thenThrow(
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
