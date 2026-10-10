package com.dappunq.service;

import com.dappunq.dto.LeagueResponse;
import com.dappunq.dto.PlayerResponse;
import com.dappunq.exception.CatalogUnavailableException;
import com.dappunq.exception.InvalidPlayerIdException;
import com.dappunq.exception.PlayerNotFoundException;
import com.dappunq.persistence.NewLeagueEntity;
import com.dappunq.persistence.NewLeagueRepository;
import com.dappunq.persistence.WhoScoredEntity;
import com.dappunq.persistence.WhoScoredRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlayerCatalogReadService {
    private final NewLeagueRepository leagueRepository;
    private final WhoScoredRepository playerRepository;

    public PlayerCatalogReadService(NewLeagueRepository leagueRepository, WhoScoredRepository playerRepository) {
        this.leagueRepository = leagueRepository;
        this.playerRepository = playerRepository;
    }

    @Transactional(readOnly = true)
    public List<List<PlayerResponse>> findCatalog() {
        try {
            return leagueRepository.findAllByOrderByIdAsc().stream()
                    .map(this::readLeague)
                    .toList();
        } catch (RuntimeException exception) {
            throw new CatalogUnavailableException("No fue posible leer el catálogo de jugadores", exception);
        }
    }

    private List<PlayerResponse> readLeague(NewLeagueEntity league) {
        return playerRepository.findByLeagueOrderByIdAsc(league).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlayerResponse findPlayer(Integer id) {
        if (id == null || id <= 0) {
            throw new InvalidPlayerIdException("El identificador del jugador debe ser positivo");
        }
        return playerRepository.findByExternalId(Long.valueOf(id))
                .map(this::toResponse)
                .orElseThrow(() -> new PlayerNotFoundException(id));
    }

    private PlayerResponse toResponse(WhoScoredEntity entity) {
        return new PlayerResponse(
                entity.getExternalId() != null ? entity.getExternalId().intValue() : null,
                entity.getName(),
                null,
                entity.getTeam() != null ? entity.getTeam().getName() : null,
                entity.getLeague() != null ? new LeagueResponse(
                        entity.getLeague().getExternalId() != null ? String.valueOf(entity.getLeague().getExternalId()) : null,
                        entity.getLeague().getName(),
                        entity.getLeague().getCountry()) : null,
                entity.getAppearances(),
                entity.getGoals(),
                entity.getAssists(),
                null
        );
    }
}
