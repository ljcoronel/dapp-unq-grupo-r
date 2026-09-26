package com.dappunq.integration;

import com.dappunq.config.LeagueCatalogProperties;
import com.dappunq.dto.footballdata.FootballDataResponse;
import com.dappunq.dto.footballdata.PlayerPayload;
import com.dappunq.dto.footballdata.ScorerPayload;
import com.dappunq.dto.footballdata.TeamPayload;
import com.dappunq.persistence.LeagueEntity;
import com.dappunq.persistence.LeagueRepository;
import com.dappunq.service.FootballDataClient;
import com.dappunq.service.PlayerCatalogRefreshService;
import com.dappunq.persistence.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DataJpaTest
@Testcontainers
class PlayerCatalogRefreshIntegrationTest {
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

    private FootballDataClient client;
    private PlayerCatalogRefreshService refreshService;

    @BeforeEach
    void setUp() {
        leagueRepository.deleteAll();
        client = mock(FootballDataClient.class);
        refreshService = new PlayerCatalogRefreshService(client, leagueRepository);
    }

    @Test
    void preservesSnapshotWhenRefreshFails() {
        var definition = league("PL");
        var existing = new LeagueEntity("PL", "Premier League", "Inglaterra");
        existing.addPlayer(player(1, "Anterior"));
        leagueRepository.saveAndFlush(existing);
        when(client.fetchScorers("PL")).thenThrow(new RuntimeException("provider unavailable"));

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> refreshService.refreshLeague(definition));

        assertThat(leagueRepository.findByCode("PL").orElseThrow().getPlayers())
                .extracting(player -> player.getName()).containsExactly("Anterior");
    }

    @Test
    void preservesSnapshotWhenResponseIsEmpty() {
        var definition = league("PL");
        var existing = new LeagueEntity("PL", "Premier League", "Inglaterra");
        existing.addPlayer(player(1, "Anterior"));
        leagueRepository.saveAndFlush(existing);
        when(client.fetchScorers("PL")).thenReturn(new FootballDataResponse(null, List.of()));

        refreshService.refreshLeague(definition);

        assertThat(leagueRepository.findByCode("PL").orElseThrow().getPlayers())
                .extracting(player -> player.getName()).containsExactly("Anterior");
    }

    @Test
    void replacesNonEmptySnapshotAndDeletesObsoletePlayers() {
        var definition = league("PL");
        var existing = new LeagueEntity("PL", "Premier League", "Inglaterra");
        existing.addPlayer(player(1, "Obsoleto"));
        existing.addPlayer(player(2, "Conservado"));
        leagueRepository.saveAndFlush(existing);
        when(client.fetchScorers("PL")).thenReturn(new FootballDataResponse(null, List.of(
                scorer(2, "Conservado"),
                scorer(3, "Nuevo"))));

        refreshService.refreshLeague(definition);

        var refreshed = leagueRepository.findByCode("PL").orElseThrow();
        assertThat(refreshed.getPlayers()).extracting(player -> player.getId())
                .containsExactly(2, 3);
        assertThat(playerRepository.findById(1)).isEmpty();
    }

    @Test
    void limitsReplacementToTenValidPlayers() {
        var definition = league("PL");
        var scorers = java.util.stream.IntStream.rangeClosed(1, 11)
                .mapToObj(id -> scorer(id, "Jugador " + id))
                .toList();
        when(client.fetchScorers("PL")).thenReturn(new FootballDataResponse(null, scorers));

        refreshService.refreshLeague(definition);

        assertThat(leagueRepository.findByCode("PL").orElseThrow().getPlayers())
                .hasSize(10)
                .extracting(player -> player.getId())
                .containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    private static LeagueCatalogProperties.LeagueDefinition league(String code) {
        return LeagueCatalogProperties.findByCode(code);
    }

    private static com.dappunq.persistence.PlayerEntity player(int id, String name) {
        return new com.dappunq.persistence.PlayerEntity(id, name, null, "Equipo",
                1, 1, 0, null);
    }

    private static ScorerPayload scorer(int id, String name) {
        return new ScorerPayload(new PlayerPayload(id, name, "Delantero"),
                new TeamPayload("Equipo"), 1, 1, 0, null);
    }
}
