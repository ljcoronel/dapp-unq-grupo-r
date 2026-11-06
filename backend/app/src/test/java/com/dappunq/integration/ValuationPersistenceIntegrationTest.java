package com.dappunq.integration;

import com.dappunq.persistence.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ValuationPersistenceIntegrationTest {
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        postgres.start();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired HoldingRepository holdingRepository;
    @Autowired JpaUserEntityRepository userRepository;
    @Autowired PlayerRepository playerRepository;
    @Autowired TeamRepository teamRepository;
    @Autowired LeagueRepository leagueRepository;

    @Test
    void persisteElValorAcumuladoYLoConservaEntreValuaciones() {
        UserEntity user = userRepository.save(new UserEntity("valuation-user", "hash"));
        TeamEntity team = teamRepository.save(new TeamEntity(1L, "Team"));
        LeagueEntity league = leagueRepository.save(new LeagueEntity(1L, "League", "Region"));
        PlayerEntity player = playerRepository.save(new PlayerEntity(1L, "Player", team, league, 1, 180,
                75, "Forward", 1, 100, 1, 1, 0, 0, 0f, 50f, 1f, 0));
        HoldingEntity holding = holdingRepository.save(new HoldingEntity(user, player, 100));

        holding.valuate(BigDecimal.valueOf(1.25));
        holdingRepository.saveAndFlush(holding);
        holding.valuate(holding.getValorUnitario().add(BigDecimal.valueOf(2)));
        holdingRepository.saveAndFlush(holding);

        HoldingEntity persisted = holdingRepository.findById(holding.getId()).orElseThrow();
        assertEquals(BigDecimal.valueOf(3.25).setScale(2), persisted.getValorUnitario());
        assertEquals(BigDecimal.valueOf(325.00).setScale(2), persisted.getValorTotal());
    }
}
