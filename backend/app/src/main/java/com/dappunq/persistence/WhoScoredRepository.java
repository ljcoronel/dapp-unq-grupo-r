package com.dappunq.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

public interface WhoScoredRepository extends JpaRepository<WhoScoredEntity, Long> {
    Optional<WhoScoredEntity> findByExternalId(Long externalId);

    List<WhoScoredEntity> findByExternalIdIn(Collection<Long> externalIds);

    List<WhoScoredEntity> findByLeagueOrderByIdAsc(NewLeagueEntity league);

    List<WhoScoredEntity> findByLeagueIdOrderByIdAsc(Long leagueId);
}
