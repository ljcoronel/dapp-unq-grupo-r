package com.dappunq.e2e;

import com.dappunq.persistence.LeagueEntity;
import com.dappunq.persistence.LeagueRepository;
import com.dappunq.persistence.PlayerEntity;
import com.dappunq.persistence.PlayerRepository;
import com.dappunq.persistence.TeamEntity;
import com.dappunq.persistence.TeamRepository;
import com.dappunq.persistence.ApiKeyEntity;
import com.dappunq.persistence.ApiKeyRepository;
import com.dappunq.service.ApiKeyService;
import com.dappunq.service.WhoScoredScrapingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class QuoteRecalculateE2ETest {

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
    private PlayerRepository playerRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    private MockMvc mockMvc;
    private String apiKey;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        apiKey = apiKeyService.rotateScrapingApiKey();
    }

    @Test
    void recalculoAutorizadoSinJwtExitosoDevuelve200() throws Exception {
        doNothing().when(scrapingService).scrape(anyString());

        TeamEntity team = teamRepository.save(new TeamEntity(1L, "Team Test"));
        LeagueEntity league = leagueRepository.save(new LeagueEntity(1L, "League Test", "Region Test"));
        playerRepository.save(new PlayerEntity(1L, "Player Test", team, league, 24, 180, 75, "Defender", 10, 900, 2, 1, 0, 0, 1.5f, 85f, 3f, 1));

        mockMvc.perform(post("/quotes/recalculate").header("X-API-Key", apiKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.executedAt").exists());
    }

    @Test
    void recalculoAutorizadoConJwtExitosoDevuelve200() throws Exception {
        doNothing().when(scrapingService).scrape(anyString());
        TeamEntity team = teamRepository.save(new TeamEntity(2L, "Team JWT"));
        LeagueEntity league = leagueRepository.save(new LeagueEntity(2L, "League JWT", "Region JWT"));
        playerRepository.save(new PlayerEntity(2L, "Player JWT", team, league, 24, 180, 75, "Defender", 10, 900, 2, 1, 0, 0, 1.5f, 85f, 3f, 1));
        mockMvc.perform(post("/quotes/recalculate")
                        .header("X-API-Key", apiKey)
                        .header("Authorization", "Bearer no-es-necesario"))
                .andExpect(status().isOk());
    }

    @Test
    void recalculoAutorizadoConErrorEnScrapingDevuelve503() throws Exception {
        doThrow(new RuntimeException("Simulated scraping failure")).when(scrapingService).scrape(anyString());

        mockMvc.perform(post("/quotes/recalculate").header("X-API-Key", apiKey))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void recalculoSinApiKeyDevuelve401SinInvocarScraping() throws Exception {
        mockMvc.perform(post("/quotes/recalculate"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
        verifyNoInteractions(scrapingService);
    }

    @Test
    void recalculoSoloConJwtDevuelve401() throws Exception {
        mockMvc.perform(post("/quotes/recalculate")
                        .header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(scrapingService);
    }

    @Test
    void recalculoConApiKeyIncorrectaDevuelve403() throws Exception {
        mockMvc.perform(post("/quotes/recalculate").header("X-API-Key", "X" + apiKey.substring(1)))
                .andExpect(status().isForbidden());
        verifyNoInteractions(scrapingService);
    }

    @Test
    void recalculoConMayusculasOEspaciosDevuelve403() throws Exception {
        mockMvc.perform(post("/quotes/recalculate").header("X-API-Key", apiKey.toUpperCase()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/quotes/recalculate").header("X-API-Key", " " + apiKey))
                .andExpect(status().isForbidden());
        verifyNoInteractions(scrapingService);
    }

    @Test
    void recalculoSinCredencialActivaDevuelve500() throws Exception {
        ApiKeyEntity credential = apiKeyRepository
                .findByPermissionAndActiveTrue(ApiKeyService.SCRAPING_PERMISSION)
                .orElseThrow();
        credential.setActive(false);
        apiKeyRepository.save(credential);

        mockMvc.perform(post("/quotes/recalculate").header("X-API-Key", apiKey))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").exists());
        verifyNoInteractions(scrapingService);
    }
}
