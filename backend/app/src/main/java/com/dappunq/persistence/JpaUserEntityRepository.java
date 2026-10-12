package com.dappunq.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaUserEntityRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByNombreIgnoreCase(String nombre);
}
