package com.dappunq.e2e;

import com.dappunq.model.valuation.StrategyStatus;
import com.dappunq.model.valuation.StrategyType;
import com.dappunq.model.User;
import com.dappunq.persistence.ApiKeyRepository;
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
import com.dappunq.persistence.order.JpaOrdenEntityRepository;
import com.dappunq.service.WhoScoredScrapingService;
import com.dappunq.security.JwtService;
import com.dappunq.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
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
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class OrderManagementControllerE2ETest {
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

    @Autowired private WebApplicationContext context;
    @Autowired private JpaUserEntityRepository users;
    @Autowired private StrategyConfigRepository strategies;
    @Autowired private PlayerRepository players;
    @Autowired private TeamRepository teams;
    @Autowired private LeagueRepository leagues;
    @Autowired private HoldingRepository holdings;
    @Autowired private JpaOrdenEntityRepository orders;
    @Autowired private ValuationRepository valuations;
    @Autowired private ValuationHistoryRepository valuationHistory;
    @Autowired private ApiKeyRepository apiKeys;
    @Autowired private ValuationPersistenceMapper mapper;
    @Autowired private JwtService jwtService;

    private MockMvc mockMvc;
    private UserEntity user;
    private PlayerEntity player;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        orders.deleteAll();
        valuationHistory.deleteAll();
        valuations.deleteAll();
        holdings.deleteAll();
        strategies.deleteAll();
        apiKeys.deleteAll();
        players.deleteAll();
        teams.deleteAll();
        leagues.deleteAll();
        users.deleteAll();

        user = users.save(new UserEntity("market_e2e", "password"));
        Map<String, BigDecimal> weights = new HashMap<>();
        weights.put("goals", BigDecimal.valueOf(30));
        weights.put("assists", BigDecimal.valueOf(25));
        weights.put("passSuccessPercentage", BigDecimal.valueOf(15));
        weights.put("aerialsWon", BigDecimal.valueOf(15));
        weights.put("timesManOfTheMatch", BigDecimal.valueOf(15));
        strategies.save(new StrategyConfigEntity(user, StrategyType.ESTRATEGIA_A,
                mapper.write(weights), StrategyStatus.ACTUAL, Instant.now()));
        TeamEntity team = teams.save(new TeamEntity(1L, "Market Team"));
        LeagueEntity league = leagues.save(new LeagueEntity(1L, "Market League", "Test"));
        player = players.save(new PlayerEntity(1L, "Market Player", team, league, 25, 180, 75,
                "Defender", 10, 900, 2, 1, 0, 0, 1.5f, 85f, 3f, 1));
    }

    @Test
    void endpointsPublicosPermitenCrearConsultarYCancelarConLiberacionDeReserva() throws Exception {
        String request = """
                {"usuarioId":%d,"playerId":%d,"quantity":2,"price":10.00,"tolerance":1.00}
                """.formatted(user.getId(), player.getId());

        mockMvc.perform(orderPost("/orders/buy", request, user))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDIENTE"))
                .andExpect(jsonPath("$.issuedAt").exists())
                .andExpect(jsonPath("$.executedAt").doesNotExist());

        var pending = orders.findAll().getFirst();
        mockMvc.perform(get("/users/" + user.getId() + "/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderId").value(pending.getId()));

        mockMvc.perform(delete("/orders/" + pending.getId()).param("usuarioId", "999999"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/users/" + user.getId() + "/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(delete("/orders/" + pending.getId()).param("usuarioId", user.getId().toString()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/users/" + user.getId() + "/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        org.junit.jupiter.api.Assertions.assertEquals(0,
                users.findById(user.getId()).orElseThrow().getCreditosDisponibles()
                        .compareTo(new BigDecimal("100.00")));
    }

    @Test
    void creacionDeOrdenRequiereJwtValidoYNoPermiteSuplantarUsuario() throws Exception {
        String request = """
                {"usuarioId":%d,"playerId":%d,"quantity":1,"price":10.00,"tolerance":0.00}
                """.formatted(user.getId(), player.getId());

        mockMvc.perform(post("/orders/buy").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/orders/buy")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(orderPost("/orders/buy", request.replace(
                        "\"usuarioId\":" + user.getId(), "\"usuarioId\":999999"), user))
                .andExpect(status().isForbidden());

        org.junit.jupiter.api.Assertions.assertTrue(orders.findAll().isEmpty());
    }

    @Test
    void endpointsDeVentaEHistorialSonPublicosYRechazanDatosInvalidos() throws Exception {
        UserEntity seller = users.save(new UserEntity("market_seller", "password"));
        Map<String, BigDecimal> weights = new HashMap<>();
        weights.put("goals", BigDecimal.valueOf(30));
        weights.put("assists", BigDecimal.valueOf(25));
        weights.put("passSuccessPercentage", BigDecimal.valueOf(15));
        weights.put("aerialsWon", BigDecimal.valueOf(15));
        weights.put("timesManOfTheMatch", BigDecimal.valueOf(15));
        strategies.save(new StrategyConfigEntity(seller, StrategyType.ESTRATEGIA_A,
                mapper.write(weights), StrategyStatus.ACTUAL, Instant.now()));
        holdings.save(new com.dappunq.persistence.HoldingEntity(seller, player, 5));

        String sellRequest = """
                {"usuarioId":%d,"playerId":%d,"quantity":2,"price":10.00,"tolerance":0.00}
                """.formatted(seller.getId(), player.getId());
        mockMvc.perform(orderPost("/orders/sell", sellRequest, seller))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDIENTE"));

        String invalidSell = """
                {"usuarioId":%d,"playerId":%d,"quantity":0,"price":10.00,"tolerance":0.00}
                """.formatted(seller.getId(), player.getId());
        mockMvc.perform(orderPost("/orders/sell", invalidSell, seller))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        String invalidBuy = """
                {"usuarioId":%d,"playerId":%d,"quantity":101,"price":10.00,"tolerance":0.00}
                """.formatted(user.getId(), player.getId());
        mockMvc.perform(orderPost("/orders/buy", invalidBuy, user))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        String buyRequest = """
                {"usuarioId":%d,"playerId":%d,"quantity":2,"price":10.00,"tolerance":0.00}
                """.formatted(user.getId(), player.getId());
        mockMvc.perform(orderPost("/orders/buy", buyRequest, user))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("EJECUTADA"))
                .andExpect(jsonPath("$.settledPrice").value(10.0))
                .andExpect(jsonPath("$.executedAt").exists());

        mockMvc.perform(get("/users/" + user.getId() + "/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].buyerId").value(user.getId()));
        mockMvc.perform(get("/users/" + seller.getId() + "/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sellerId").value(seller.getId()));
    }

    @Test
    void compraE2EConsumeLiquidezPrioritariaYQuedaPendienteAlAgotarse() throws Exception {
        UserEntity superuser = users.save(new UserEntity("superuser", "password"));
        Map<String, BigDecimal> weights = new HashMap<>();
        weights.put("goals", BigDecimal.valueOf(30));
        weights.put("assists", BigDecimal.valueOf(25));
        weights.put("passSuccessPercentage", BigDecimal.valueOf(15));
        weights.put("aerialsWon", BigDecimal.valueOf(15));
        weights.put("timesManOfTheMatch", BigDecimal.valueOf(15));
        strategies.save(new StrategyConfigEntity(superuser, StrategyType.ESTRATEGIA_A,
                mapper.write(weights), StrategyStatus.ACTUAL, Instant.now()));
        holdings.save(new com.dappunq.persistence.HoldingEntity(superuser, player, 2));
        String request = """
                {"usuarioId":%d,"playerId":%d,"quantity":2,"price":10.00,"tolerance":0.00}
                """.formatted(user.getId(), player.getId());

        mockMvc.perform(orderPost("/orders/buy", request, user))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("EJECUTADA"));
        mockMvc.perform(orderPost("/orders/buy", """
                {"usuarioId":%d,"playerId":%d,"quantity":1,"price":10.00,"tolerance":0.00}
                """.formatted(user.getId(), player.getId()), user))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDIENTE"));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder orderPost(
            String path, String content, UserEntity account) {
        return post(path)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.generateToken(
                        new UserPrincipal(new User(account.getId(), account.getNombre(), account.getPasswordHash()))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
    }
}
