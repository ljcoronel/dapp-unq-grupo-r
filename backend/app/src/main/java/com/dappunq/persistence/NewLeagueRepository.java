package com.dappunq.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

public interface NewLeagueRepository extends JpaRepository<NewLeagueEntity, Long> {
    Optional<NewLeagueEntity> findByExternalId(Long externalId);

    List<NewLeagueEntity> findByExternalIdIn(Collection<Long> externalIds);

    List<NewLeagueEntity> findAllByOrderByIdAsc();
}
