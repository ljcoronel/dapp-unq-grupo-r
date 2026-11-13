package com.dappunq.service;

import com.dappunq.exception.PlayerNotFoundException;
import com.dappunq.exception.UserNotFoundException;
import com.dappunq.model.valuation.*;
import com.dappunq.persistence.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

// TODO: testear este Service
@Service
public class ValuationServiceImpl implements ValuationService {

    private final ValuationRepository valuationRepository;
    private final NormalizationRangeRepository normalizationRangeRepository;
    private final PlayerRepository playerRepository;
    private final ValuationPersistenceMapper mapper;
    private final StrategyConfigRepository strategyRepository;
    private final JpaUserEntityRepository userRepository;
    private final PlayerValuationCalculator calculator = new PlayerValuationCalculator();

    public ValuationServiceImpl(ValuationRepository valuationRepository, ValuationPersistenceMapper mapper,
                                NormalizationRangeRepository normalizationRangeRepository,
                                PlayerRepository playerRepository, StrategyConfigRepository strategyRepository,
                                JpaUserEntityRepository userRepository) {
        this.valuationRepository = valuationRepository;
        this.mapper = mapper;
        this.normalizationRangeRepository = normalizationRangeRepository;
        this.playerRepository = playerRepository;
        this.strategyRepository = strategyRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Valuation getCurrentValuation(Long userId, Long playerId) {
        Optional<ValuationEntity> optionalValuation = valuationRepository.findCurrentByUserAndPlayer(userId, playerId);
        if (optionalValuation.isEmpty()) {
            NormalizationContext context = currentNormalizationContext();
            if (context.isEmpty()) {
                List<PlayerSnapshot> snapshots = playerRepository.findAll().stream().map(mapper::toSnapshot).toList();
                context = calculator.buildNormalizationContext(snapshots);
                normalizationRangeRepository.replaceCurrentRanges(context.ranges().values().stream()
                    .map(range -> new MetricNormalizationEntity(
                        range.metric(), range.minimum(), range.maximum(), java.time.Instant.now()))
                    .toList());
            }
            PlayerEntity player = playerRepository.findById(playerId).
                orElseThrow(() -> new PlayerNotFoundException(playerId));
            UserEntity user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
            StrategyConfigEntity strategyConfig = strategyRepository.findFirstByUserIdAndEstado(userId, StrategyStatus.ACTUAL)
                .orElseThrow(() -> new ValuationDomainException("No strategy config found for userId: " + userId));
            BigDecimal v = calculator.calculate(mapper.toSnapshot(player),
                new StrategyConfig(strategyConfig.getTipoEstrategia(), mapper.read(strategyConfig.getPesos())), context, BigDecimal.ZERO);
            // TODO: evaluar eliminar el campo valorTotal de Valuation (ese 1 fue puesto para que funcione)
            // TODO: evaluar eliminar el campo window de Valuation y que lo absorba ejecutadoEn
            ValuationEntity result = valuationRepository.save(new ValuationEntity(user, player,
                v.setScale(2, RoundingMode.HALF_UP),
                strategyConfig.getTipoEstrategia(), ExecutionType.MANUAL));
            return mapper.toModel(result);
        }
        return optionalValuation
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

    private NormalizationContext currentNormalizationContext() {
        EnumMap<Metric, NormalizationRange> ranges = new EnumMap<>(Metric.class);
        normalizationRangeRepository.findCurrentRanges().forEach(entity ->
            ranges.put(entity.getMetric(), mapper.toNormalizationRange(entity)));
        return new NormalizationContext(ranges);
    }
}
