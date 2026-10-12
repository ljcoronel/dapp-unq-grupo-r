package com.dappunq.service;

import com.dappunq.dto.PlayerResponse;
import com.dappunq.exception.CatalogUnavailableException;
import com.dappunq.exception.InvalidPlayerIdException;
import com.dappunq.persistence.LeagueEntity;
import com.dappunq.persistence.PlayerEntity;
import com.dappunq.persistence.PlayerRepository;
import com.dappunq.persistence.TeamEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlayerCatalogReadServiceTest {
    @Test
    void findCatalogMapsEveryEntityToFlatPlayerResponse() {
        var repository = mock(PlayerRepository.class);
        var service = new PlayerCatalogReadService(repository);
        var team = new TeamEntity(11L, "Equipo");
        var league = new LeagueEntity(22L, "Premier League", "Inglaterra");
        var entity = new PlayerEntity(
                7L,
                "Jugador",
                team,
                league,
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
        when(repository.findAll()).thenReturn(List.of(entity));

        List<PlayerResponse> result = service.findCatalog();

        assertEquals(1, result.size());
        assertEquals(7L, result.get(0).externalId());
        assertEquals("Jugador", result.get(0).name());
        assertNotNull(result.get(0).team());
        assertEquals("Equipo", result.get(0).team().name());
        assertEquals("22", result.get(0).league().codigo());
        assertEquals(3, result.get(0).goals());
        verify(repository).findAll();
    }

    @Test
    void findCatalogReturnsEmptyListWhenRepositoryIsEmpty() {
        var repository = mock(PlayerRepository.class);
        var service = new PlayerCatalogReadService(repository);
        when(repository.findAll()).thenReturn(List.of());

        var result = service.findCatalog();

        assertTrue(result.isEmpty());
        verify(repository).findAll();
    }

    @Test
    void findCatalogPreservesNullsAndZeros() {
        var repository = mock(PlayerRepository.class);
        var service = new PlayerCatalogReadService(repository);
        var team = new TeamEntity(13L, "Equipo Nulls");
        var league = new LeagueEntity(7L, "LaLiga", "España");
        var entity = new PlayerEntity(
                99L,
                "Jugador con nulos",
                team,
                league,
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
        );
        when(repository.findAll()).thenReturn(List.of(entity));

        PlayerResponse response = service.findCatalog().get(0);

        assertNull(response.age());
        assertNull(response.assists());
        assertEquals(0, response.goals());
        assertEquals(0, response.yellowCards());
    }

    @Test
    void findCatalogWrapsRepositoryFailures() {
        var repository = mock(PlayerRepository.class);
        var service = new PlayerCatalogReadService(repository);
        when(repository.findAll()).thenThrow(new RuntimeException("boom"));

        assertThrows(CatalogUnavailableException.class, service::findCatalog);
    }

    @Test
    void findPlayerUsesExternalIdAndRejectsInvalidIds() {
        var repository = mock(PlayerRepository.class);
        var service = new PlayerCatalogReadService(repository);
        var team = new TeamEntity(33L, "Equipo");
        var league = new LeagueEntity(21L, "Bundesliga", "Alemania");
        var entity = new PlayerEntity(
                42L,
                "Jugador 42",
                team,
                league,
                29,
                186,
                80,
                "Pivote",
                18,
                1400,
                7,
                5,
                2,
                1,
                1.0f,
                88.0f,
                5.5f,
                2
        );
        when(repository.findByExternalId(42L)).thenReturn(java.util.Optional.of(entity));

        PlayerResponse response = service.findPlayer(42);

        assertEquals(42L, response.externalId());
        assertEquals("Jugador 42", response.name());
        assertEquals("Pivote", response.position());

        assertThrows(InvalidPlayerIdException.class, () -> service.findPlayer(0));
        assertThrows(InvalidPlayerIdException.class, () -> service.findPlayer(null));
    }
}
