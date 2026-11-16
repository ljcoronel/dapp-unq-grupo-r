package com.dappunq.config;

import com.dappunq.model.valuation.Metric;
import org.springframework.boot.test.context.TestConfiguration;
import java.math.BigDecimal;
import java.util.Map;

@TestConfiguration
public class ValuationTestDataConfig {
    public static final int TOKENS_INICIALES = 100;
    public static final BigDecimal VALOR_INICIAL = BigDecimal.ZERO.setScale(2);

    public static Map<Metric, BigDecimal> estrategiaA() {
        return Map.of(Metric.goals, BigDecimal.valueOf(30), Metric.assists, BigDecimal.valueOf(25),
                Metric.passSuccessPercentage, BigDecimal.valueOf(15), Metric.aerialsWon, BigDecimal.valueOf(15),
                Metric.timesManOfTheMatch, BigDecimal.valueOf(15));
    }

    public static Map<Metric, BigDecimal> estrategiaB() {
        return Map.of(Metric.passSuccessPercentage, BigDecimal.valueOf(35), Metric.aerialsWon, BigDecimal.valueOf(20),
                Metric.minutesPlayed, BigDecimal.valueOf(20), Metric.appearances, BigDecimal.valueOf(20));
    }
}
