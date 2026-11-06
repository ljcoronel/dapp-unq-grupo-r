package com.dappunq.service;

import com.dappunq.model.valuation.StrategyStatus;
import com.dappunq.model.valuation.StrategyType;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class SuperuserInitializationServiceTest {

    @Mock
    private JpaUserEntityRepository userRepository;

    @Mock
    private StrategyConfigRepository strategyConfigRepository;

    @Mock
    private HoldingsValuationService holdingsValuationService;

    @Mock
    private InitialHoldingsService initialHoldingsService;

    @Mock
    private PasswordEncoder passwordEncoder;


    private final ValuationPersistenceMapper mapper = new ValuationPersistenceMapper();
    private SuperuserInitializationService suService;
    private UserEntity superuser;

    @BeforeEach
    void setUp() {
        suService = new SuperuserInitializationService(userRepository, strategyConfigRepository, mapper, passwordEncoder, holdingsValuationService, null);
        superuser = new UserEntity(999L, "superuser", "hash999");
    }

    @Test
    void superuserInitializationServiceCreaSuperusuarioYEstrategiaAConPesosCorrectos() {
        when(userRepository.findByNombreIgnoreCase("superuser")).thenReturn(Optional.of(superuser));
        when(strategyConfigRepository.findFirstByUserIdAndEstado(999L, StrategyStatus.ACTUAL)).thenReturn(Optional.empty());
        when(strategyConfigRepository.save(any(StrategyConfigEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        StrategyConfigEntity config = suService.initializeSuperuserStrategy();

        assertNotNull(config);
        assertEquals(StrategyType.ESTRATEGIA_A, config.getTipoEstrategia());
        assertEquals(StrategyStatus.ACTUAL, config.getEstado());

        Map<String, BigDecimal> weights = mapper.read(config.getPesos());
        assertEquals(BigDecimal.valueOf(30), weights.get("goals"));
        assertEquals(BigDecimal.valueOf(25), weights.get("assists"));
        assertEquals(BigDecimal.valueOf(15), weights.get("passSuccessPercentage"));
        assertEquals(BigDecimal.valueOf(15), weights.get("aerialsWon"));
        assertEquals(BigDecimal.valueOf(15), weights.get("timesManOfTheMatch"));
    }

    @Test
    void initializeSuperuserEjecutaTenenciasEstrategiaYValuacionEnOrden() {
        when(userRepository.findByNombreIgnoreCase("superuser")).thenReturn(Optional.of(superuser));
        when(strategyConfigRepository.findFirstByUserIdAndEstado(999L, StrategyStatus.ACTUAL))
                .thenReturn(Optional.empty());
        when(strategyConfigRepository.save(any(StrategyConfigEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        suService = new SuperuserInitializationService(userRepository, strategyConfigRepository,
                mapper, passwordEncoder, holdingsValuationService, initialHoldingsService);

        suService.initializeSuperuser();

        var order = inOrder(initialHoldingsService, strategyConfigRepository, holdingsValuationService);
        order.verify(initialHoldingsService).initializeHoldingsForAllPlayers();
        order.verify(strategyConfigRepository).save(any(StrategyConfigEntity.class));
        order.verify(holdingsValuationService).revalue(org.mockito.ArgumentMatchers.eq(999L),
                any(StrategyConfigEntity.class),
                org.mockito.ArgumentMatchers.eq(com.dappunq.model.valuation.ExecutionType.MANUAL));
    }

    @Test
    void initializeSuperuserEsIdempotenteCuandoYaExisteEstrategiaActual() {
        StrategyConfigEntity existing = new StrategyConfigEntity(superuser, StrategyType.ESTRATEGIA_A,
                mapper.write(Map.of("goals", BigDecimal.valueOf(100))), StrategyStatus.ACTUAL, java.time.Instant.now());
        when(userRepository.findByNombreIgnoreCase("superuser")).thenReturn(Optional.of(superuser));
        when(strategyConfigRepository.findFirstByUserIdAndEstado(999L, StrategyStatus.ACTUAL))
                .thenReturn(Optional.of(existing));
        suService = new SuperuserInitializationService(userRepository, strategyConfigRepository,
                mapper, passwordEncoder, holdingsValuationService, initialHoldingsService);

        suService.initializeSuperuser();

        verify(holdingsValuationService, never()).revalue(any(), any(), any());
        verify(strategyConfigRepository, never()).save(any());
    }
}
