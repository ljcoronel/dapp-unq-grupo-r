package com.dappunq.persistence;

import com.dappunq.model.valuation.StrategyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StrategyConfigRepository extends JpaRepository<StrategyConfigEntity, Long> {
    Optional<StrategyConfigEntity> findFirstByUserIdAndEstado(Long id, StrategyStatus e);
    Optional<StrategyConfigEntity> findByUserIdAndEstado(Long id, StrategyStatus e);
    List<StrategyConfigEntity> findAllByUserId(Long id);
    List<StrategyConfigEntity> findAllByEstado(StrategyStatus e);
    void deleteByUserIdAndEstado(Long userId, StrategyStatus estado);
}
