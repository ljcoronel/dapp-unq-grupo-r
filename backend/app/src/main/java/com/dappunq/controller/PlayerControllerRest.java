package com.dappunq.controller;

import com.dappunq.dto.PlayerResponse;
import com.dappunq.service.PlayerCatalogReadService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/players")
public class PlayerControllerRest {
    private final PlayerCatalogReadService readService;

    public PlayerControllerRest(PlayerCatalogReadService readService) {
        this.readService = readService;
    }

    @GetMapping
    // The outer list always contains the five configured leagues in catalog order.
    public List<List<PlayerResponse>> getPlayers() {
        return readService.findCatalog();
    }

    @GetMapping("/{id}")
    public PlayerResponse getPlayer(@org.springframework.web.bind.annotation.PathVariable Integer id) {
        return readService.findPlayer(id);
    }
}
