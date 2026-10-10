package com.dappunq.integration;

import com.dappunq.persistence.LeagueRepository;
import com.dappunq.persistence.TeamRepository;
import com.dappunq.persistence.PlayerRepository;
import com.dappunq.service.WhoScoredScrapingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
class WhoScoredScrapingServiceTest {

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
    private WhoScoredScrapingService scrapingService;

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    void shouldScrapeOnePlayerFromWhoScoredAndPersistTheCatalog() {
        scrapingService.scrape("1");

        assertThat(playerRepository.count()).isGreaterThanOrEqualTo(1);
        assertThat(teamRepository.count()).isGreaterThanOrEqualTo(1);
        assertThat(leagueRepository.count()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void shouldRejectInvalidNumberOfPlayersWithoutNavigatingToWhoScored() {
        WhoScoredScrapingService service = new WhoScoredScrapingService(
            org.mockito.Mockito.mock(LeagueRepository.class),
            org.mockito.Mockito.mock(TeamRepository.class),
            org.mockito.Mockito.mock(PlayerRepository.class)
        );

        assertThatThrownBy(() -> service.scrape("0"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.scrape("not-a-number"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.scrape(null))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
