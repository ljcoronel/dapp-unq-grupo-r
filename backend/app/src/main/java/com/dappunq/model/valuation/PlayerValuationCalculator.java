package com.dappunq.model.valuation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PlayerValuationCalculator {

    public static final BigDecimal FACTOR_ESCALA = BigDecimal.valueOf(2);

    public NormalizationContext buildNormalizationContext(List<PlayerSnapshot> snapshot) {
        Map<Metric, NormalizationRange> ranges = new EnumMap<>(Metric.class);
        if (snapshot == null || snapshot.isEmpty()) {
            return new NormalizationContext(ranges);
        }

        for (Metric metric : Metric.values()) {
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            for (PlayerSnapshot player : snapshot) {
                double value = value(player, metric);
                min = Math.min(min, value);
                max = Math.max(max, value);
            }
            ranges.put(metric, new NormalizationRange(
                    metric,
                    BigDecimal.valueOf(min),
                    BigDecimal.valueOf(max)
            ));
        }
        return new NormalizationContext(ranges);
    }

    public BigDecimal calculate(PlayerSnapshot p, StrategyConfig c, NormalizationContext context,
                                BigDecimal valorBase) {
        BigDecimal base = valorBase == null ? BigDecimal.ZERO : valorBase;
        if (base.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValuationDomainException("El valor base no puede ser negativo");
        }
        return base.add(calculateRaw(p, c, context).multiply(FACTOR_ESCALA))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRaw(PlayerSnapshot p, StrategyConfig c, NormalizationContext context) {
        if (p == null || context == null || context.isEmpty() || c == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal result = BigDecimal.ZERO;
        for (var e : c.weights().entrySet()) {
            Metric m = e.getKey();
            NormalizationRange range = context.ranges().get(m);
            double n;
            if (isMissing(p, m) || range == null
                    || range.maximum().compareTo(range.minimum()) <= 0) {
                n = 0.0;
            } else {
                double minimum = range.minimum().doubleValue();
                double maximum = range.maximum().doubleValue();
                n = (value(p, m) - minimum) / (maximum - minimum);
                n = Math.max(0.0, Math.min(1.0, n));
            }
            BigDecimal weight = e.getValue().divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
            result = result.add(weight.multiply(BigDecimal.valueOf(n)));
        }

        if (c.tipo() == StrategyType.ESTRATEGIA_B && "Defender".equals(p.getPosition())) {
            result = result.add(BigDecimal.valueOf(0.05));
        }

        return result;
    }

    private boolean isMissing(PlayerSnapshot p, Metric m) {
        if (p == null) return true;
        return switch (m) {
            case goals -> p.getGoals() == null;
            case assists -> p.getAssists() == null;
            case passSuccessPercentage -> p.getPassSuccessPercentage() == null;
            case aerialsWon -> p.getAerialsWon() == null;
            case timesManOfTheMatch -> p.getTimesManOfTheMatch() == null;
            case minutesPlayed -> p.getMinutesPlayed() == null;
            case appearances -> p.getAppearances() == null;
            case position -> p.getPosition() == null;
        };
    }

    private double value(PlayerSnapshot p, Metric m) {
        if (p == null) return 0.0;
        return switch (m) {
            case goals -> nz(p.getGoals());
            case assists -> nz(p.getAssists());
            case passSuccessPercentage -> nz(p.getPassSuccessPercentage());
            case aerialsWon -> nz(p.getAerialsWon());
            case timesManOfTheMatch -> nz(p.getTimesManOfTheMatch());
            case minutesPlayed -> nz(p.getMinutesPlayed());
            case appearances -> nz(p.getAppearances());
            case position -> "Defender".equals(p.getPosition()) ? 1.0 : 0.0;
        };
    }

    private double nz(Number n) {
        return n == null ? 0.0 : n.doubleValue();
    }
}
