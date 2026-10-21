package com.dappunq.service;

import com.dappunq.dto.valuation.RecalculationResult;
import com.dappunq.model.valuation.ExecutionType;
import com.dappunq.model.valuation.StrategyStatus;
import com.dappunq.model.valuation.StrategyType;
import com.dappunq.model.valuation.ValuationDomainException;
import com.dappunq.persistence.HoldingEntity;
import com.dappunq.persistence.HoldingRepository;
import com.dappunq.persistence.LeagueEntity;
import com.dappunq.persistence.PlayerEntity;
import com.dappunq.persistence.PlayerRepository;
import com.dappunq.persistence.StrategyConfigEntity;
import com.dappunq.persistence.StrategyConfigRepository;
import com.dappunq.persistence.TeamEntity;
import com.dappunq.persistence.UserEntity;
import com.dappunq.persistence.ValuationHistoryRepository;
import com.dappunq.persistence.ValuationPersistenceMapper;
import com.dappunq.persistence.ValuationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeeklyValuationServiceTest {

    @Mock
    private WhoScoredScrapingService scrapingService;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private StrategyConfigRepository strategyConfigRepository;

    @Mock
    private HoldingRepository holdingRepository;

    @Mock
    private ValuationRepository valuationRepository;

    @Mock
    private ValuationHistoryRepository valuationHistoryRepository;

    private final ValuationPersistenceMapper mapper = new ValuationPersistenceMapper();
    private WeeklyValuationService service;

    private UserEntity userWithStrategy;
    private UserEntity userWithoutStrategy;
    private PlayerEntity player;
    private Map<String, BigDecimal> validWeightsA;
    private Map<String, BigDecimal> validWeightsB;

    @BeforeEach
    void setUp() {
        service = new WeeklyValuationService(
                scrapingService,
                playerRepository,
                strategyConfigRepository,
                holdingRepository,
                valuationRepository,
                valuationHistoryRepository,
                mapper
        );

        userWithStrategy = new UserEntity(1L, "user1", "hash1");
        userWithoutStrategy = new UserEntity(2L, "user2", "hash2");

        TeamEntity team = new TeamEntity(1L, "Team A");
        LeagueEntity league = new LeagueEntity(1L, "League A", "Region A");
        player = new PlayerEntity(10L, "Player 1", team, league, 25, 180, 75, "Defender", 10, 900, 2, 1, 0, 0, 1.5f, 85f, 3f, 1);

        validWeightsA = new HashMap<>();
        validWeightsA.put("goals", BigDecimal.valueOf(30));
        validWeightsA.put("assists", BigDecimal.valueOf(25));
        validWeightsA.put("passSuccessPercentage", BigDecimal.valueOf(15));
        validWeightsA.put("aerialsWon", BigDecimal.valueOf(15));
        validWeightsA.put("timesManOfTheMatch", BigDecimal.valueOf(15));

        validWeightsB = new HashMap<>();
        validWeightsB.put("passSuccessPercentage", BigDecimal.valueOf(35));
        validWeightsB.put("aerialsWon", BigDecimal.valueOf(20));
        validWeightsB.put("minutesPlayed", BigDecimal.valueOf(20));
        validWeightsB.put("appearances", BigDecimal.valueOf(20));
    }

    @Test
    void ordenDeEjecucionScrapeAntesDeCalculo() {
        when(playerRepository.findAll()).thenReturn(List.of(player));
        StrategyConfigEntity active = new StrategyConfigEntity(userWithStrategy, StrategyType.ESTRATEGIA_A, mapper.write(validWeightsA), StrategyStatus.ACTUAL, Instant.now());
        when(strategyConfigRepository.findAllByEstado(StrategyStatus.PENDIENTE)).thenReturn(Collections.emptyList());
        when(strategyConfigRepository.findAllByEstado(StrategyStatus.ACTUAL)).thenReturn(List.of(active));

        HoldingEntity holding = new HoldingEntity(userWithStrategy, player, 100);
        when(holdingRepository.findByUserId(1L)).thenReturn(List.of(holding));

        RecalculationResult result = service.execute(ExecutionType.DOMINICAL);

        InOrder inOrder = inOrder(scrapingService, playerRepository, holdingRepository);
        inOrder.verify(scrapingService).scrape("50");
        inOrder.verify(playerRepository).findAll();
        inOrder.verify(holdingRepository).save(any(HoldingEntity.class));

        assertEquals("COMPLETED", result.status());
        assertEquals(1, result.playersScraped());
        assertEquals(1, result.recalculatedHoldings());
    }

    @Test
    void falloEnScrapingPropagaExcepcionYSinGuardarValuaciones() {
        doThrow(new RuntimeException("Scraping connection error")).when(scrapingService).scrape("50");

        assertThrows(RuntimeException.class, () -> service.execute(ExecutionType.DOMINICAL));

        verify(playerRepository, never()).findAll();
        verify(holdingRepository, never()).save(any());
        verify(valuationRepository, never()).save(any());
        verify(valuationHistoryRepository, never()).save(any());
    }

    @Test
    void snapshotVacioLanzaExcepcionDeDominio() {
        when(playerRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(ValuationDomainException.class, () -> service.execute(ExecutionType.DOMINICAL));

        verify(holdingRepository, never()).save(any());
    }

    @Test
    void omiteUsuariosSinEstrategia() {
        when(playerRepository.findAll()).thenReturn(List.of(player));
        when(strategyConfigRepository.findAllByEstado(StrategyStatus.PENDIENTE)).thenReturn(Collections.emptyList());
        StrategyConfigEntity active = new StrategyConfigEntity(userWithStrategy, StrategyType.ESTRATEGIA_A, mapper.write(validWeightsA), StrategyStatus.ACTUAL, Instant.now());
        when(strategyConfigRepository.findAllByEstado(StrategyStatus.ACTUAL)).thenReturn(List.of(active));

        HoldingEntity holding1 = new HoldingEntity(userWithStrategy, player, 100);
        when(holdingRepository.findByUserId(1L)).thenReturn(List.of(holding1));

        RecalculationResult result = service.execute(ExecutionType.DOMINICAL);

        verify(holdingRepository).findByUserId(1L);
        verify(holdingRepository, never()).findByUserId(2L);
        assertEquals(1, result.recalculatedHoldings());
    }

    @Test
    void aplicaPendientesAntesDeRecalcular() {
        when(playerRepository.findAll()).thenReturn(List.of(player));

        StrategyConfigEntity oldActual = new StrategyConfigEntity(userWithStrategy, StrategyType.ESTRATEGIA_A, mapper.write(validWeightsA), StrategyStatus.ACTUAL, Instant.now());
        StrategyConfigEntity pending = new StrategyConfigEntity(userWithStrategy, StrategyType.ESTRATEGIA_B, mapper.write(validWeightsB), StrategyStatus.PENDIENTE, Instant.now());

        when(strategyConfigRepository.findAllByEstado(StrategyStatus.PENDIENTE)).thenReturn(List.of(pending));
        when(strategyConfigRepository.findFirstByUserIdAndEstado(1L, StrategyStatus.ACTUAL)).thenReturn(Optional.of(oldActual));
        when(strategyConfigRepository.findAllByEstado(StrategyStatus.ACTUAL)).thenReturn(List.of(pending));

        HoldingEntity holding = new HoldingEntity(userWithStrategy, player, 100);
        when(holdingRepository.findByUserId(1L)).thenReturn(List.of(holding));

        service.execute(ExecutionType.DOMINICAL);

        verify(strategyConfigRepository).delete(oldActual);
        verify(strategyConfigRepository).save(pending);
        assertEquals(StrategyStatus.ACTUAL, pending.getEstado());
    }
}
