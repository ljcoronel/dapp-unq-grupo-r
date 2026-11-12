package com.dappunq.service;

import com.dappunq.model.valuation.Valuation;
import com.dappunq.persistence.ValuationPersistenceMapper;
import com.dappunq.persistence.ValuationRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

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
        return valuationRepository.findClosestByUserAndPlayerAndDate(userId, playerId, date)
                .map(mapper::toModel)
                .orElseThrow(() ->
                    new RuntimeException("No closest valuation found for userId: " + userId +
                        ", playerId: " + playerId + ", and date: " + date));
    }

    @Override
    public List<Valuation> getAllValuationsForPlayer(Long userId, Long playerId) {
        return valuationRepository.findAllByUserAndPlayer(userId, playerId)
                .stream()
                .map(mapper::toModel)
                .toList();
    }
}
