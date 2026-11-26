package com.dappunq.integration;

import com.dappunq.adapter.RankingRepositoryAdapter;
import com.dappunq.model.ranking.RankingFiltros;
import com.dappunq.model.valuation.ExecutionType;
import com.dappunq.model.valuation.StrategyStatus;
import com.dappunq.model.valuation.StrategyType;
import com.dappunq.persistence.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class RankingRepositoryAdapterIntegrationTest {
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        postgres.start();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired JpaUserEntityRepository users;
    @Autowired LeagueRepository leagues;
    @Autowired TeamRepository teams;
    @Autowired PlayerRepository players;
    @Autowired HoldingRepository holdings;
    @Autowired ValuationRepository valuations;
    @Autowired StrategyConfigRepository configurations;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void consolidaVigentesAplicaFallbackFiltraYPagina() {
        jdbcTemplate.update("DELETE FROM tenencias WHERE jugador_id IN (SELECT id FROM players WHERE external_id = 700)");
        jdbcTemplate.update("DELETE FROM valuaciones WHERE jugador_id IN (SELECT id FROM players WHERE external_id = 700)");
        jdbcTemplate.update("DELETE FROM players WHERE external_id = 700");
        var superusuario = users.findByNombreIgnoreCase("superuser")
            .orElseGet(() -> users.save(new UserEntity("superuser", "hash")));
        var inversor = users.findByNombreIgnoreCase("ranking-inversor")
            .orElseGet(() -> users.save(new UserEntity("ranking-inversor", "hash")));
        var inversorB = users.findByNombreIgnoreCase("ranking-inversor-b")
            .orElseGet(() -> users.save(new UserEntity("ranking-inversor-b", "hash")));
        var liga = leagues.findByExternalId(2L)
            .orElseGet(() -> leagues.save(new LeagueEntity(2L, "Premier League", "Inglaterra")));
        var equipo = teams.findByExternalId(10L)
            .orElseGet(() -> teams.save(new TeamEntity(10L, "Arsenal")));
        var jugador = players.findByExternalId(700L)
            .orElseGet(() -> players.save(new PlayerEntity(700L, "Jugador Ranking", equipo, liga, 25, 180,
                75, "Forward", 10, 800, 5, 2, 1, 0, 2f, 80f, 3f, 1)));

        holdings.save(new HoldingEntity(superusuario, jugador, 10, BigDecimal.valueOf(8.50)));
        holdings.save(new HoldingEntity(inversor, jugador, 25));

        configurations.save(new StrategyConfigEntity(inversor, StrategyType.ESTRATEGIA_A,
            "{}", StrategyStatus.ACTUAL, Instant.now()));
        configurations.save(new StrategyConfigEntity(inversorB, StrategyType.ESTRATEGIA_B,
            "{}", StrategyStatus.ACTUAL, Instant.now()));
        valuations.save(new ValuationEntity(inversor, jugador, BigDecimal.valueOf(12.50),
            StrategyType.ESTRATEGIA_A, ExecutionType.MANUAL));
        valuations.save(new ValuationEntity(inversorB, jugador, BigDecimal.valueOf(11.00),
            StrategyType.ESTRATEGIA_B, ExecutionType.MANUAL));

        var adapter = new RankingRepositoryAdapter(jdbcTemplate);
        var pagina = adapter.buscar(new RankingFiltros("Premier League", "Arsenal", "Forward", 1, 20));

        assertEquals(1, pagina.total());
        assertEquals(1, pagina.items().size());
        assertEquals("ESTRATEGIA_A", pagina.items().getFirst().origen());
        assertEquals(0, BigDecimal.valueOf(12.50).compareTo(pagina.items().getFirst().valorRepresentativo()));

        var vacia = adapter.buscar(new RankingFiltros("No Existe", null, null, 1, 20));
        assertEquals(0, vacia.total());
        assertEquals(0, vacia.items().size());
    }
}
