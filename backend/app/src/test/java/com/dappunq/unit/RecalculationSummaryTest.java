package com.dappunq.unit;

import com.dappunq.model.valuation.RecalculationSummary;
import com.dappunq.model.valuation.ValuationDomainException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RecalculationSummaryTest {

    @Test
    void creacionExitosaConValoresValidos() {
        Instant now = Instant.now();
        RecalculationSummary summary = new RecalculationSummary("COMPLETED", 50, 10, now);

        assertEquals("COMPLETED", summary.getStatus());
        assertEquals(50, summary.getPlayersScraped());
        assertEquals(10, summary.getRecalculatedHoldings());
        assertEquals(now, summary.getExecutedAt());

        // Metodos estilo record
        assertEquals("COMPLETED", summary.status());
        assertEquals(50, summary.playersScraped());
        assertEquals(10, summary.recalculatedHoldings());
        assertEquals(now, summary.executedAt());
    }

    @Test
    void rechazaEstadoNuloOVacio() {
        Instant now = Instant.now();
        assertThrows(ValuationDomainException.class, () -> new RecalculationSummary(null, 50, 10, now));
        assertThrows(ValuationDomainException.class, () -> new RecalculationSummary("  ", 50, 10, now));
    }

    @Test
    void rechazaContadoresNegativos() {
        Instant now = Instant.now();
        assertThrows(ValuationDomainException.class, () -> new RecalculationSummary("COMPLETED", -1, 10, now));
        assertThrows(ValuationDomainException.class, () -> new RecalculationSummary("COMPLETED", 50, -1, now));
    }

    @Test
    void rechazaFechaNula() {
        assertThrows(NullPointerException.class, () -> new RecalculationSummary("COMPLETED", 50, 10, null));
    }
}
