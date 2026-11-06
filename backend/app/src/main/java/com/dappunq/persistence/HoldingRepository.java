package com.dappunq.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<HoldingEntity, Long> {
    List<HoldingEntity> findByUserId(Long id);
    Optional<HoldingEntity> findByUserIdAndPlayerId(Long userId, Long playerId);
    boolean existsByPlayerId(Long playerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from HoldingEntity h where h.user.id = :userId and h.player.id = :playerId")
    Optional<HoldingEntity> findByUserIdAndPlayerIdForUpdate(@Param("userId") Long userId,
                                                              @Param("playerId") Long playerId);
}
