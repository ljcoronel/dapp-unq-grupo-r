package com.dappunq.model.valuation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerValuationCalculator {

    public static final BigDecimal FACTOR_ESCALA = BigDecimal.valueOf(2);

    public BigDecimal calculate(PlayerSnapshot p, StrategyConfig c, List<PlayerSnapshot> snapshot,
                                BigDecimal valorBase) {
        BigDecimal base = valorBase == null ? BigDecimal.ZERO : valorBase;
        if (base.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValuationDomainException("El valor base no puede ser negativo");
        }
        return base.add(calculateRaw(p, c, snapshot).multiply(FACTOR_ESCALA))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRaw(PlayerSnapshot p, StrategyConfig c, List<PlayerSnapshot> snapshot) {
        if (p == null || snapshot == null || snapshot.isEmpty() || c == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        // TODO: la normalización debe hacerse una única vez cuando se haga el cálculo dominical o manual y no en cada cálculo de valuación
        Map<String, double[]> ranges = new HashMap<>();
        for (String m : c.weights().keySet()) {
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            for (PlayerSnapshot x : snapshot) {
                double v = value(x, m);
                min = Math.min(min, v);
                max = Math.max(max, v);
            }
            ranges.put(m, new double[]{min, max});
        }

        BigDecimal result = BigDecimal.ZERO;
        for (var e : c.weights().entrySet()) {
            String m = e.getKey();
            double[] r = ranges.get(m);
            double n;
            if (isMissing(p, m) || r == null || r[1] <= r[0]) {
                n = 0.0;
            } else {
                n = (value(p, m) - r[0]) / (r[1] - r[0]);
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

    private boolean isMissing(PlayerSnapshot p, String m) {
        if (p == null) return true;
        return switch (m) {
            case "goals" -> p.getGoals() == null;
            case "assists" -> p.getAssists() == null;
            case "passSuccessPercentage" -> p.getPassSuccessPercentage() == null;
            case "aerialsWon" -> p.getAerialsWon() == null;
            case "timesManOfTheMatch" -> p.getTimesManOfTheMatch() == null;
            case "minutesPlayed" -> p.getMinutesPlayed() == null;
            case "appearances" -> p.getAppearances() == null;
            case "position" -> p.getPosition() == null;
            default -> true;
        };
    }

    private double value(PlayerSnapshot p, String m) {
        if (p == null) return 0.0;
        return switch (m) {
            case "goals" -> nz(p.getGoals());
            case "assists" -> nz(p.getAssists());
            case "passSuccessPercentage" -> nz(p.getPassSuccessPercentage());
            case "aerialsWon" -> nz(p.getAerialsWon());
            case "timesManOfTheMatch" -> nz(p.getTimesManOfTheMatch());
            case "minutesPlayed" -> nz(p.getMinutesPlayed());
            case "appearances" -> nz(p.getAppearances());
            case "position" -> "Defender".equals(p.getPosition()) ? 1.0 : 0.0;
            default -> 0.0;
        };
    }

    private double nz(Number n) {
        return n == null ? 0.0 : n.doubleValue();
    }
}
