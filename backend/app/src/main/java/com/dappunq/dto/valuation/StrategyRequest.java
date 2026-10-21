package com.dappunq.dto.valuation;

import com.dappunq.model.valuation.StrategyStatus;
import com.dappunq.model.valuation.StrategyType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Map;

public record StrategyRequest(
        @NotNull StrategyType estrategia,
        @NotEmpty Map<String, @NotNull @DecimalMin("0") BigDecimal> pesos,
        StrategyStatus estado
) {
    public StrategyRequest(@NotNull StrategyType estrategia, @NotEmpty Map<String, @NotNull @DecimalMin("0") BigDecimal> pesos) {
        this(estrategia, pesos, null);
    }
}
