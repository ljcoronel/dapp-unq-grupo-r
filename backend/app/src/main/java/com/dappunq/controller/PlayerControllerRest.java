package com.dappunq.controller;

import com.dappunq.controller.mapper.ValuationDtoMapper;
import com.dappunq.controller.mapper.RankingDtoMapper;
import com.dappunq.dto.PlayerResponse;
import com.dappunq.dto.valuation.ValuationResponse;
import com.dappunq.dto.ranking.RankingPageResponse;
import com.dappunq.model.ranking.RankingFiltros;
import com.dappunq.service.PlayerService;
import com.dappunq.service.RankingService;
import com.dappunq.service.ValuationService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final RankingService rankingService;
    private final RankingDtoMapper rankingMapper;

    public PlayerControllerRest(PlayerService playerService, ValuationService valuationService, ValuationDtoMapper mapper) {
        this.playerService = playerService;
        this.valuationService = valuationService;
        this.mapper = mapper;
        this.rankingService = null;
        this.rankingMapper = null;
    }

    @Autowired
    public PlayerControllerRest(PlayerService playerService, ValuationService valuationService, ValuationDtoMapper mapper,
                                RankingService rankingService, RankingDtoMapper rankingMapper) {
        this.playerService = playerService;
        this.valuationService = valuationService;
        this.mapper = mapper;
        this.rankingService = rankingService;
        this.rankingMapper = rankingMapper;
    }

    @GetMapping
    public List<PlayerResponse> getPlayers() {
        return playerService.getAll();
    }

    @GetMapping("/ranking")
    public RankingPageResponse getRanking(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) String league,
            @RequestParam(required = false) String team,
            @RequestParam(required = false) String position) {
        if (rankingService == null || rankingMapper == null) {
            throw new IllegalStateException("El servicio de ranking no esta disponible");
        }
        var filtros = new RankingFiltros(league, team, position, page, limit);
        return rankingMapper.toResponse(rankingService.consultar(filtros));
    }

    @GetMapping("/{id}")
    public PlayerResponse getPlayer(@PathVariable Long id) {
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
