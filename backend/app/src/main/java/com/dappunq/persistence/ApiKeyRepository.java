package com.dappunq.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKeyEntity, Long> {
    Optional<ApiKeyEntity> findByPermission(String permission);

    Optional<ApiKeyEntity> findByPermissionAndActiveTrue(String permission);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from ApiKeyEntity a where a.permission = :permission")
    Optional<ApiKeyEntity> findByPermissionForUpdate(@Param("permission") String permission);
}
