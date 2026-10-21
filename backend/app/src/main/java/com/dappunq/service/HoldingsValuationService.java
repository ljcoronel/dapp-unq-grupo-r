package com.dappunq.service;

import com.dappunq.dto.valuation.HoldingHistoryResponse;
import com.dappunq.dto.valuation.HoldingResponse;
import com.dappunq.model.valuation.ExecutionType;
import com.dappunq.model.valuation.PlayerValuationCalculator;
import com.dappunq.model.valuation.StrategyConfig;
import com.dappunq.persistence.HoldingEntity;
import com.dappunq.persistence.HoldingRepository;
import com.dappunq.persistence.PlayerEntity;
import com.dappunq.persistence.PlayerRepository;
import com.dappunq.persistence.StrategyConfigEntity;
import com.dappunq.persistence.StrategyConfigRepository;
import com.dappunq.persistence.ValuationHistoryEntity;
import com.dappunq.persistence.ValuationHistoryRepository;
import com.dappunq.persistence.ValuationPersistenceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class HoldingsValuationService {

    private static final Logger log = LoggerFactory.getLogger(HoldingsValuationService.class);

    private final HoldingRepository holdings;
    private final ValuationHistoryRepository history;
    private final PlayerRepository players;
    private final StrategyConfigRepository configs;
    private final ValuationPersistenceMapper mapper;
    private final PlayerValuationCalculator calculator = new PlayerValuationCalculator();

    public HoldingsValuationService(HoldingRepository holdings,
                                    ValuationHistoryRepository history,
                                    PlayerRepository players,
                                    StrategyConfigRepository configs,
                                    ValuationPersistenceMapper mapper) {
        this.holdings = holdings;
        this.history = history;
        this.players = players;
        this.configs = configs;
        this.mapper = mapper;
    }

    @Transactional
    public int revalue(Long userId, StrategyConfigEntity ce, ExecutionType type) {
        List<PlayerEntity> snapshot = players.findAll();
        StrategyConfig c = new StrategyConfig(ce.getEstrategia(), mapper.read(ce.getPesos()));
        int n = 0;
        String window = LocalDate.now().toString() + "-" + type;

        for (HoldingEntity h : holdings.findByUserId(userId)) {
            BigDecimal v = calculator.calculate(h.getPlayer(), c, snapshot);
            h.valuate(v);
            holdings.save(h);
            history.save(new ValuationHistoryEntity(h.getUser(), h.getPlayer(), h.getTokens(), v, c.strategy(), window, type));
            n++;
        }
        log.info("Revaluadas {} tenencias para el usuario {} con tipo {}", n, userId, type);
        return n;
    }

    public List<HoldingResponse> list(Long id) {
        return holdings.findByUserId(id).stream()
                .map(h -> new HoldingResponse(
                        h.getId(),
                        h.getUser().getId(),
                        h.getPlayer().getId(),
                        h.getTokens(),
                        h.getValorTotal(),
                        h.getValorUnitario()
                ))
                .toList();
    }

    public List<HoldingHistoryResponse> history(Long id) {
        return history.findByUserIdOrderByEjecutadoEnDesc(id).stream()
                .map(h -> new HoldingHistoryResponse(
                        h.getUser().getId(),
                        h.getPlayer().getId(),
                        h.getTokens(),
                        h.getValorUnitario(),
                        h.getValorTotal(),
                        h.getEstrategia(),
                        h.getEjecutadoEn(),
                        h.getTipoEjecucion()
                ))
                .toList();
    }
}
