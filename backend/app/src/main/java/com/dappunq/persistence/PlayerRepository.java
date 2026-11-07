package com.dappunq.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {
    Optional<PlayerEntity> findByExternalId(Long externalId);

    List<PlayerEntity> findByExternalIdIn(Collection<Long> externalIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PlayerEntity p where p.id = :id")
    Optional<PlayerEntity> findByIdForUpdate(@Param("id") Long id);
}
