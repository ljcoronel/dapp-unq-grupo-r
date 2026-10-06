package com.dappunq.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScrapperRepository extends JpaRepository<WhoScoredEntity, Long> {
}
