package com.dappunq.service;

import com.dappunq.model.valuation.Metric;
import com.dappunq.model.valuation.ExecutionType;
import com.dappunq.model.valuation.StrategyStatus;
import com.dappunq.model.valuation.StrategyType;
import com.dappunq.persistence.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HoldingsValuationServiceTest {

    @Mock
    HoldingRepository holdings;

    @Mock
    ValuationRepository history;

    @Mock
    PlayerRepository players;

    @Mock
    StrategyConfigRepository configs;

    @Mock
    NormalizationRangeRepository normalizationRangeRepository;

    private final ValuationPersistenceMapper mapper = new ValuationPersistenceMapper();

    @Test
    void revalueAcumulaSobreValorUnitarioPersistidoYRegistraHistorial() {
        UserEntity user = new UserEntity(1L, "user", "hash");
        TeamEntity team = new TeamEntity(1L, "Team");
        LeagueEntity league = new LeagueEntity(1L, "League", "Region");
        PlayerEntity player = new PlayerEntity(1L, "Player", team, league, 10, 180, 75,
                "Forward", 0, 100, 0, 0, 0, 0, 0f, 50f, 1f, 0);
        HoldingEntity holding = new HoldingEntity(user, player, 100, BigDecimal.valueOf(3));
        StrategyConfigEntity config = new StrategyConfigEntity(user,
                StrategyType.ESTRATEGIA_A,
                mapper.write(Map.of(Metric.goals, BigDecimal.valueOf(30), Metric.assists, BigDecimal.valueOf(25),
                        Metric.passSuccessPercentage, BigDecimal.valueOf(15), Metric.aerialsWon, BigDecimal.valueOf(15),
                        Metric.timesManOfTheMatch, BigDecimal.valueOf(15))),
                StrategyStatus.ACTUAL, Instant.now());
        when(holdings.findByUserId(1L)).thenReturn(List.of(holding));

        when(normalizationRangeRepository.findCurrentRanges()).thenReturn(List.of(
                new MetricNormalizationEntity(Metric.goals, BigDecimal.ZERO, BigDecimal.ZERO, Instant.now()),
                new MetricNormalizationEntity(Metric.assists, BigDecimal.ZERO, BigDecimal.ZERO, Instant.now()),
                new MetricNormalizationEntity(Metric.passSuccessPercentage, BigDecimal.valueOf(50), BigDecimal.valueOf(50), Instant.now()),
                new MetricNormalizationEntity(Metric.aerialsWon, BigDecimal.ONE, BigDecimal.ONE, Instant.now()),
                new MetricNormalizationEntity(Metric.timesManOfTheMatch, BigDecimal.ZERO, BigDecimal.ZERO, Instant.now())
        ));

        int count = new HoldingsValuationService(holdings, history, players, configs, mapper,
                        normalizationRangeRepository)
                .revalue(1L, config, ExecutionType.MANUAL);

        assertEquals(1, count);
        assertEquals(BigDecimal.valueOf(3.00).setScale(2), holding.getValorUnitario());
        verify(holdings).save(holding);
        verify(history).save(any(ValuationEntity.class));
    }
}
