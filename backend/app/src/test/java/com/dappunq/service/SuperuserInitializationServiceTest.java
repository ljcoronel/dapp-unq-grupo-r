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

@ExtendWith(MockitoExtension.class)
class SuperuserInitializationServiceTest {

    @Mock
    private JpaUserEntityRepository userRepository;

    @Mock
    private StrategyConfigRepository strategyConfigRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final ValuationPersistenceMapper mapper = new ValuationPersistenceMapper();
    private SuperuserInitializationService suService;
    private UserEntity superuser;

    @BeforeEach
    void setUp() {
        suService = new SuperuserInitializationService(userRepository, strategyConfigRepository, mapper, passwordEncoder);
        superuser = new UserEntity(999L, "superuser", "hash999");
    }

    @Test
    void superuserInitializationServiceCreaSuperusuarioYEstrategiaAConPesosCorrectos() {
        when(userRepository.findByNombreIgnoreCase("superuser")).thenReturn(Optional.of(superuser));
        when(strategyConfigRepository.findFirstByUserIdAndEstado(999L, StrategyStatus.ACTUAL)).thenReturn(Optional.empty());
        when(strategyConfigRepository.save(any(StrategyConfigEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        StrategyConfigEntity config = suService.initializeSuperuserStrategy();

        assertNotNull(config);
        assertEquals(StrategyType.ESTRATEGIA_A, config.getEstrategia());
        assertEquals(StrategyStatus.ACTUAL, config.getEstado());

        Map<String, BigDecimal> weights = mapper.read(config.getPesos());
        assertEquals(BigDecimal.valueOf(30), weights.get("goals"));
        assertEquals(BigDecimal.valueOf(25), weights.get("assists"));
        assertEquals(BigDecimal.valueOf(15), weights.get("passSuccessPercentage"));
        assertEquals(BigDecimal.valueOf(15), weights.get("aerialsWon"));
        assertEquals(BigDecimal.valueOf(15), weights.get("timesManOfTheMatch"));
    }
}
