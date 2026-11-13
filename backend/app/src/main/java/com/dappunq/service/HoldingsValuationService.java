package com.dappunq.service;

import com.dappunq.model.valuation.*;
import com.dappunq.persistence.HoldingEntity;
import com.dappunq.persistence.HoldingRepository;
import com.dappunq.persistence.MetricNormalizationEntity;
import com.dappunq.persistence.NormalizationRangeRepository;
import com.dappunq.persistence.PlayerRepository;
import com.dappunq.persistence.StrategyConfigEntity;
import com.dappunq.persistence.StrategyConfigRepository;
import com.dappunq.persistence.ValuationEntity;
import com.dappunq.persistence.ValuationRepository;
import com.dappunq.persistence.ValuationPersistenceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;

@Service
public class HoldingsValuationService {

    private static final Logger log = LoggerFactory.getLogger(HoldingsValuationService.class);

    private final HoldingRepository holdings;
    private final ValuationRepository valuationRepository;
    private final PlayerRepository players;
    private final StrategyConfigRepository configs;
    private final ValuationPersistenceMapper mapper;
    private final NormalizationRangeRepository normalizationRangeRepository;
    private final PlayerValuationCalculator calculator = new PlayerValuationCalculator();

    public HoldingsValuationService(HoldingRepository holdings,
                                    ValuationRepository valuationRepository,
                                    PlayerRepository players,
                                    StrategyConfigRepository configs,
                                    ValuationPersistenceMapper mapper,
                                    NormalizationRangeRepository normalizationRangeRepository) {
        this.holdings = holdings;
        this.valuationRepository = valuationRepository;
        this.players = players;
        this.configs = configs;
        this.mapper = mapper;
        this.normalizationRangeRepository = normalizationRangeRepository;
    }

    // TODO: quizá esto deba ser parte de la inicialización del superusuario
    @Transactional
    public int revalue(Long userId, StrategyConfigEntity ce, ExecutionType type) {
        NormalizationContext context = currentNormalizationContext();
        if (context.isEmpty()) {
            List<PlayerSnapshot> snapshots = players.findAll().stream().map(mapper::toSnapshot).toList();
            context = calculator.buildNormalizationContext(snapshots);
            normalizationRangeRepository.replaceCurrentRanges(context.ranges().values().stream()
                    .map(range -> new MetricNormalizationEntity(
                            range.metric(), range.minimum(), range.maximum(), java.time.Instant.now()))
                    .toList());
        }
        StrategyConfig c = new StrategyConfig(ce.getTipoEstrategia(), mapper.read(ce.getPesos()));
        int n = 0;

        for (HoldingEntity h : holdings.findByUserId(userId)) {
            PlayerSnapshot playerSnapshot = mapper.toSnapshot(h.getPlayer());
            BigDecimal v = calculator.calculate(playerSnapshot, c, context, h.getValorUnitario());
            h.valuate(v);
            holdings.save(h);
            valuationRepository.save(new ValuationEntity(h.getUser(), h.getPlayer(), v, c.tipo(), type));
            n++;
        }
        log.info("Revaluadas {} tenencias para el usuario {} con tipo {}", n, userId, type);
        return n;
    }

    private NormalizationContext currentNormalizationContext() {
        EnumMap<Metric, NormalizationRange> ranges = new EnumMap<>(Metric.class);
        normalizationRangeRepository.findCurrentRanges().forEach(entity ->
                ranges.put(entity.getMetric(), mapper.toNormalizationRange(entity)));
        return new NormalizationContext(ranges);
    }

    public List<Holding> list(Long id) {
        return holdings.findByUserId(id).stream()
                .map(mapper::toModel)
                .toList();
    }
}
