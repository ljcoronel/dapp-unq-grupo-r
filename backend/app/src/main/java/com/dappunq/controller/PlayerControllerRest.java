package com.dappunq.controller;

import com.dappunq.controller.mapper.ValuationDtoMapper;
import com.dappunq.dto.PlayerResponse;
import com.dappunq.dto.valuation.ValuationResponse;
import com.dappunq.service.HoldingsValuationService;
import com.dappunq.service.PlayerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/players")
public class PlayerControllerRest {

    private final PlayerService playerService;
    private final HoldingsValuationService holdings;
    private final ValuationDtoMapper mapper;

    public PlayerControllerRest(PlayerService playerService, HoldingsValuationService holdings, ValuationDtoMapper mapper) {
        this.playerService = playerService;
        this.holdings = holdings;
        this.mapper = mapper;
    }

    @GetMapping
    public List<PlayerResponse> getPlayers() {
        return playerService.findCatalog();
    }

    @GetMapping("/{id}")
    public PlayerResponse getPlayer(@PathVariable Integer id) {
        return playerService.findPlayer(id);
    }

    @GetMapping("/{playerId}/quotes/{userId}")
    public List<ValuationResponse> history(@PathVariable Long playerId, @PathVariable Long userId) {
        return holdings.history(userId).stream()
            .map(mapper::toResponse)
            .toList();
    }
}
