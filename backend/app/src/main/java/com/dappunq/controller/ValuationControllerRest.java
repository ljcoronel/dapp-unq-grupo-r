package com.dappunq.controller;

import com.dappunq.dto.valuation.ValuationResponse;
import com.dappunq.dto.valuation.HoldingResponse;
import com.dappunq.dto.valuation.StrategyRequest;
import com.dappunq.dto.valuation.StrategyStateResponse;
import com.dappunq.service.HoldingsValuationService;
import com.dappunq.service.StrategyConfigurationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users/{usuarioId}")
public class ValuationControllerRest {

    private final StrategyConfigurationService strategies;
    private final HoldingsValuationService holdings;
    private final ValuationDtoMapper mapper;

    public ValuationControllerRest(StrategyConfigurationService strategies,
                                  HoldingsValuationService holdings,
                                  ValuationDtoMapper mapper) {
        this.strategies = strategies;
        this.holdings = holdings;
        this.mapper = mapper;
    }

    @PutMapping("/strategy")
    public StrategyStateResponse put(@PathVariable Long usuarioId,
                                     @Valid @RequestBody StrategyRequest r) {
        return mapper.toResponse(strategies.configure(usuarioId, r.aModelo()));
    }

    @GetMapping("/strategy")
    public StrategyStateResponse get(@PathVariable Long usuarioId) {
        return mapper.toResponse(strategies.get(usuarioId));
    }

    @GetMapping("/holdings")
    public List<HoldingResponse> holdings(@PathVariable Long usuarioId) {
        return holdings.list(usuarioId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/holdings/history")
    public List<ValuationResponse> history(@PathVariable Long usuarioId) {
        return holdings.history(usuarioId).stream()
                .map(mapper::toResponse)
                .toList();
    }
}
