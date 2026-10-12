package com.dappunq.service;

import com.dappunq.dto.LeagueResponse;
import com.dappunq.dto.PlayerResponse;
import com.dappunq.dto.TeamResponse;
import com.dappunq.exception.CatalogUnavailableException;
import com.dappunq.exception.InvalidPlayerIdException;
import com.dappunq.exception.PlayerNotFoundException;
import com.dappunq.persistence.PlayerEntity;
import com.dappunq.persistence.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlayerCatalogReadService {
    private final PlayerRepository playerRepository;

    public PlayerCatalogReadService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Transactional(readOnly = true)
    public List<PlayerResponse> findCatalog() {
        try {
            return playerRepository.findAll().stream()
                    .map(this::toResponse)
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
        return playerRepository.findByExternalId(Long.valueOf(id))
                .map(this::toResponse)
                .orElseThrow(() -> new PlayerNotFoundException(id));
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
