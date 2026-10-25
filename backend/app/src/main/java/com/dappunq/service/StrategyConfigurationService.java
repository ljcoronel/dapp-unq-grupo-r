package com.dappunq.service;

import com.dappunq.dto.valuation.StrategyRequest;
import com.dappunq.model.valuation.ExecutionType;
import com.dappunq.model.valuation.StrategyConfig;
import com.dappunq.model.valuation.StrategyState;
import com.dappunq.model.valuation.StrategyStatus;
import com.dappunq.model.valuation.StrategyType;
import com.dappunq.model.valuation.ValuationDomainException;
import com.dappunq.persistence.JpaUserEntityRepository;
import com.dappunq.persistence.StrategyConfigEntity;
import com.dappunq.persistence.StrategyConfigRepository;
import com.dappunq.persistence.UserEntity;
import com.dappunq.persistence.ValuationPersistenceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class StrategyConfigurationService {

    private final JpaUserEntityRepository users;
    private final StrategyConfigRepository configs;
    private final ValuationPersistenceMapper mapper;
    private final HoldingsValuationService holdings;

    public StrategyConfigurationService(JpaUserEntityRepository users,
                                        StrategyConfigRepository configs,
                                        ValuationPersistenceMapper mapper,
                                        HoldingsValuationService holdings) {
        this.users = users;
        this.configs = configs;
        this.mapper = mapper;
        this.holdings = holdings;
    }

    @Transactional
    public StrategyState configure(Long id, StrategyRequest r) {
        UserEntity u = users.findById(id).orElseThrow(() -> new NoSuchElementException("Usuario inexistente"));
        StrategyConfig domain = new StrategyConfig(r.estrategia(), r.pesos());

        Optional<StrategyConfigEntity> current = configs.findFirstByUserIdAndEstado(id, StrategyStatus.ACTUAL);

        if (r.estado() == StrategyStatus.PENDIENTE && current.isEmpty()) {
            throw ValuationDomainException.pendingWithoutCurrent();
        }

        StrategyStatus targetStatus = current.isEmpty() ? StrategyStatus.ACTUAL : StrategyStatus.PENDIENTE;

        if (targetStatus == StrategyStatus.PENDIENTE) {
            configs.findFirstByUserIdAndEstado(id, StrategyStatus.PENDIENTE).ifPresent(configs::delete);
        }

        StrategyConfigEntity saved = configs.save(new StrategyConfigEntity(
                u,
                domain.strategy(),
                mapper.write(domain.weights()),
                targetStatus,
                Instant.now()
        ));

        if (targetStatus == StrategyStatus.ACTUAL) {
            holdings.revalue(id, saved, ExecutionType.MANUAL);
        }

        StrategyType estrategiaActual = current
                .map(StrategyConfigEntity::getEstrategia)
                .orElse(targetStatus == StrategyStatus.ACTUAL ? saved.getEstrategia() : null);

        Map<String, BigDecimal> pesosActual = current
                .map(c -> mapper.read(c.getPesos()))
                .orElse(targetStatus == StrategyStatus.ACTUAL ? domain.weights() : null);

        Instant vigenteDesdeActual = current
                .map(StrategyConfigEntity::getVigenteDesde)
                .orElse(targetStatus == StrategyStatus.ACTUAL ? saved.getVigenteDesde() : null);

        StrategyType estrategiaPendiente = (targetStatus == StrategyStatus.PENDIENTE) ? saved.getEstrategia() : null;
        Map<String, BigDecimal> pesosPendiente = (targetStatus == StrategyStatus.PENDIENTE) ? domain.weights() : null;
        Instant vigentesDesdePendiente = (targetStatus == StrategyStatus.PENDIENTE) ? saved.getVigenteDesde() : null;

        return new StrategyState(
                saved.getEstrategia(),
                domain.weights(),
                targetStatus,
                saved.getVigenteDesde(),
                estrategiaActual,
                pesosActual,
                vigenteDesdeActual,
                estrategiaPendiente,
                pesosPendiente,
                vigentesDesdePendiente
        );
    }

    @Transactional
    public StrategyState configurePending(Long id, StrategyRequest r) {
        Optional<StrategyConfigEntity> current = configs.findFirstByUserIdAndEstado(id, StrategyStatus.ACTUAL);
        if (current.isEmpty()) {
            throw ValuationDomainException.pendingWithoutCurrent();
        }
        return configure(id, new StrategyRequest(r.estrategia(), r.pesos(), StrategyStatus.PENDIENTE));
    }

    @Transactional(readOnly = true)
    public StrategyState get(Long id) {
        users.findById(id).orElseThrow(() -> new NoSuchElementException("Usuario inexistente"));
        Optional<StrategyConfigEntity> actual = configs.findFirstByUserIdAndEstado(id, StrategyStatus.ACTUAL);
        Optional<StrategyConfigEntity> pendiente = configs.findFirstByUserIdAndEstado(id, StrategyStatus.PENDIENTE);

        if (actual.isEmpty() && pendiente.isEmpty()) {
            return new StrategyState(null, null, StrategyStatus.SIN_ESTRATEGIA, null, null, null, null, null, null, null);
        }

        StrategyType estrategiaActual = actual.map(StrategyConfigEntity::getEstrategia).orElse(null);
        Map<String, BigDecimal> pesosActual = actual.map(e -> mapper.read(e.getPesos())).orElse(null);
        Instant vigenteDesdeActual = actual.map(StrategyConfigEntity::getVigenteDesde).orElse(null);

        StrategyType estrategiaPendiente = pendiente.map(StrategyConfigEntity::getEstrategia).orElse(null);
        Map<String, BigDecimal> pesosPendiente = pendiente.map(e -> mapper.read(e.getPesos())).orElse(null);
        Instant vigentesDesdePendiente = pendiente.map(StrategyConfigEntity::getVigenteDesde).orElse(null);

        StrategyStatus status = pendiente.isPresent() ? StrategyStatus.PENDIENTE : StrategyStatus.ACTUAL;
        StrategyConfigEntity main = pendiente.orElseGet(actual::get);

        return new StrategyState(
                main.getEstrategia(),
                mapper.read(main.getPesos()),
                status,
                main.getVigenteDesde(),
                estrategiaActual,
                pesosActual,
                vigenteDesdeActual,
                estrategiaPendiente,
                pesosPendiente,
                vigentesDesdePendiente
        );
    }
}
