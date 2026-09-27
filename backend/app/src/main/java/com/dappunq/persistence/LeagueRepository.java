package com.dappunq.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeagueRepository extends JpaRepository<LeagueEntity, Long> {
    Optional<LeagueEntity> findByCode(String code);
    List<LeagueEntity> findAllByOrderByIdAsc();
}
