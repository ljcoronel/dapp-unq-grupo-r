package com.dappunq.integration;

import com.dappunq.model.valuation.Metric;
import com.dappunq.model.valuation.ExecutionType;
import com.dappunq.model.valuation.RecalculationSummary;
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
import com.dappunq.persistence.ValuationRepository;
import com.dappunq.persistence.ValuationPersistenceMapper;
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
    private ValuationPersistenceMapper mapper;

    private UserEntity user1;
    private UserEntity user2;
    private UserEntity userWithoutStrategy;
    private PlayerEntity player;

    @BeforeEach
    void setUp() {
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
        playerRepository.save(new PlayerEntity(11L, "Player 2", team, league, 0, 180, 75, "Forward", 0, 100, 0, 0, 0, 0, 0f, 50f, 1f, 0));

        Map<Metric, BigDecimal> weightsA = new HashMap<>();
        weightsA.put(Metric.goals, BigDecimal.valueOf(30));
        weightsA.put(Metric.assists, BigDecimal.valueOf(25));
        weightsA.put(Metric.passSuccessPercentage, BigDecimal.valueOf(15));
        weightsA.put(Metric.aerialsWon, BigDecimal.valueOf(15));
        weightsA.put(Metric.timesManOfTheMatch, BigDecimal.valueOf(15));

        Map<Metric, BigDecimal> weightsB = new HashMap<>();
        weightsB.put(Metric.passSuccessPercentage, BigDecimal.valueOf(35));
        weightsB.put(Metric.aerialsWon, BigDecimal.valueOf(20));
        weightsB.put(Metric.minutesPlayed, BigDecimal.valueOf(20));
        weightsB.put(Metric.appearances, BigDecimal.valueOf(20));

        // User 1 tiene Estrategia A ACTUAL
        strategyConfigRepository.save(new StrategyConfigEntity(user1, StrategyType.ESTRATEGIA_A, mapper.write(weightsA), StrategyStatus.ACTUAL, Instant.now()));

        // User 2 tiene Estrategia A ACTUAL y Estrategia B PENDIENTE
        strategyConfigRepository.save(new StrategyConfigEntity(user2, StrategyType.ESTRATEGIA_A, mapper.write(weightsA), StrategyStatus.ACTUAL, Instant.now()));
        strategyConfigRepository.save(new StrategyConfigEntity(user2, StrategyType.ESTRATEGIA_B, mapper.write(weightsB), StrategyStatus.PENDIENTE, Instant.now()));

        // Holdings
        // TODO: no debería permitirse que haya más de 100 tokens por jugador. Usar Service
        holdingRepository.save(new HoldingEntity(user1, player, 100));
        holdingRepository.save(new HoldingEntity(user2, player, 100));
        // TODO: no debería permitirse que un usuario tenga un Holding sin Estrategia configurada. Usar Service
        holdingRepository.save(new HoldingEntity(userWithoutStrategy, player, 100));

        // Valuation
        valuationRepository.save(new ValuationEntity(user1, player, 100, BigDecimal.ONE, StrategyType.ESTRATEGIA_A, null, null));
        valuationRepository.save(new ValuationEntity(user2, player, 100, BigDecimal.ONE, StrategyType.ESTRATEGIA_B, null, null));
    }

    @Test
    void ejecucionValuacionSemanalAplicaPendientesCreaSnapshotsYEsIdempotente() {
        RecalculationSummary firstResult = weeklyValuationService.execute(ExecutionType.DOMINICAL);
        assertEquals("COMPLETED", firstResult.status());
        assertEquals(2, firstResult.recalculatedHoldings());
        BigDecimal firstUserValue = holdingRepository.findByUserId(user1.getId()).get(0).getValorUnitario();
        assertTrue(firstUserValue.compareTo(BigDecimal.ZERO) > 0);

        // Verificar que user2 ahora tiene Estrategia B como ACTUAL y 0 PENDIENTE
        StrategyConfigEntity user2Current = strategyConfigRepository.findFirstByUserIdAndEstado(user2.getId(), StrategyStatus.ACTUAL).orElseThrow();
        assertEquals(StrategyType.ESTRATEGIA_B, user2Current.getTipoEstrategia());
        assertTrue(strategyConfigRepository.findFirstByUserIdAndEstado(user2.getId(), StrategyStatus.PENDIENTE).isEmpty());

        // Verificar historial
        List<ValuationEntity> historyUser1 = valuationRepository.findHistoricalByUser(user1.getId());
        assertEquals(1, historyUser1.size());
        assertEquals(StrategyType.ESTRATEGIA_A, historyUser1.get(0).getEstrategia());
        assertEquals(100, historyUser1.get(0).getTokens());
        assertNotNull(historyUser1.get(0).getValorUnitario());

        List<ValuationEntity> historyUser2 = valuationRepository.findHistoricalByUser(user2.getId());
        assertEquals(1, historyUser2.size());
        assertEquals(StrategyType.ESTRATEGIA_B, historyUser2.get(0).getEstrategia());

        // User sin estrategia no debe tener historial
        List<ValuationEntity> historyNoStrat = valuationRepository.findHistoricalByUser(userWithoutStrategy.getId());
        assertEquals(0, historyNoStrat.size());

        // Idempotencia: reejecutar en la misma ventana no debe duplicar historial ni valuaciones
        RecalculationSummary secondResult = weeklyValuationService.execute(ExecutionType.DOMINICAL);
        assertEquals("COMPLETED", secondResult.status());
        BigDecimal secondUserValue = holdingRepository.findByUserId(user1.getId()).get(0).getValorUnitario();
        assertTrue(secondUserValue.compareTo(firstUserValue) > 0);

        List<ValuationEntity> totalHistory = valuationRepository.findAllHistorical();
        assertEquals(4, totalHistory.size());

        List<ValuationEntity> totalValuations = valuationRepository.findAllCurrent();
        assertEquals(2, totalValuations.size());
    }
}
