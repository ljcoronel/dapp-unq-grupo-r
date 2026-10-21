package com.dappunq.integration;

import com.dappunq.dto.valuation.StrategyRequest;
import com.dappunq.dto.valuation.StrategyStateResponse;
import com.dappunq.model.valuation.StrategyStatus;
import com.dappunq.model.valuation.StrategyType;
import com.dappunq.persistence.JpaUserEntityRepository;
import com.dappunq.persistence.StrategyConfigEntity;
import com.dappunq.persistence.StrategyConfigRepository;
import com.dappunq.persistence.UserEntity;
import com.dappunq.service.StrategyConfigurationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class PendingStrategyIntegrationTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        postgres.start();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired
    private StrategyConfigurationService strategyService;

    @Autowired
    private StrategyConfigRepository strategyConfigRepository;

    @Autowired
    private JpaUserEntityRepository userRepository;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        strategyConfigRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(new UserEntity("pending_user", "password123"));
    }

    @Test
    void unicidadDePendienteYFechasDeVigencia() {
        Map<String, BigDecimal> weightsA = new HashMap<>();
        weightsA.put("goals", BigDecimal.valueOf(30));
        weightsA.put("assists", BigDecimal.valueOf(25));
        weightsA.put("passSuccessPercentage", BigDecimal.valueOf(15));
        weightsA.put("aerialsWon", BigDecimal.valueOf(15));
        weightsA.put("timesManOfTheMatch", BigDecimal.valueOf(15));

        // 1. Primera configuración -> ACTUAL
        StrategyStateResponse first = strategyService.configure(user.getId(), new StrategyRequest(StrategyType.ESTRATEGIA_A, weightsA));
        assertEquals(StrategyStatus.ACTUAL, first.estado());
        assertNotNull(first.vigenteDesde());

        Map<String, BigDecimal> weightsB = new HashMap<>();
        weightsB.put("passSuccessPercentage", BigDecimal.valueOf(35));
        weightsB.put("aerialsWon", BigDecimal.valueOf(20));
        weightsB.put("minutesPlayed", BigDecimal.valueOf(20));
        weightsB.put("appearances", BigDecimal.valueOf(20));

        // 2. Segunda configuración -> PENDIENTE
        StrategyStateResponse second = strategyService.configure(user.getId(), new StrategyRequest(StrategyType.ESTRATEGIA_B, weightsB));
        assertEquals(StrategyStatus.PENDIENTE, second.estado());
        assertNotNull(second.vigenteDesde());

        // 3. Tercera configuración -> Reemplaza PENDIENTE
        weightsA.put("goals", BigDecimal.valueOf(40));
        weightsA.put("assists", BigDecimal.valueOf(15));
        StrategyStateResponse third = strategyService.configure(user.getId(), new StrategyRequest(StrategyType.ESTRATEGIA_A, weightsA));
        assertEquals(StrategyStatus.PENDIENTE, third.estado());

        // Validar unicidad en base de datos: exactamente 1 ACTUAL y exactamente 1 PENDIENTE
        List<StrategyConfigEntity> allUserConfigs = strategyConfigRepository.findAllByUserId(user.getId());
        assertEquals(2, allUserConfigs.size());

        long pendingCount = allUserConfigs.stream().filter(c -> c.getEstado() == StrategyStatus.PENDIENTE).count();
        long actualCount = allUserConfigs.stream().filter(c -> c.getEstado() == StrategyStatus.ACTUAL).count();
        assertEquals(1, pendingCount);
        assertEquals(1, actualCount);

        StrategyConfigEntity currentPending = strategyConfigRepository.findFirstByUserIdAndEstado(user.getId(), StrategyStatus.PENDIENTE).orElseThrow();
        assertEquals(StrategyType.ESTRATEGIA_A, currentPending.getEstrategia());
    }
}
