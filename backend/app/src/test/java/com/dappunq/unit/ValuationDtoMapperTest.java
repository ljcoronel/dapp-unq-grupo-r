package com.dappunq.unit;

import com.dappunq.controller.ValuationDtoMapper;
import com.dappunq.dto.valuation.RecalculationResult;
import com.dappunq.model.valuation.RecalculationSummary;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ValuationDtoMapperTest {

    private final ValuationDtoMapper mapper = new ValuationDtoMapper();

    @Test
    void mapeaRecalculationSummaryARecalculationResult() {
        Instant now = Instant.now();
        RecalculationSummary summary = new RecalculationSummary("COMPLETED", 50, 10, now);

        RecalculationResult result = mapper.toResponse(summary);

        assertNotNull(result);
        assertEquals("COMPLETED", result.status());
        assertEquals(50, result.playersScraped());
        assertEquals(10, result.recalculatedHoldings());
        assertEquals(now, result.executedAt());
    }

    @Test
    void mapeaRecalculationSummaryNuloANulo() {
        assertNull(mapper.toResponse((RecalculationSummary) null));
    }
}
