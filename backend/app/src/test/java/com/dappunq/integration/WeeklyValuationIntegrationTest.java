package com.dappunq.integration;

import com.dappunq.dto.valuation.RecalculationResult;
import com.dappunq.model.valuation.ExecutionType;
import com.dappunq.model.valuation.StrategyStatus;
import com.dappunq.model.valuation.StrategyType;
import com.dappunq.persistence.HoldingEntity;
import com.dappunq.persistence.HoldingRepository;
import com.dappunq.persistence.JpaUserEntityRepository;
import com.dappunq.persistence.LeagueEntity;
import com.dappunq.persistence.LeagueRepository;
import com.dappunq.persistence.PlayerEntity;
import com.dappunq.persistence.PlayerRepository;
import com.dappunq.persistence.StrategyConfigEntity;
import com.dappunq.persistence.StrategyConfigRepository;
import com.dappunq.persistence.TeamEntity;
import com.dappunq.persistence.TeamRepository;
import com.dappunq.persistence.UserEntity;
import com.dappunq.persistence.ValuationEntity;
import com.dappunq.persistence.ValuationHistoryEntity;
import com.dappunq.persistence.ValuationHistoryRepository;
import com.dappunq.persistence.ValuationPersistenceMapper;
import com.dappunq.persistence.ValuationRepository;
import com.dappunq.service.WeeklyValuationService;
import com.dappunq.service.WhoScoredScrapingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
class WeeklyValuationIntegrationTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        postgres.start();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @MockitoBean
    private WhoScoredScrapingService scrapingService;

    @Autowired
    private WeeklyValuationService weeklyValuationService;

    @Autowired
    private JpaUserEntityRepository userRepository;

    @Autowired
    private StrategyConfigRepository strategyConfigRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private ValuationRepository valuationRepository;

    @Autowired
    private ValuationHistoryRepository valuationHistoryRepository;

    @Autowired
    private ValuationPersistenceMapper mapper;

    private UserEntity user1;
    private UserEntity user2;
    private UserEntity userWithoutStrategy;
    private PlayerEntity player;

    @BeforeEach
    void setUp() {
        valuationHistoryRepository.deleteAll();
        valuationRepository.deleteAll();
        holdingRepository.deleteAll();
        strategyConfigRepository.deleteAll();
        playerRepository.deleteAll();
        teamRepository.deleteAll();
        leagueRepository.deleteAll();
        userRepository.deleteAll();

        doNothing().when(scrapingService).scrape(anyString());

        user1 = userRepository.save(new UserEntity("user1", "hash1"));
        user2 = userRepository.save(new UserEntity("user2", "hash2"));
        userWithoutStrategy = userRepository.save(new UserEntity("user_no_strat", "hash3"));

        TeamEntity team = teamRepository.save(new TeamEntity(1L, "Team A"));
        LeagueEntity league = leagueRepository.save(new LeagueEntity(1L, "League A", "Region A"));
        player = playerRepository.save(new PlayerEntity(10L, "Player 1", team, league, 25, 180, 75, "Defender", 10, 900, 2, 1, 0, 0, 1.5f, 85f, 3f, 1));

        Map<String, BigDecimal> weightsA = new HashMap<>();
        weightsA.put("goals", BigDecimal.valueOf(30));
        weightsA.put("assists", BigDecimal.valueOf(25));
        weightsA.put("passSuccessPercentage", BigDecimal.valueOf(15));
        weightsA.put("aerialsWon", BigDecimal.valueOf(15));
        weightsA.put("timesManOfTheMatch", BigDecimal.valueOf(15));

        Map<String, BigDecimal> weightsB = new HashMap<>();
        weightsB.put("passSuccessPercentage", BigDecimal.valueOf(35));
        weightsB.put("aerialsWon", BigDecimal.valueOf(20));
        weightsB.put("minutesPlayed", BigDecimal.valueOf(20));
        weightsB.put("appearances", BigDecimal.valueOf(20));

        // User 1 tiene Estrategia A ACTUAL
        strategyConfigRepository.save(new StrategyConfigEntity(user1, StrategyType.ESTRATEGIA_A, mapper.write(weightsA), StrategyStatus.ACTUAL, Instant.now()));

        // User 2 tiene Estrategia A ACTUAL y Estrategia B PENDIENTE
        strategyConfigRepository.save(new StrategyConfigEntity(user2, StrategyType.ESTRATEGIA_A, mapper.write(weightsA), StrategyStatus.ACTUAL, Instant.now()));
        strategyConfigRepository.save(new StrategyConfigEntity(user2, StrategyType.ESTRATEGIA_B, mapper.write(weightsB), StrategyStatus.PENDIENTE, Instant.now()));

        // Holdings
        holdingRepository.save(new HoldingEntity(user1, player, 100));
        holdingRepository.save(new HoldingEntity(user2, player, 100));
        holdingRepository.save(new HoldingEntity(userWithoutStrategy, player, 100));
    }

    @Test
    void ejecucionValuacionSemanalAplicaPendientesCreaSnapshotsYEsIdempotente() {
        RecalculationResult firstResult = weeklyValuationService.execute(ExecutionType.DOMINICAL);
        assertEquals("COMPLETED", firstResult.status());
        assertEquals(2, firstResult.recalculatedHoldings());

        // Verificar que user2 ahora tiene Estrategia B como ACTUAL y 0 PENDIENTE
        StrategyConfigEntity user2Current = strategyConfigRepository.findFirstByUserIdAndEstado(user2.getId(), StrategyStatus.ACTUAL).orElseThrow();
        assertEquals(StrategyType.ESTRATEGIA_B, user2Current.getEstrategia());
        assertTrue(strategyConfigRepository.findFirstByUserIdAndEstado(user2.getId(), StrategyStatus.PENDIENTE).isEmpty());

        // Verificar historial
        List<ValuationHistoryEntity> historyUser1 = valuationHistoryRepository.findByUserIdOrderByEjecutadoEnDesc(user1.getId());
        assertEquals(1, historyUser1.size());
        assertEquals(StrategyType.ESTRATEGIA_A, historyUser1.get(0).getEstrategia());
        assertEquals(100, historyUser1.get(0).getTokens());
        assertNotNull(historyUser1.get(0).getValorUnitario());

        List<ValuationHistoryEntity> historyUser2 = valuationHistoryRepository.findByUserIdOrderByEjecutadoEnDesc(user2.getId());
        assertEquals(1, historyUser2.size());
        assertEquals(StrategyType.ESTRATEGIA_B, historyUser2.get(0).getEstrategia());

        // User sin estrategia no debe tener historial
        List<ValuationHistoryEntity> historyNoStrat = valuationHistoryRepository.findByUserIdOrderByEjecutadoEnDesc(userWithoutStrategy.getId());
        assertEquals(0, historyNoStrat.size());

        // Idempotencia: reejecutar en la misma ventana no debe duplicar historial ni valuaciones
        RecalculationResult secondResult = weeklyValuationService.execute(ExecutionType.DOMINICAL);
        assertEquals("COMPLETED", secondResult.status());

        List<ValuationHistoryEntity> totalHistory = valuationHistoryRepository.findAll();
        assertEquals(2, totalHistory.size());

        List<ValuationEntity> totalValuations = valuationRepository.findAll();
        assertEquals(2, totalValuations.size());
    }
}
