package com.dappunq.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

public interface TeamRepository extends JpaRepository<TeamEntity, Long> {
    Optional<TeamEntity> findByExternalId(Long externalId);

    List<TeamEntity> findByExternalIdIn(Collection<Long> externalIds);

    List<TeamEntity> findAllByOrderByIdAsc();
}
