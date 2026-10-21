package com.dappunq.persistence;

import com.dappunq.model.valuation.ExecutionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ValuationHistoryRepository extends JpaRepository<ValuationHistoryEntity, Long> {
    List<ValuationHistoryEntity> findByUserIdOrderByEjecutadoEnDesc(Long id);
    boolean existsByUserIdAndPlayerIdAndVentanaAndTipoEjecucion(Long userId, Long playerId, String ventana, ExecutionType tipoEjecucion);
    List<ValuationHistoryEntity> findByVentana(String ventana);
}
