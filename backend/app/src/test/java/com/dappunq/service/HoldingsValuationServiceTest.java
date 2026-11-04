package com.dappunq.service;

import com.dappunq.model.valuation.ExecutionType;
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
    @Mock HoldingRepository holdings;
    @Mock ValuationHistoryRepository history;
    @Mock PlayerRepository players;
    @Mock StrategyConfigRepository configs;
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
                com.dappunq.model.valuation.StrategyType.ESTRATEGIA_A,
                mapper.write(Map.of("goals", BigDecimal.valueOf(30), "assists", BigDecimal.valueOf(25),
                        "passSuccessPercentage", BigDecimal.valueOf(15), "aerialsWon", BigDecimal.valueOf(15),
                        "timesManOfTheMatch", BigDecimal.valueOf(15))),
                com.dappunq.model.valuation.StrategyStatus.ACTUAL, Instant.now());
        when(players.findAll()).thenReturn(List.of(player));
        when(holdings.findByUserId(1L)).thenReturn(List.of(holding));

        int count = new HoldingsValuationService(holdings, history, players, configs, mapper)
                .revalue(1L, config, ExecutionType.MANUAL);

        assertEquals(1, count);
        assertEquals(BigDecimal.valueOf(3.00).setScale(2), holding.getValorUnitario());
        verify(holdings).save(holding);
        verify(history).save(any(ValuationHistoryEntity.class));
    }
}
