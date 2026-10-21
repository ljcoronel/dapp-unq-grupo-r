package com.dappunq.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ValuationRepository extends JpaRepository<ValuationEntity, Long> {
    boolean existsByUserIdAndPlayerIdAndVentana(Long userId, Long playerId, String ventana);
    Optional<ValuationEntity> findByUserIdAndPlayerIdAndVentana(Long userId, Long playerId, String ventana);
    List<ValuationEntity> findByVentana(String ventana);
}
