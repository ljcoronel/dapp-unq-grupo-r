package com.dappunq.model.valuation;

import java.util.Set;

public enum Metric {
    goals, assists, passSuccessPercentage, aerialsWon, timesManOfTheMatch, minutesPlayed, appearances, position;

    public static Set<Metric> metricsForStrategyA() {
        return Set.of(goals, assists, passSuccessPercentage, aerialsWon, timesManOfTheMatch);
    }

    public static Set<Metric> metricsForStrategyB() {
        return Set.of(passSuccessPercentage, aerialsWon, minutesPlayed, appearances);
    }
}
