package com.dappunq.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

public interface LeagueRepository extends JpaRepository<LeagueEntity, Long> {
    Optional<LeagueEntity> findByExternalId(Long externalId);

    List<LeagueEntity> findByExternalIdIn(Collection<Long> externalIds);

    List<LeagueEntity> findAllByOrderByIdAsc();
}
