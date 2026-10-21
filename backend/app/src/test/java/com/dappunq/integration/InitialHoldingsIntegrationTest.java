package com.dappunq.integration;

import com.dappunq.model.valuation.ValuationDomainException;
import com.dappunq.persistence.HoldingEntity;
import com.dappunq.persistence.HoldingRepository;
import com.dappunq.persistence.JpaUserEntityRepository;
import com.dappunq.persistence.LeagueEntity;
import com.dappunq.persistence.LeagueRepository;
import com.dappunq.persistence.PlayerEntity;
import com.dappunq.persistence.PlayerRepository;
import com.dappunq.persistence.TeamEntity;
import com.dappunq.persistence.TeamRepository;
import com.dappunq.persistence.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class InitialHoldingsIntegrationTest {

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
    private HoldingRepository holdingRepository;

    @Autowired
    private JpaUserEntityRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private LeagueRepository leagueRepository;

    private UserEntity user;
    private PlayerEntity player;

    @BeforeEach
    void setUp() {
        holdingRepository.deleteAll();
        playerRepository.deleteAll();
        teamRepository.deleteAll();
        leagueRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(new UserEntity("test_user_holding", "pass"));
        TeamEntity team = teamRepository.save(new TeamEntity(1L, "Team Test"));
        LeagueEntity league = leagueRepository.save(new LeagueEntity(1L, "League Test", "Region Test"));
        player = playerRepository.save(new PlayerEntity(1L, "Player Test", team, league, 22, 175, 70, "Midfielder", 5, 400, 1, 1, 0, 0, 1.2f, 82f, 1.5f, 0));
    }

    @Test
    void unicidadUsuarioJugadorImpideDuplicados() {
        holdingRepository.save(new HoldingEntity(user, player, 100));

        assertThrows(DataIntegrityViolationException.class, () -> {
            holdingRepository.saveAndFlush(new HoldingEntity(user, player, 100));
        });
    }

    @Test
    void formulaValorTotalMultiplicaTokensPorValorUnitarioConDosDecimales() {
        HoldingEntity holding = new HoldingEntity(user, player, 100);
        holding.valuate(BigDecimal.valueOf(12.3456));
        HoldingEntity saved = holdingRepository.saveAndFlush(holding);

        assertEquals(new BigDecimal("12.35"), saved.getValorUnitario());
        assertEquals(new BigDecimal("1235.00"), saved.getValorTotal());
    }

    @Test
    void tokensNegativosLanzaExcepcionDeDominio() {
        assertThrows(ValuationDomainException.class, () -> {
            new HoldingEntity(user, player, -10);
        });
    }
}
