package com.dappunq.service;

import com.dappunq.model.valuation.StrategyConfig;
import com.dappunq.model.valuation.StrategyState;
import com.dappunq.model.valuation.StrategyStatus;
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
public class StrategyConfigurationServiceImpl implements StrategyConfigurationService {

    private final JpaUserEntityRepository users;
    private final StrategyConfigRepository configs;
    private final ValuationPersistenceMapper mapper;

    public StrategyConfigurationServiceImpl(JpaUserEntityRepository users,
                                            StrategyConfigRepository configs,
                                            ValuationPersistenceMapper mapper) {
        this.users = users;
        this.configs = configs;
        this.mapper = mapper;
    }

    @Transactional
    public StrategyState configure(Long id, StrategyConfig r) {
        UserEntity u = users.findById(id).orElseThrow(() -> new NoSuchElementException("Usuario inexistente"));

        Optional<StrategyConfigEntity> current = configs.findFirstByUserIdAndEstado(id, StrategyStatus.ACTUAL);
        Optional<StrategyConfigEntity> pending = configs.findFirstByUserIdAndEstado(id, StrategyStatus.PENDIENTE);

        if (current.isEmpty()) {
            StrategyConfigEntity saved = configs.save(new StrategyConfigEntity(u, r.tipo(),
                mapper.write(r.weights()), StrategyStatus.ACTUAL, Instant.now()));
            return new StrategyState(saved.getEstado(), saved.getTipoEstrategia(), mapper.read(saved.getPesos()),
                saved.getVigenteDesde(), null, null, null, null);
        }

        StrategyConfigEntity actual = current.get();
        var estadoActual = actual.getEstado();
        var tipoActual = actual.getTipoEstrategia();
        var pesosActual = mapper.read(actual.getPesos());
        var vigenteDesdeActual = actual.getVigenteDesde();

        pending.ifPresent(configs::delete);
        StrategyConfigEntity savedPendiente = configs.save(new StrategyConfigEntity(u, r.tipo(), mapper.write(r.weights()),
            StrategyStatus.PENDIENTE, Instant.now()));
        return new StrategyState(estadoActual, tipoActual, pesosActual, vigenteDesdeActual, savedPendiente.getEstado(),
            savedPendiente.getTipoEstrategia(), mapper.read(savedPendiente.getPesos()), savedPendiente.getVigenteDesde());
    }

    @Transactional(readOnly = true)
    public StrategyState get(Long id) {
        users.findById(id).orElseThrow(() -> new NoSuchElementException("Usuario inexistente"));
        Optional<StrategyConfigEntity> actual = configs.findFirstByUserIdAndEstado(id, StrategyStatus.ACTUAL);
        Optional<StrategyConfigEntity> pendiente = configs.findFirstByUserIdAndEstado(id, StrategyStatus.PENDIENTE);
        StrategyConfigEntity eActual;
        StrategyConfigEntity ePendiente;

        if (actual.isEmpty()) {
            return new StrategyState(StrategyStatus.SIN_ESTRATEGIA, null, null, null, null, null, null, null);
        }

        eActual = actual.get();
        var estadoActual = eActual.getEstado();
        var tipoActual = eActual.getTipoEstrategia();
        var pesosActual = mapper.read(eActual.getPesos());
        var vigenteDeesde = eActual.getVigenteDesde();

        if (pendiente.isEmpty()) {
            return new StrategyState(estadoActual, tipoActual,
                pesosActual, vigenteDeesde, null, null,
                null, null);
        }

        ePendiente = pendiente.get();
        var estadoPendiente = ePendiente.getEstado();
        var tipoPendiente = ePendiente.getTipoEstrategia();
        var pesosPendiente = mapper.read(ePendiente.getPesos());
        var vigenteDesdePendiente = ePendiente.getVigenteDesde();

        return new StrategyState(estadoActual, tipoActual,
            pesosActual, vigenteDeesde, estadoPendiente, tipoPendiente,
            pesosPendiente, vigenteDesdePendiente);
    }
}
