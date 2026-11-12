package com.dappunq.controller;

import com.dappunq.controller.mapper.ValuationDtoMapper;
import com.dappunq.dto.PlayerResponse;
import com.dappunq.dto.valuation.ValuationResponse;
import com.dappunq.service.PlayerService;
import com.dappunq.service.ValuationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/players")
public class PlayerControllerRest {

    private final PlayerService playerService;
    private final ValuationService valuationService;
    private final ValuationDtoMapper mapper;

    public PlayerControllerRest(PlayerService playerService, ValuationService valuationService, ValuationDtoMapper mapper) {
        this.playerService = playerService;
        this.valuationService = valuationService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<PlayerResponse> getPlayers() {
        return playerService.getAll();
    }

    @GetMapping("/{id}")
    public PlayerResponse getPlayer(@PathVariable Integer id) {
        return playerService.findPlayer(id);
    }

    @GetMapping("/{playerId}/quotes/{userId}")
    public List<ValuationResponse> getAllValuationsFromPlayer(@PathVariable Long playerId, @PathVariable Long userId) {
        return valuationService.getAllValuationsForPlayer(userId, playerId).stream()
            .map(mapper::toResponse)
            .toList();
    }

    @GetMapping("/{playerId}/quotes/{userId}/closest")
    public ValuationResponse getClosestValuationFromPlayer(@PathVariable Long playerId,
                                                           @PathVariable Long userId,
                                                           @RequestParam
                                                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                               Instant date) {
        return mapper.toResponse(valuationService.getClosestValuation(userId, playerId, date));
    }

    @GetMapping("/{playerId}/quotes/{userId}/current")
    public ValuationResponse getCurrentValuationFromPlayer(@PathVariable Long playerId, @PathVariable Long userId) {
        return mapper.toResponse(valuationService.getCurrentValuation(userId, playerId));
    }

}
