package com.dappunq.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerRepository extends JpaRepository<PlayerEntity, Integer> {
    List<PlayerEntity> findByLeagueCodeOrderByIdAsc(String code);
    List<PlayerEntity> findByLeagueCodeOrderByNameAsc(String code);
}
