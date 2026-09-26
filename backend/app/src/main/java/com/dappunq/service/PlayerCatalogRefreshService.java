package com.dappunq.service;

import com.dappunq.config.LeagueCatalogProperties;
import com.dappunq.persistence.LeagueEntity;
import com.dappunq.persistence.LeagueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlayerCatalogRefreshService {
    private final FootballDataClient client;
    private final LeagueRepository leagueRepository;

    public PlayerCatalogRefreshService(FootballDataClient client, LeagueRepository leagueRepository) {
        this.client = client;
        this.leagueRepository = leagueRepository;
    }

    @Transactional
    public void refreshLeague(LeagueCatalogProperties.LeagueDefinition definition) {
        var payload = client.fetchScorers(definition.code());
        var players = PlayerMapper.toPlayers(payload.scorers()).stream()
                .limit(10)
                .toList();
        if (players.isEmpty()) {
            return;
        }

        LeagueEntity league = leagueRepository.findByCode(definition.code())
                .orElseGet(() -> new LeagueEntity(
                        definition.code(), definition.name(), definition.country()));
        league.replacePlayers(players.stream().map(PlayerMapper::toEntity).toList());
        leagueRepository.saveAndFlush(league);
    }

    public void refreshAll() {
        LeagueCatalogProperties.leagues().forEach(this::refreshLeague);
    }
}
