package com.dappunq.controller;

import com.dappunq.dto.valuation.HoldingResponse;
import com.dappunq.dto.valuation.StrategyStateResponse;
import com.dappunq.persistence.HoldingEntity;
import com.dappunq.persistence.StrategyConfigEntity;

import java.math.BigDecimal;
import java.util.Map;

public class ValuationDtoMapper {

    public StrategyStateResponse strategy(StrategyConfigEntity e, Map<String, BigDecimal> weights) {
        return new StrategyStateResponse(e.getEstrategia(), weights, e.getEstado(), e.getVigenteDesde());
    }

    public HoldingResponse holding(HoldingEntity h) {
        return new HoldingResponse(
                h.getId(),
                h.getUser().getId(),
                h.getPlayer().getId(),
                h.getTokens(),
                h.getValorTotal(),
                h.getValorUnitario()
        );
    }
}
