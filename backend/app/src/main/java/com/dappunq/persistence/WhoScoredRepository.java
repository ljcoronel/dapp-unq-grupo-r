package com.dappunq.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WhoScoredRepository extends JpaRepository<WhoScoredEntity, Long> {
    Optional<WhoScoredEntity> findByExternalId(Long externalId);

    List<WhoScoredEntity> findByLeagueOrderByIdAsc(NewLeagueEntity league);

    List<WhoScoredEntity> findByLeagueIdOrderByIdAsc(Long leagueId);
}
