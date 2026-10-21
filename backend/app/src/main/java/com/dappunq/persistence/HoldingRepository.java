package com.dappunq.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HoldingRepository extends JpaRepository<HoldingEntity, Long> {
    List<HoldingEntity> findByUserId(Long id);
    Optional<HoldingEntity> findByUserIdAndPlayerId(Long userId, Long playerId);
    boolean existsByUserIdAndPlayerId(Long userId, Long playerId);
}
