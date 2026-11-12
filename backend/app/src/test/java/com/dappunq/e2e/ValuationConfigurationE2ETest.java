package com.dappunq.e2e;

import com.dappunq.controller.HoldingControllerRest;
import com.dappunq.controller.mapper.ValuationDtoMapper;
import com.dappunq.controller.QuoteControllerRest;
import com.dappunq.model.valuation.Holding;
import com.dappunq.model.valuation.RecalculationSummary;
import com.dappunq.model.valuation.ExecutionType;
import com.dappunq.service.HoldingsValuationService;
import com.dappunq.service.ScrapingService;
import com.dappunq.service.StrategyConfigurationServiceImpl;
import com.dappunq.service.WeeklyValuationService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ValuationConfigurationE2ETest {
    @Test
    void holdingsEndpointExponeElValorUnitarioAcumulado() throws Exception {
        HoldingsValuationService holdings = mock(HoldingsValuationService.class);
        StrategyConfigurationServiceImpl strategies = mock(StrategyConfigurationServiceImpl.class);
        when(holdings.list(7L)).thenReturn(List.of(
                new Holding(7L, 3L, 100, BigDecimal.valueOf(4))));

        MockMvc mockMvc = standaloneSetup(new HoldingControllerRest(
            holdings, new ValuationDtoMapper())).build();

        mockMvc.perform(get("/users/7/holdings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tokens").value(100))
                .andExpect(jsonPath("$[0].valorUnitario").value(4.0))
                .andExpect(jsonPath("$[0].valorTotal").value(400.0));
    }

    @Test
    void recalculoManualYConsultaDeHoldingsReflejanValorAcumulado() throws Exception {
        HoldingsValuationService holdings = mock(HoldingsValuationService.class);
        ScrapingService scraping = mock(ScrapingService.class);
        StrategyConfigurationServiceImpl strategies = mock(StrategyConfigurationServiceImpl.class);
        WeeklyValuationService weekly = mock(WeeklyValuationService.class);
        when(weekly.execute(ExecutionType.MANUAL))
                .thenReturn(new RecalculationSummary("OK", 1, 1, Instant.parse("2026-01-01T00:00:00Z")));
        when(holdings.list(7L)).thenReturn(List.of(
                new Holding(7L, 3L, 100, new BigDecimal("4.00"))));

        ValuationDtoMapper mapper = new ValuationDtoMapper();
        MockMvc mockMvc = standaloneSetup(
                new QuoteControllerRest(weekly, scraping, mapper),
                new HoldingControllerRest(holdings, mapper)).build();

        mockMvc.perform(post("/quotes/recalculate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.recalculatedHoldings").value(1));
        mockMvc.perform(get("/users/7/holdings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].valorUnitario").value(4.0))
                .andExpect(jsonPath("$[0].valorTotal").value(400.0));
    }
}
