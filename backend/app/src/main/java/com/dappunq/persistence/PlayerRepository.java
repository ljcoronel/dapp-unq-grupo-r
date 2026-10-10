package com.dappunq.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {
    Optional<PlayerEntity> findByExternalId(Long externalId);

    List<PlayerEntity> findByExternalIdIn(Collection<Long> externalIds);

    List<PlayerEntity> findByLeagueOrderByIdAsc(LeagueEntity league);

    List<PlayerEntity> findByLeagueIdOrderByIdAsc(Long leagueId);
}
