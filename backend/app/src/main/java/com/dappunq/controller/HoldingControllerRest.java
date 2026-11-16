package com.dappunq.controller;

import com.dappunq.controller.mapper.ValuationDtoMapper;
import com.dappunq.dto.valuation.HoldingResponse;
import com.dappunq.service.HoldingsValuationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users/{usuarioId}")
public class HoldingControllerRest {

    private final HoldingsValuationService holdings;
    private final ValuationDtoMapper mapper;

    public HoldingControllerRest(HoldingsValuationService holdings, ValuationDtoMapper mapper) {
        this.holdings = holdings;
        this.mapper = mapper;
    }

    @GetMapping("/holdings")
    public List<HoldingResponse> holdings(@PathVariable Long usuarioId) {
        return holdings.list(usuarioId).stream()
                .map(mapper::toResponse)
                .toList();
    }

}
