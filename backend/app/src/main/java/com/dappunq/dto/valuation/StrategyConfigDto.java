package com.dappunq.dto.valuation;

import com.dappunq.model.valuation.StrategyType;
import com.dappunq.model.valuation.Metric;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record StrategyConfigDto(
        StrategyType estrategia,
        Map<Metric, BigDecimal> pesos,
        Instant vigenteDesde
) {}
