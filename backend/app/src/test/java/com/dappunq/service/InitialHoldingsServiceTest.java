package com.dappunq.service;

import com.dappunq.persistence.HoldingEntity;
import com.dappunq.persistence.HoldingRepository;
import com.dappunq.persistence.LeagueEntity;
import com.dappunq.persistence.PlayerEntity;
import com.dappunq.persistence.PlayerRepository;
import com.dappunq.persistence.TeamEntity;
import com.dappunq.persistence.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InitialHoldingsServiceTest {

    @Mock
    private SuperuserInitializationService superuserService;

    @Mock
    private HoldingRepository holdingRepository;

    @Mock
    private PlayerRepository playerRepository;

    private InitialHoldingsService initialHoldingsService;
    private UserEntity superuser;
    private PlayerEntity player1;
    private PlayerEntity player2;

    @BeforeEach
    void setUp() {
        initialHoldingsService = new InitialHoldingsService(superuserService, holdingRepository, playerRepository);
        superuser = new UserEntity(999L, "superuser", "hash999");

        TeamEntity team = new TeamEntity(1L, "Team A");
        LeagueEntity league = new LeagueEntity(1L, "League A", "Region A");
        player1 = new PlayerEntity(1L, "Player 1", team, league, 25, 180, 75, "Forward", 10, 900, 5, 2, 0, 0, 2f, 80f, 2f, 2);
        player1.setId(1L);
        player2 = new PlayerEntity(2L, "Player 2", team, league, 28, 185, 80, "Defender", 12, 1080, 1, 0, 1, 0, 0.5f, 90f, 4f, 0);
        player2.setId(2L);
    }

    @Test
    void inicializacionCrea100TokensBajoSuperusuario() {
        when(superuserService.getOrCreateSuperuser()).thenReturn(superuser);
        when(holdingRepository.findByUserIdAndPlayerId(999L, 1L)).thenReturn(Optional.empty());
        when(holdingRepository.save(any(HoldingEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        HoldingEntity result = initialHoldingsService.initializeHoldingForPlayer(player1);

        assertNotNull(result);
        assertEquals(999L, result.getUser().getId());
        assertEquals(1L, result.getPlayer().getId());
        assertEquals(100, result.getTokens());
        verify(holdingRepository).save(any(HoldingEntity.class));
    }

    @Test
    void inicializacionEsIdempotenteYNoDuplicaTenenciasExistentes() {
        HoldingEntity existingHolding = new HoldingEntity(superuser, player1, 100);
        when(superuserService.getOrCreateSuperuser()).thenReturn(superuser);
        when(holdingRepository.findByUserIdAndPlayerId(999L, 1L)).thenReturn(Optional.of(existingHolding));

        HoldingEntity result = initialHoldingsService.initializeHoldingForPlayer(player1);

        assertEquals(100, result.getTokens());
        verify(holdingRepository, never()).save(any(HoldingEntity.class));
    }

    @Test
    void inicializacionParaTodosLosJugadoresRegistra100TokensPorJugador() {
        when(superuserService.getOrCreateSuperuser()).thenReturn(superuser);
        when(playerRepository.findAll()).thenReturn(List.of(player1, player2));
        when(holdingRepository.findByUserIdAndPlayerId(999L, 1L)).thenReturn(Optional.empty());
        when(holdingRepository.findByUserIdAndPlayerId(999L, 2L)).thenReturn(Optional.empty());
        when(holdingRepository.save(any(HoldingEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        List<HoldingEntity> results = initialHoldingsService.initializeHoldingsForAllPlayers();

        assertEquals(2, results.size());
        assertEquals(100, results.get(0).getTokens());
        assertEquals(100, results.get(1).getTokens());
    }
}
