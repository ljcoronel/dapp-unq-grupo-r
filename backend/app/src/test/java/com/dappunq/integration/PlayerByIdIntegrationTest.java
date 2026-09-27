package com.dappunq.integration;

import com.dappunq.persistence.LeagueEntity;
import com.dappunq.persistence.LeagueRepository;
import com.dappunq.persistence.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class PlayerByIdIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    void findsPlayerByExternalNonGeneratedPrimaryKey() {
        var league = new LeagueEntity("PL", "Premier League", "Inglaterra");
        league.addPlayer(new com.dappunq.persistence.PlayerEntity(
                987654, "Jugador", "Delantero", "Equipo", 1, 0, null, null));
        leagueRepository.saveAndFlush(league);

        var player = playerRepository.findById(987654);

        assertThat(player).isPresent();
        assertThat(player.orElseThrow().getId()).isEqualTo(987654);
        assertThat(player.orElseThrow().getLeague().getCode()).isEqualTo("PL");
    }
}
