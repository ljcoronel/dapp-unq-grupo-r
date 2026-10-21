package com.dappunq.model.valuation;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public record StrategyConfig(StrategyType strategy, Map<String, BigDecimal> weights) {

    public StrategyConfig {
        weights = Map.copyOf(weights);
        validate(strategy, weights);
    }

    public void validate() {
        validate(strategy, weights);
    }

    private static void validate(StrategyType strategy, Map<String, BigDecimal> weights) {
        Set<String> required = strategy == StrategyType.ESTRATEGIA_A
                ? Set.of("goals", "assists", "passSuccessPercentage", "aerialsWon", "timesManOfTheMatch")
                : Set.of("passSuccessPercentage", "aerialsWon", "minutesPlayed", "appearances");

        if (!weights.keySet().equals(required)) {
            throw ValuationDomainException.invalidWeights("metricas requeridas incompletas");
        }

        BigDecimal sum = weights.values().stream()
                .peek(v -> {
                    if (v == null || v.signum() < 0) {
                        throw ValuationDomainException.invalidWeights("no negativos");
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal target = strategy == StrategyType.ESTRATEGIA_B
                ? BigDecimal.valueOf(95)
                : BigDecimal.valueOf(100);

        if (sum.compareTo(target) != 0) {
            throw ValuationDomainException.invalidWeights("la suma debe ser " + target);
        }
    }
}
