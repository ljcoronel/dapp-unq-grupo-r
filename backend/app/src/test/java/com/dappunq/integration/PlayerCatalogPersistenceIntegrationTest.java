package com.dappunq.integration;

import com.dappunq.persistence.LeagueEntity;
import com.dappunq.persistence.LeagueRepository;
import com.dappunq.persistence.PlayerEntity;
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
class PlayerCatalogPersistenceIntegrationTest {
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

    @Test
    void persistsPlayersInExplicitOrderAndReadsThemByLeague() {
        var league = new LeagueEntity("PL", "Premier League", "Inglaterra");
        league.addPlayer(new PlayerEntity(20, "Segundo", null, "Equipo", null, 2, null, null));
        league.addPlayer(new PlayerEntity(10, "Primero", null, "Equipo", null, 1, null, null));
        leagueRepository.saveAndFlush(league);

        var persisted = leagueRepository.findByCode("PL").orElseThrow();

        assertThat(persisted.getPlayers()).extracting(PlayerEntity::getName)
                .containsExactly("Segundo", "Primero");
    }
}
