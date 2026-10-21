package com.dappunq.dto.valuation;

import com.dappunq.model.valuation.StrategyStatus;
import com.dappunq.model.valuation.StrategyType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StrategyStateResponse(
        StrategyType estrategia,
        Map<String, BigDecimal> pesos,
        StrategyStatus estado,
        Instant vigenteDesde,
        StrategyConfigDto actual,
        StrategyConfigDto pendiente
) {
    public StrategyStateResponse(StrategyType estrategia, Map<String, BigDecimal> pesos, StrategyStatus estado, Instant vigenteDesde) {
        this(
                estrategia,
                pesos,
                estado,
                vigenteDesde,
                estrategia != null ? new StrategyConfigDto(estrategia, pesos, vigenteDesde) : null,
                null
        );
    }

    public StrategyStateResponse(StrategyStatus estado, StrategyConfigDto actual, StrategyConfigDto pendiente) {
        this(
                actual != null ? actual.estrategia() : (pendiente != null ? pendiente.estrategia() : null),
                actual != null ? actual.pesos() : (pendiente != null ? pendiente.pesos() : null),
                estado,
                actual != null ? actual.vigenteDesde() : (pendiente != null ? pendiente.vigenteDesde() : null),
                actual,
                pendiente
        );
    }
}
