package com.dappunq.service;

import com.dappunq.model.valuation.Valuation;

import java.time.Instant;

public interface ValuationService {

    Valuation getCurrentValuation(Long userId, Long playerId);
    Valuation getClosestValuation(Long userId, Long playerId, Instant date);
    Valuation getAllValuations(Long userId, Long playerId);
}
