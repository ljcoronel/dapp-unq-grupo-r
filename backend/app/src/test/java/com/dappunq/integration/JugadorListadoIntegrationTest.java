package com.dappunq.integration;

import com.dappunq.model.Jugador;
import com.dappunq.persistence.JugadorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class JugadorListadoIntegrationTest {
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
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private JugadorRepository jugadorRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        jugadorRepository.deleteAll();
        jugadorRepository.saveAll(List.of(
                new Jugador("player-1", "Kylian Mbappé", "La Liga", "Real Madrid", "Delantero"),
                new Jugador("player-2", "Vinícius Júnior", "La Liga", "Real Madrid", "Delantero"),
                new Jugador("player-3", "Rodri", "Premier League", "Manchester City", "Mediocampista")
        ));
    }

    @Test
    void shouldFilterPlayersByLigaEquipoAndPosicion() throws Exception {
        mockMvc.perform(get("/players")
                        .param("liga", "La Liga")
                        .param("equipo", "Real Madrid")
                        .param("posicion", "Delantero"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("player-1"))
                .andExpect(jsonPath("$[0].nombre").value("Kylian Mbappé"))
                .andExpect(jsonPath("$[0].liga").value("La Liga"))
                .andExpect(jsonPath("$[0].equipo").value("Real Madrid"));
    }
}
