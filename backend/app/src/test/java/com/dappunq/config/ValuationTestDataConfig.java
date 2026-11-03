package com.dappunq.config;
import org.springframework.boot.test.context.TestConfiguration;
import java.math.BigDecimal;
import java.util.Map;

@TestConfiguration
public class ValuationTestDataConfig {
    public static final int TOKENS_INICIALES = 100;
    public static final BigDecimal VALOR_INICIAL = BigDecimal.ZERO.setScale(2);

    public static Map<String, BigDecimal> estrategiaA() {
        return Map.of("goals", BigDecimal.valueOf(30), "assists", BigDecimal.valueOf(25),
                "passSuccessPercentage", BigDecimal.valueOf(15), "aerialsWon", BigDecimal.valueOf(15),
                "timesManOfTheMatch", BigDecimal.valueOf(15));
    }

    public static Map<String, BigDecimal> estrategiaB() {
        return Map.of("passSuccessPercentage", BigDecimal.valueOf(35), "aerialsWon", BigDecimal.valueOf(20),
                "minutesPlayed", BigDecimal.valueOf(20), "appearances", BigDecimal.valueOf(20));
    }
}
