package com.dappunq.service;

import com.dappunq.dto.valuation.RecalculationResult;
import com.dappunq.model.valuation.ExecutionType;
import com.dappunq.model.valuation.PlayerValuationCalculator;
import com.dappunq.model.valuation.StrategyConfig;
import com.dappunq.model.valuation.StrategyStatus;
import com.dappunq.model.valuation.ValuationDomainException;
import com.dappunq.persistence.HoldingEntity;
import com.dappunq.persistence.HoldingRepository;
import com.dappunq.persistence.PlayerEntity;
import com.dappunq.persistence.PlayerRepository;
import com.dappunq.persistence.StrategyConfigEntity;
import com.dappunq.persistence.StrategyConfigRepository;
import com.dappunq.persistence.ValuationEntity;
import com.dappunq.persistence.ValuationHistoryEntity;
import com.dappunq.persistence.ValuationHistoryRepository;
import com.dappunq.persistence.ValuationPersistenceMapper;
import com.dappunq.persistence.ValuationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class WeeklyValuationService {

    private static final Logger log = LoggerFactory.getLogger(WeeklyValuationService.class);

    private final WhoScoredScrapingService scrapingService;
    private final PlayerRepository playerRepository;
    private final StrategyConfigRepository strategyConfigRepository;
    private final HoldingRepository holdingRepository;
    private final ValuationRepository valuationRepository;
    private final ValuationHistoryRepository valuationHistoryRepository;
    private final ValuationPersistenceMapper mapper;
    private final PlayerValuationCalculator calculator = new PlayerValuationCalculator();

    private Clock clock = Clock.system(ZoneId.of("America/Argentina/Buenos_Aires"));

    @Value("${valuation.players-limit:50}")
    private String playersLimit = "50";

    public WeeklyValuationService(WhoScoredScrapingService scrapingService,
                                  PlayerRepository playerRepository,
                                  StrategyConfigRepository strategyConfigRepository,
                                  HoldingRepository holdingRepository,
                                  ValuationRepository valuationRepository,
                                  ValuationHistoryRepository valuationHistoryRepository,
                                  ValuationPersistenceMapper mapper) {
        this.scrapingService = scrapingService;
        this.playerRepository = playerRepository;
        this.strategyConfigRepository = strategyConfigRepository;
        this.holdingRepository = holdingRepository;
        this.valuationRepository = valuationRepository;
        this.valuationHistoryRepository = valuationHistoryRepository;
        this.mapper = mapper;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public void setPlayersLimit(String playersLimit) {
        this.playersLimit = playersLimit;
    }

    @Transactional
    public RecalculationResult execute(ExecutionType executionType) {
        log.info("Iniciando ejecucion de valuacion tipo {} con limite de {} jugadores", executionType, playersLimit);
        scrapingService.scrape(playersLimit != null ? playersLimit : "50");

        List<PlayerEntity> snapshot = playerRepository.findAll();
        if (snapshot == null || snapshot.isEmpty()) {
            log.error("Fallo de snapshot: la lista de jugadores obtenida tras el scrape esta vacia o incompleta");
            throw ValuationDomainException.snapshotFailure("Snapshot de jugadores vacio o incompleto");
        }

        // 1. Aplicar estrategias pendientes antes de recalcular
        List<StrategyConfigEntity> pendingList = strategyConfigRepository.findAllByEstado(StrategyStatus.PENDIENTE);
        log.info("Aplicando {} estrategias pendientes a estado ACTUAL", pendingList.size());
        for (StrategyConfigEntity pending : pendingList) {
            strategyConfigRepository.findFirstByUserIdAndEstado(pending.getUser().getId(), StrategyStatus.ACTUAL)
                    .ifPresent(strategyConfigRepository::delete);
            pending.setEstado(StrategyStatus.ACTUAL);
            pending.setVigenteDesde(Instant.now(clock));
            strategyConfigRepository.save(pending);
        }

        // 2. Recalcular tenencias de usuarios con estrategia actual (omitir usuarios sin estrategia)
        String window = LocalDate.now(clock).toString() + "-" + executionType.name();
        List<StrategyConfigEntity> activeConfigs = strategyConfigRepository.findAllByEstado(StrategyStatus.ACTUAL);
        int recalculatedHoldings = 0;

        for (StrategyConfigEntity activeConfig : activeConfigs) {
            Long userId = activeConfig.getUser().getId();
            StrategyConfig domain = new StrategyConfig(activeConfig.getEstrategia(), mapper.read(activeConfig.getPesos()));
            List<HoldingEntity> userHoldings = holdingRepository.findByUserId(userId);

            for (HoldingEntity holding : userHoldings) {
                BigDecimal unitValue = calculator.calculate(holding.getPlayer(), domain, snapshot);
                holding.valuate(unitValue);
                holdingRepository.save(holding);

                if (!valuationRepository.existsByUserIdAndPlayerIdAndVentana(userId, holding.getPlayer().getId(), window)) {
                    valuationRepository.save(new ValuationEntity(
                            holding.getUser(),
                            holding.getPlayer(),
                            window,
                            domain.strategy(),
                            unitValue
                    ));
                }

                if (!valuationHistoryRepository.existsByUserIdAndPlayerIdAndVentanaAndTipoEjecucion(userId, holding.getPlayer().getId(), window, executionType)) {
                    valuationHistoryRepository.save(new ValuationHistoryEntity(
                            holding.getUser(),
                            holding.getPlayer(),
                            holding.getTokens(),
                            unitValue,
                            domain.strategy(),
                            window,
                            executionType
                    ));
                }
                recalculatedHoldings++;
            }
        }

        log.info("Valuacion tipo {} completada. Jugadores: {}, tenencias recalculadas: {}", executionType, snapshot.size(), recalculatedHoldings);
        return new RecalculationResult("COMPLETED", snapshot.size(), recalculatedHoldings, Instant.now(clock));
    }
}
