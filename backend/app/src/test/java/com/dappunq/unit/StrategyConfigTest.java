package com.dappunq.unit;

import com.dappunq.model.valuation.*;
import org.junit.jupiter.api.Test;
import java.math.*; import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class StrategyConfigTest {

    @Test
    void validaEstrategias() {
        var m = new HashMap<Metric,BigDecimal>();
        Map<Metric, BigDecimal> pesoInvalido = Map.of(Metric.goals, BigDecimal.ONE);
        for (Metric x : Metric.metricsForStrategyA())
            m.put(x, BigDecimal.valueOf(20));
        assertDoesNotThrow(() -> new StrategyConfig(StrategyType.ESTRATEGIA_A, m));
        assertThrows(ValuationDomainException.class,() -> new StrategyConfig(StrategyType.ESTRATEGIA_A, pesoInvalido));
    }
}
