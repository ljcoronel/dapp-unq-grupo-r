package com.dappunq.controller;

import com.dappunq.dto.PlayerResponse;
import com.dappunq.service.PlayerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/players")
public class PlayerControllerRest {
    private final PlayerService readService;

    public PlayerControllerRest(PlayerService readService) {
        this.readService = readService;
    }

    @GetMapping
    public List<PlayerResponse> getPlayers() {
        return readService.findCatalog();
    }

    @GetMapping("/{id}")
    public PlayerResponse getPlayer(@org.springframework.web.bind.annotation.PathVariable Integer id) {
        return readService.findPlayer(id);
    }
}
