package com.dappunq.controller;

import com.dappunq.dto.valuation.HoldingHistoryResponse;
import com.dappunq.dto.valuation.HoldingResponse;
import com.dappunq.dto.valuation.StrategyRequest;
import com.dappunq.dto.valuation.StrategyStateResponse;
import com.dappunq.model.valuation.StrategyStatus;
import com.dappunq.service.HoldingsValuationService;
import com.dappunq.service.StrategyConfigurationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users/{usuarioId}")
public class ValuationControllerRest {

    private final StrategyConfigurationService strategies;
    private final HoldingsValuationService holdings;

    public ValuationControllerRest(StrategyConfigurationService strategies, HoldingsValuationService holdings) {
        this.strategies = strategies;
        this.holdings = holdings;
    }

    @PutMapping("/strategy")
    public StrategyStateResponse put(@PathVariable Long usuarioId,
                                     @RequestParam(required = false) StrategyStatus estado,
                                     @Valid @RequestBody StrategyRequest r) {
        StrategyStatus targetStatus = (estado != null) ? estado : r.estado();
        if (targetStatus != null && targetStatus != r.estado()) {
            r = new StrategyRequest(r.estrategia(), r.pesos(), targetStatus);
        }
        return strategies.configure(usuarioId, r);
    }

    @GetMapping("/strategy")
    public StrategyStateResponse get(@PathVariable Long usuarioId) {
        return strategies.get(usuarioId);
    }

    @GetMapping("/holdings")
    public List<HoldingResponse> holdings(@PathVariable Long usuarioId) {
        return holdings.list(usuarioId);
    }

    @GetMapping("/holdings/history")
    public List<HoldingHistoryResponse> history(@PathVariable Long usuarioId) {
        return holdings.history(usuarioId);
    }
}
