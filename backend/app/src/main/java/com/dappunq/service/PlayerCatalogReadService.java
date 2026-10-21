package com.dappunq.service;

import com.dappunq.dto.LeagueResponse;
import com.dappunq.dto.PlayerResponse;
import com.dappunq.dto.TeamResponse;
import com.dappunq.exception.CatalogUnavailableException;
import com.dappunq.exception.InvalidPlayerIdException;
import com.dappunq.exception.PlayerNotFoundException;
import com.dappunq.persistence.PlayerEntity;
import com.dappunq.persistence.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlayerCatalogReadService {

    private final PlayerRepository playerRepository;
    private final InitialHoldingsService initialHoldingsService;

    public PlayerCatalogReadService(PlayerRepository playerRepository) {
        this(playerRepository, null);
    }

    @Autowired
    public PlayerCatalogReadService(PlayerRepository playerRepository,
                                    @Autowired(required = false) InitialHoldingsService initialHoldingsService) {
        this.playerRepository = playerRepository;
        this.initialHoldingsService = initialHoldingsService;
    }

    @Transactional
    public List<PlayerResponse> findCatalog() {
        try {
            if (initialHoldingsService != null) {
                initialHoldingsService.initializeHoldingsForAllPlayers();
            }
            return playerRepository.findAll().stream()
                    .map(this::toResponse)
                    .toList();
        } catch (RuntimeException exception) {
            throw new CatalogUnavailableException("No fue posible leer el catálogo de jugadores", exception);
        }
    }

    @Transactional
    public PlayerResponse findPlayer(Integer id) {
        if (id == null || id <= 0) {
            throw new InvalidPlayerIdException("El identificador del jugador debe ser positivo");
        }
        PlayerEntity entity = playerRepository.findByExternalId(Long.valueOf(id))
                .orElseThrow(() -> new PlayerNotFoundException(id));
        if (initialHoldingsService != null) {
            initialHoldingsService.initializeHoldingForPlayer(entity);
        }
        return toResponse(entity);
    }

    private PlayerResponse toResponse(PlayerEntity entity) {
        return new PlayerResponse(
                entity.getExternalId(),
                entity.getName(),
                TeamResponse.from(entity.getTeam()),
                entity.getLeague() != null ? LeagueResponse.from(entity.getLeague()) : null,
                entity.getAge(),
                entity.getHeightCM(),
                entity.getWeightKG(),
                entity.getPosition(),
                entity.getAppearances(),
                entity.getMinutesPlayed(),
                entity.getGoals(),
                entity.getAssists(),
                entity.getYellowCards(),
                entity.getRedCards(),
                entity.getAvgShotsPerGame(),
                entity.getPassSuccessPercentage(),
                entity.getAerialsWon(),
                entity.getTimesManOfTheMatch()
        );
    }
}
