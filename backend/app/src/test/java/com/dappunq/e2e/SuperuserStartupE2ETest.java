package com.dappunq.e2e;

import com.dappunq.model.valuation.Metric;
import com.dappunq.controller.HoldingControllerRest;
import com.dappunq.controller.StrategyControllerRest;
import com.dappunq.controller.mapper.ValuationDtoMapper;
import com.dappunq.model.valuation.Holding;
import com.dappunq.model.valuation.StrategyStatus;
import com.dappunq.model.valuation.StrategyState;
import com.dappunq.model.valuation.StrategyType;
import com.dappunq.service.HoldingsValuationService;
import com.dappunq.service.StrategyConfigurationServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class SuperuserStartupE2ETest {

    @Test
    void startupExponeEstrategiaActualYTenenciasValuadas() throws Exception {
        StrategyConfigurationServiceImpl strategies = mock(StrategyConfigurationServiceImpl.class);
        HoldingsValuationService holdings = mock(HoldingsValuationService.class);
        Map<Metric, BigDecimal> weights = Map.of(
                Metric.goals, BigDecimal.valueOf(30),
                Metric.assists, BigDecimal.valueOf(25),
                Metric.passSuccessPercentage, BigDecimal.valueOf(15),
                Metric.aerialsWon, BigDecimal.valueOf(15),
                Metric.timesManOfTheMatch, BigDecimal.valueOf(15));
        when(strategies.get(1L)).thenReturn(new StrategyState(
                StrategyStatus.ACTUAL,
                StrategyType.ESTRATEGIA_A,
                weights,
                Instant.parse("2026-01-01T00:00:00Z"),
                null,
                null,
                null,
                null));
        when(holdings.list(1L)).thenReturn(List.of(
                new Holding(1L, 8L, 100, new BigDecimal("2.00"))));

        MockMvc mockMvc = standaloneSetup(new HoldingControllerRest(
            holdings, new ValuationDtoMapper())).build();

        mockMvc.perform(get("/users/1/holdings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].valorUnitario").value(2.0))
                .andExpect(jsonPath("$[0].valorTotal").value(200.0));

        MockMvc mockMvc2 = standaloneSetup(new StrategyControllerRest(
            strategies, new ValuationDtoMapper())).build();
        mockMvc2.perform(get("/strategy/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("ACTUAL"));
    }
}
