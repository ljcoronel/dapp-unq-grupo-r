package com.dappunq.dto.valuation;

import com.dappunq.model.valuation.StrategyConfig;
import com.dappunq.model.valuation.Metric;
import com.dappunq.model.valuation.StrategyType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Map;

public record StrategyRequest(
        @NotNull StrategyType tipo,
        @NotEmpty Map<Metric, @NotNull @DecimalMin("0") BigDecimal> pesos
) {

    public StrategyConfig aModelo() {
        return new StrategyConfig(tipo, pesos);
    }
}
