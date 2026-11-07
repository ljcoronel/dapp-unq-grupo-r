package com.dappunq.e2e;

import com.dappunq.model.User;
import com.dappunq.model.valuation.StrategyStatus;
import com.dappunq.model.valuation.StrategyType;
import com.dappunq.persistence.HoldingEntity;
import com.dappunq.persistence.HoldingRepository;
import com.dappunq.persistence.JpaUserEntityRepository;
import com.dappunq.persistence.LeagueEntity;
import com.dappunq.persistence.LeagueRepository;
import com.dappunq.persistence.PlayerEntity;
import com.dappunq.persistence.PlayerRepository;
import com.dappunq.persistence.StrategyConfigEntity;
import com.dappunq.persistence.StrategyConfigRepository;
import com.dappunq.persistence.TeamEntity;
import com.dappunq.persistence.TeamRepository;
import com.dappunq.persistence.UserEntity;
import com.dappunq.persistence.ValuationHistoryRepository;
import com.dappunq.persistence.ValuationPersistenceMapper;
import com.dappunq.persistence.ValuationRepository;
import com.dappunq.security.JwtService;
import com.dappunq.security.UserPrincipal;
import com.dappunq.service.WhoScoredScrapingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class HoldingsInvariantE2ETest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        postgres.start();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @MockitoBean
    private WhoScoredScrapingService scrapingService;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JpaUserEntityRepository userRepository;

    @Autowired
    private StrategyConfigRepository strategyConfigRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private ValuationRepository valuationRepository;

    @Autowired
    private ValuationHistoryRepository valuationHistoryRepository;

    @Autowired
    private ValuationPersistenceMapper mapper;

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

        valuationHistoryRepository.deleteAll();
        valuationRepository.deleteAll();
        holdingRepository.deleteAll();
        strategyConfigRepository.deleteAll();
        playerRepository.deleteAll();
        teamRepository.deleteAll();
        leagueRepository.deleteAll();
        userRepository.deleteAll();

        doNothing().when(scrapingService).scrape(anyString());

        user = userRepository.save(new UserEntity("invariant_user", passwordEncoder.encode("secret")));
        token = "Bearer " + jwtService.generateToken(new UserPrincipal(new User(user.getId(), user.getNombre(), user.getPasswordHash())));

        TeamEntity team = teamRepository.save(new TeamEntity(1L, "Team Test"));
        LeagueEntity league = leagueRepository.save(new LeagueEntity(1L, "League Test", "Region Test"));
        PlayerEntity player = playerRepository.save(new PlayerEntity(1L, "Player Test", team, league, 24, 180, 75, "Defender", 10, 900, 2, 1, 0, 0, 1.5f, 85f, 3f, 1));

        Map<String, BigDecimal> weightsA = new HashMap<>();
        weightsA.put("goals", BigDecimal.valueOf(30));
        weightsA.put("assists", BigDecimal.valueOf(25));
        weightsA.put("passSuccessPercentage", BigDecimal.valueOf(15));
        weightsA.put("aerialsWon", BigDecimal.valueOf(15));
        weightsA.put("timesManOfTheMatch", BigDecimal.valueOf(15));

        strategyConfigRepository.save(new StrategyConfigEntity(user, StrategyType.ESTRATEGIA_A, mapper.write(weightsA), StrategyStatus.ACTUAL, Instant.now()));
        holdingRepository.save(new HoldingEntity(user, player, 100));
    }

    @Test
    void recalcularConservaCantidadDeTokensEInvariantesDeValor() throws Exception {
        // Primera ejecución de recalculo
        mockMvc.perform(post("/quotes/recalculate"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/" + user.getId() + "/holdings")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tokens").value(100))
                .andExpect(jsonPath("$[0].valorUnitario").exists())
                .andExpect(jsonPath("$[0].valorTotal").exists());

        // Segunda ejecución de recalculo: la cantidad de tokens sigue siendo exactamente 100
        mockMvc.perform(post("/quotes/recalculate"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/" + user.getId() + "/holdings")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tokens").value(100));
    }
}
