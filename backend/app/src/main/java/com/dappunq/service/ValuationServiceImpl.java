package com.dappunq.service;

import com.dappunq.model.valuation.Valuation;
import com.dappunq.persistence.ValuationPersistenceMapper;
import com.dappunq.persistence.ValuationRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ValuationServiceImpl implements ValuationService {

    private final ValuationRepository valuationRepository;
    private final ValuationPersistenceMapper mapper;

    public ValuationServiceImpl(ValuationRepository valuationRepository, ValuationPersistenceMapper mapper) {
        this.valuationRepository = valuationRepository;
        this.mapper = mapper;
    }

    @Override
    public Valuation getCurrentValuation(Long userId, Long playerId) {
        return valuationRepository.findCurrentByUserAndPlayer(userId, playerId)
                .map(mapper::toModel)
                .orElseThrow(() ->
                    new RuntimeException("No current valuation found for userId: " + userId +
                        " and playerId: " + playerId));
    }

    @Override
    public Valuation getClosestValuation(Long userId, Long playerId, Instant date) {
        return null;
    }

    @Override
    public Valuation getAllValuations(Long userId, Long playerId) {
        return null;
    }
}
