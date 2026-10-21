package com.dappunq.service;

import com.dappunq.dto.valuation.StrategyConfigDto;
import com.dappunq.dto.valuation.StrategyRequest;
import com.dappunq.dto.valuation.StrategyStateResponse;
import com.dappunq.model.valuation.ExecutionType;
import com.dappunq.model.valuation.StrategyConfig;
import com.dappunq.model.valuation.StrategyStatus;
import com.dappunq.model.valuation.ValuationDomainException;
import com.dappunq.persistence.JpaUserEntityRepository;
import com.dappunq.persistence.StrategyConfigEntity;
import com.dappunq.persistence.StrategyConfigRepository;
import com.dappunq.persistence.UserEntity;
import com.dappunq.persistence.ValuationPersistenceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
    public StrategyStateResponse configure(Long id, StrategyRequest r) {
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

        StrategyConfigDto actualDto = current
                .map(c -> new StrategyConfigDto(c.getEstrategia(), mapper.read(c.getPesos()), c.getVigenteDesde()))
                .orElse(targetStatus == StrategyStatus.ACTUAL
                        ? new StrategyConfigDto(saved.getEstrategia(), domain.weights(), saved.getVigenteDesde())
                        : null);

        StrategyConfigDto pendienteDto = (targetStatus == StrategyStatus.PENDIENTE)
                ? new StrategyConfigDto(saved.getEstrategia(), domain.weights(), saved.getVigenteDesde())
                : null;

        return new StrategyStateResponse(
                saved.getEstrategia(),
                domain.weights(),
                targetStatus,
                saved.getVigenteDesde(),
                actualDto,
                pendienteDto
        );
    }

    @Transactional
    public StrategyStateResponse configurePending(Long id, StrategyRequest r) {
        Optional<StrategyConfigEntity> current = configs.findFirstByUserIdAndEstado(id, StrategyStatus.ACTUAL);
        if (current.isEmpty()) {
            throw ValuationDomainException.pendingWithoutCurrent();
        }
        return configure(id, new StrategyRequest(r.estrategia(), r.pesos(), StrategyStatus.PENDIENTE));
    }

    @Transactional(readOnly = true)
    public StrategyStateResponse get(Long id) {
        users.findById(id).orElseThrow(() -> new NoSuchElementException("Usuario inexistente"));
        Optional<StrategyConfigEntity> actual = configs.findFirstByUserIdAndEstado(id, StrategyStatus.ACTUAL);
        Optional<StrategyConfigEntity> pendiente = configs.findFirstByUserIdAndEstado(id, StrategyStatus.PENDIENTE);

        if (actual.isEmpty() && pendiente.isEmpty()) {
            return new StrategyStateResponse(null, null, StrategyStatus.SIN_ESTRATEGIA, null, null, null);
        }

        StrategyConfigDto actualDto = actual
                .map(e -> new StrategyConfigDto(e.getEstrategia(), mapper.read(e.getPesos()), e.getVigenteDesde()))
                .orElse(null);

        StrategyConfigDto pendienteDto = pendiente
                .map(e -> new StrategyConfigDto(e.getEstrategia(), mapper.read(e.getPesos()), e.getVigenteDesde()))
                .orElse(null);

        StrategyStatus status = pendiente.isPresent() ? StrategyStatus.PENDIENTE : StrategyStatus.ACTUAL;
        StrategyConfigEntity main = pendiente.orElseGet(actual::get);

        return new StrategyStateResponse(
                main.getEstrategia(),
                mapper.read(main.getPesos()),
                status,
                main.getVigenteDesde(),
                actualDto,
                pendienteDto
        );
    }
}
