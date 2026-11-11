package com.dappunq.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ValuationRepositoryDeprecated extends JpaRepository<ValuationEntityDeprecated, Long> {
    boolean existsByUserIdAndPlayerIdAndVentana(Long userId, Long playerId, String ventana);
}
