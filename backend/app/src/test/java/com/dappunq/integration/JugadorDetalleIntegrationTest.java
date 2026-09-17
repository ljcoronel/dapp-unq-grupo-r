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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class JugadorDetalleIntegrationTest {
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
        jugadorRepository.save(new Jugador("player-1", "Kylian Mbappé", "La Liga", "Real Madrid", "Delantero"));
    }

    @Test
    void shouldReturnPlayerDetailsById() throws Exception {
        mockMvc.perform(get("/players/{id}", "player-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("player-1"))
                .andExpect(jsonPath("$.nombre").value("Kylian Mbappé"))
                .andExpect(jsonPath("$.equipo").value("Real Madrid"));
    }

    @Test
    void shouldReturnNotFoundWhenPlayerDoesNotExist() throws Exception {
        mockMvc.perform(get("/players/{id}", "missing-player"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Jugador no encontrado: missing-player"));
    }
}
