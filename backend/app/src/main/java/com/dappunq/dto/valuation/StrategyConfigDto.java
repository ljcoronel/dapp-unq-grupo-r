package com.dappunq.dto.valuation;

import com.dappunq.model.valuation.StrategyType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record StrategyConfigDto(
        StrategyType estrategia,
        Map<String, BigDecimal> pesos,
        Instant vigenteDesde
) {}
