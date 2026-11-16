package com.dappunq.service;

import com.dappunq.model.valuation.Valuation;

import java.time.Instant;
import java.util.List;

public interface ValuationService {

    Valuation getCurrentValuation(Long userId, Long playerId);
    Valuation getClosestValuation(Long userId, Long playerId, Instant date);
    List<Valuation> getAllValuationsForPlayer(Long userId, Long playerId);
}
