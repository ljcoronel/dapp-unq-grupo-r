package com.dappunq.e2e;

import com.dappunq.model.User;
import com.dappunq.persistence.JpaUserEntityRepository;
import com.dappunq.persistence.StrategyConfigRepository;
import com.dappunq.persistence.UserEntity;
import com.dappunq.security.JwtService;
import com.dappunq.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class PendingStrategyE2ETest {

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
    private WebApplicationContext context;

    @Autowired
    private JpaUserEntityRepository userRepository;

    @Autowired
    private StrategyConfigRepository strategyConfigRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private MockMvc mockMvc;
    private UserEntity user;
    private String token;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        strategyConfigRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(new UserEntity("e2e_user", passwordEncoder.encode("secret")));
        token = "Bearer " + jwtService.generateToken(new UserPrincipal(new User(user.getId(), user.getNombre(), user.getPasswordHash())));
    }

    @Test
    void rechazoPorPesosInvalidosDevuelve400() throws Exception {
        String invalidJson = """
                {
                  "estrategia": "ESTRATEGIA_A",
                  "pesos": {
                    "goals": 50,
                    "assists": 50,
                    "passSuccessPercentage": 10
                  }
                }
                """;

        mockMvc.perform(put("/users/" + user.getId() + "/strategy")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void solicitudDePendienteSinActualDevuelve409() throws Exception {
        String pendingJson = """
                {
                  "estrategia": "ESTRATEGIA_A",
                  "pesos": {
                    "goals": 30,
                    "assists": 25,
                    "passSuccessPercentage": 15,
                    "aerialsWon": 15,
                    "timesManOfTheMatch": 15
                  },
                  "estado": "PENDIENTE"
                }
                """;

        mockMvc.perform(put("/users/" + user.getId() + "/strategy")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pendingJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("No existe estrategia actual para crear una pendiente"));
    }

    @Test
    void primerPutCreaActualYSegundoPutCreaPendiente() throws Exception {
        String firstStrategyJson = """
                {
                  "estrategia": "ESTRATEGIA_A",
                  "pesos": {
                    "goals": 30,
                    "assists": 25,
                    "passSuccessPercentage": 15,
                    "aerialsWon": 15,
                    "timesManOfTheMatch": 15
                  }
                }
                """;

        mockMvc.perform(put("/users/" + user.getId() + "/strategy")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstStrategyJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTUAL"))
                .andExpect(jsonPath("$.estrategia").value("ESTRATEGIA_A"));

        String secondStrategyJson = """
                {
                  "estrategia": "ESTRATEGIA_B",
                  "pesos": {
                    "passSuccessPercentage": 35,
                    "aerialsWon": 20,
                    "minutesPlayed": 20,
                    "appearances": 20
                  }
                }
                """;

        mockMvc.perform(put("/users/" + user.getId() + "/strategy")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondStrategyJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.estrategia").value("ESTRATEGIA_B"));

        mockMvc.perform(get("/users/" + user.getId() + "/strategy")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.actual.estrategia").value("ESTRATEGIA_A"))
                .andExpect(jsonPath("$.pendiente.estrategia").value("ESTRATEGIA_B"));
    }
}
