package com.dappunq.model.valuation;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public record StrategyConfig(StrategyType tipo, Map<Metric, BigDecimal> weights) {

    public StrategyConfig {
        weights = Map.copyOf(weights);
        validate(tipo, weights);
    }

    public void validate() {
        validate(tipo, weights);
    }

    private static void validate(StrategyType strategy, Map<Metric, BigDecimal> weights) {
        Set<Metric> required = strategy == StrategyType.ESTRATEGIA_A
                ? Metric.metricsForStrategyA()
                : Metric.metricsForStrategyB();

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
