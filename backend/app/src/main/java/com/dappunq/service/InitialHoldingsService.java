package com.dappunq.service;

import com.dappunq.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InitialHoldingsService {

    public static final int INITIAL_TOKENS_PER_PLAYER = 100;

    private final HoldingRepository holdingRepository;
    private final PlayerRepository playerRepository;

    public InitialHoldingsService(HoldingRepository holdingRepository,
                                  PlayerRepository playerRepository) {
        this.holdingRepository = holdingRepository;
        this.playerRepository = playerRepository;
    }

    @Transactional
    public HoldingEntity initializeHoldingForPlayer(PlayerEntity player, UserEntity superuser) {
        return holdingRepository.findByUserIdAndPlayerId(superuser.getId(), player.getId())
                .orElseGet(() -> holdingRepository.save(new HoldingEntity(
                        superuser,
                        player,
                        INITIAL_TOKENS_PER_PLAYER
                )));
    }

    @Transactional
    public void initializeHoldingsForAllPlayers(UserEntity superuser) {
        List<PlayerEntity> players = playerRepository.findAll();
        for (PlayerEntity player : players) {
            boolean exists = holdingRepository.existsByPlayerId(player.getId());
            saveHoldingIfNotExists(superuser, player, exists);
        }
    }

    private void saveHoldingIfNotExists(UserEntity superuser, PlayerEntity player, boolean exists) {
        if (!exists) {
            holdingRepository.save(new HoldingEntity(
                superuser,
                player,
                INITIAL_TOKENS_PER_PLAYER));
        }
    }
}
