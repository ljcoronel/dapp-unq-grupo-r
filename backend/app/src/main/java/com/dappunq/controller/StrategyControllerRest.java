package com.dappunq.controller;

import com.dappunq.controller.mapper.ValuationDtoMapper;
import com.dappunq.dto.valuation.StrategyRequest;
import com.dappunq.dto.valuation.StrategyStateResponse;
import com.dappunq.service.StrategyConfigurationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/strategy/{usuarioId}")
public class StrategyControllerRest {

    private final StrategyConfigurationService strategyService;
    private final ValuationDtoMapper mapper;

    public StrategyControllerRest(StrategyConfigurationService service, ValuationDtoMapper mapper) {
        this.strategyService = service;
        this.mapper = mapper;
    }

    @PutMapping("/configure")
    public StrategyStateResponse configure(@PathVariable Long usuarioId,
                                           @Valid @RequestBody StrategyRequest r) {
        return mapper.toResponse(strategyService.configure(usuarioId, r.aModelo()));
    }

    @GetMapping
    public StrategyStateResponse get(@PathVariable Long usuarioId) {
        return mapper.toResponse(strategyService.get(usuarioId));
    }
}
