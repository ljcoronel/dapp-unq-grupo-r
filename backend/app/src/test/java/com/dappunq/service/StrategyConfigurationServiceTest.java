package com.dappunq.service;

import com.dappunq.model.valuation.*;
import com.dappunq.persistence.JpaUserEntityRepository;
import com.dappunq.persistence.StrategyConfigEntity;
import com.dappunq.persistence.StrategyConfigRepository;
import com.dappunq.persistence.UserEntity;
import com.dappunq.persistence.ValuationPersistenceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StrategyConfigurationServiceTest {

    @Mock
    private JpaUserEntityRepository userRepository;

    @Mock
    private StrategyConfigRepository strategyConfigRepository;

    @Mock
    private HoldingsValuationService holdingsValuationService;

    private final ValuationPersistenceMapper mapper = new ValuationPersistenceMapper();
    private StrategyConfigurationService service;

    private UserEntity user;
    private Map<String, BigDecimal> validWeightsA;
    private Map<String, BigDecimal> validWeightsB;

    @BeforeEach
    void setUp() {
        service = new StrategyConfigurationService(userRepository, strategyConfigRepository, mapper);
        user = new UserEntity(1L, "testuser", "hash");

        validWeightsA = new HashMap<>();
        validWeightsA.put("goals", BigDecimal.valueOf(30));
        validWeightsA.put("assists", BigDecimal.valueOf(25));
        validWeightsA.put("passSuccessPercentage", BigDecimal.valueOf(15));
        validWeightsA.put("aerialsWon", BigDecimal.valueOf(15));
        validWeightsA.put("timesManOfTheMatch", BigDecimal.valueOf(15));

        validWeightsB = new HashMap<>();
        validWeightsB.put("passSuccessPercentage", BigDecimal.valueOf(35));
        validWeightsB.put("aerialsWon", BigDecimal.valueOf(20));
        validWeightsB.put("minutesPlayed", BigDecimal.valueOf(20));
        validWeightsB.put("appearances", BigDecimal.valueOf(20));
    }

    @Test
    void primeraConfiguracionEsActual() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(strategyConfigRepository.findFirstByUserIdAndEstado(1L, StrategyStatus.ACTUAL)).thenReturn(Optional.empty());
        when(strategyConfigRepository.save(any(StrategyConfigEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        StrategyConfig req = new StrategyConfig(StrategyType.ESTRATEGIA_A, validWeightsA);
        StrategyState resp = service.configure(1L, req);

        assertEquals(StrategyStatus.ACTUAL, resp.getEstadoActual());
        assertEquals(StrategyType.ESTRATEGIA_A, resp.getTipoEstrategiaActual());
    }

    @Test
    void segundaConfiguracionEsPendiente() {
        StrategyConfigEntity current = new StrategyConfigEntity(user, StrategyType.ESTRATEGIA_A, mapper.write(validWeightsA), StrategyStatus.ACTUAL, Instant.now());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(strategyConfigRepository.findFirstByUserIdAndEstado(1L, StrategyStatus.ACTUAL)).thenReturn(Optional.of(current));
        when(strategyConfigRepository.findFirstByUserIdAndEstado(1L, StrategyStatus.PENDIENTE)).thenReturn(Optional.empty());
        when(strategyConfigRepository.save(any(StrategyConfigEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        StrategyConfig req = new StrategyConfig(StrategyType.ESTRATEGIA_B, validWeightsB);
        StrategyState resp = service.configure(1L, req);

        assertEquals(StrategyStatus.PENDIENTE, resp.getEstadoPendiente());
        assertEquals(StrategyType.ESTRATEGIA_B, resp.getTipoEstrategiaPendiente());
    }

    @Test
    void reemplazoDePendienteConservaSoloUnaPendiente() {
        StrategyConfigEntity current = new StrategyConfigEntity(user, StrategyType.ESTRATEGIA_A, mapper.write(validWeightsA), StrategyStatus.ACTUAL, Instant.now());
        StrategyConfigEntity existingPending = new StrategyConfigEntity(user, StrategyType.ESTRATEGIA_B, mapper.write(validWeightsB), StrategyStatus.PENDIENTE, Instant.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(strategyConfigRepository.findFirstByUserIdAndEstado(1L, StrategyStatus.ACTUAL)).thenReturn(Optional.of(current));
        when(strategyConfigRepository.findFirstByUserIdAndEstado(1L, StrategyStatus.PENDIENTE)).thenReturn(Optional.of(existingPending));
        when(strategyConfigRepository.save(any(StrategyConfigEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        StrategyConfig req = new StrategyConfig(StrategyType.ESTRATEGIA_A, validWeightsA);
        StrategyState resp = service.configure(1L, req);

        verify(strategyConfigRepository).delete(existingPending);
        assertEquals(StrategyStatus.PENDIENTE, resp.getEstadoPendiente());
    }
}
