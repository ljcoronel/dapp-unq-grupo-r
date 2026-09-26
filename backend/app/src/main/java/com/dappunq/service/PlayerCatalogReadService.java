package com.dappunq.service;

import com.dappunq.config.LeagueCatalogProperties;
import com.dappunq.dto.PlayerResponse;
import com.dappunq.exception.CatalogUnavailableException;
import com.dappunq.exception.InvalidPlayerIdException;
import com.dappunq.exception.PlayerNotFoundException;
import com.dappunq.persistence.LeagueRepository;
import com.dappunq.persistence.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlayerCatalogReadService {
    private final LeagueRepository leagueRepository;
    private final PlayerRepository playerRepository;

    public PlayerCatalogReadService(LeagueRepository leagueRepository, PlayerRepository playerRepository) {
        this.leagueRepository = leagueRepository;
        this.playerRepository = playerRepository;
    }

    @Transactional(readOnly = true)
    public List<List<PlayerResponse>> findCatalog() {
        try {
            return LeagueCatalogProperties.leagues().stream()
                    .map(definition -> leagueRepository.findByCode(definition.code())
                            .map(league -> league.getPlayers().stream()
                                    .map(PlayerMapper::toResponse)
                                    .toList())
                            .orElseGet(List::of))
                    .toList();
        } catch (RuntimeException exception) {
            throw new CatalogUnavailableException("No fue posible leer el catálogo de jugadores", exception);
        }
    }

    @Transactional(readOnly = true)
    public PlayerResponse findPlayer(Integer id) {
        if (id == null || id <= 0) {
            throw new InvalidPlayerIdException("El identificador del jugador debe ser positivo");
        }
        return playerRepository.findById(id)
                .map(PlayerMapper::toResponse)
                .orElseThrow(() -> new PlayerNotFoundException(id));
    }
}
