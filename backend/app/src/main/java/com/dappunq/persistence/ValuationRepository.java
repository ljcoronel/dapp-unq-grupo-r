package com.dappunq.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ValuationRepository extends JpaRepository<ValuationEntity, Long> {

    @Query("SELECT v FROM ValuationEntity v WHERE v.user.id = :userId AND v.isCurrent = false")
    List<ValuationEntity> findHistoricalByUser(
        @Param("userId") Long userId);

    @Query("SELECT v FROM ValuationEntity v WHERE v.user.id = :userId AND v.player.id = :playerId AND v.isCurrent = true")
    Optional<ValuationEntity> findCurrentByUserAndPlayer(
        @Param("userId") Long userId,
        @Param("playerId") Long playerId
    );

    @Query("SELECT v FROM ValuationEntity  v WHERE v.isCurrent = true")
    List<ValuationEntity> findAllCurrent();

    @Query("SELECT v FROM ValuationEntity  v WHERE v.isCurrent = false")
    List<ValuationEntity> findAllHistorical();

    @Query("SELECT v FROM ValuationEntity  v WHERE v.user.id = :userId AND v.player.id = :playerId ORDER BY v.ejecutadoEn DESC")
    List<ValuationEntity> findAllByUserAndPlayer(
        @Param("userId") Long userId,
        @Param("playerId") Long playerId
    );
}
