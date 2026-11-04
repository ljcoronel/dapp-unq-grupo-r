package com.dappunq.integration;

import com.dappunq.model.valuation.StrategyStatus;
import com.dappunq.persistence.JpaUserEntityRepository;
import com.dappunq.persistence.StrategyConfigRepository;
import com.dappunq.service.SuperuserInitializationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class SuperuserStartupIntegrationTest {

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
    private SuperuserInitializationService initializationService;

    @Autowired
    private JpaUserEntityRepository userRepository;

    @Autowired
    private StrategyConfigRepository strategyRepository;

    @Test
    void reiniciosSucesivosMantienenUnaEstrategiaActual() {
        initializationService.initializeSuperuser();
        initializationService.initializeSuperuser();

        Long superuserId = userRepository.findByNombreIgnoreCase("superuser").orElseThrow().getId();
        assertEquals(1, strategyRepository.findAllByUserId(superuserId).stream()
                .filter(strategy -> strategy.getEstado() == StrategyStatus.ACTUAL)
                .count());
    }
}
