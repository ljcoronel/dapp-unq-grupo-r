package com.dappunq.service;

import com.dappunq.persistence.HoldingEntity;
import com.dappunq.persistence.HoldingRepository;
import com.dappunq.persistence.PlayerEntity;
import com.dappunq.persistence.PlayerRepository;
import com.dappunq.persistence.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class InitialHoldingsService {

    public static final int INITIAL_TOKENS_PER_PLAYER = 100;

    private final SuperuserInitializationService superuserService;
    private final HoldingRepository holdingRepository;
    private final PlayerRepository playerRepository;

    public InitialHoldingsService(SuperuserInitializationService superuserService,
                                  HoldingRepository holdingRepository,
                                  PlayerRepository playerRepository) {
        this.superuserService = superuserService;
        this.holdingRepository = holdingRepository;
        this.playerRepository = playerRepository;
    }

    @Transactional
    public HoldingEntity initializeHoldingForPlayer(PlayerEntity player) {
        UserEntity superuser = superuserService.getOrCreateSuperuser();

        return holdingRepository.findByUserIdAndPlayerId(superuser.getId(), player.getId())
                .orElseGet(() -> holdingRepository.save(new HoldingEntity(
                        superuser,
                        player,
                        INITIAL_TOKENS_PER_PLAYER
                )));
    }

    @Transactional
    public List<HoldingEntity> initializeHoldingsForAllPlayers() {
        UserEntity superuser = superuserService.getOrCreateSuperuser();
        List<PlayerEntity> players = playerRepository.findAll();
        List<HoldingEntity> result = new ArrayList<>();

        for (PlayerEntity player : players) {
            HoldingEntity holding = holdingRepository.findByUserIdAndPlayerId(superuser.getId(), player.getId())
                    .orElseGet(() -> holdingRepository.save(new HoldingEntity(
                            superuser,
                            player,
                            INITIAL_TOKENS_PER_PLAYER
                    )));
            result.add(holding);
        }

        return result;
    }
}
