package com.dappunq.e2e;

import com.dappunq.service.ApiKeyService;
import com.dappunq.service.WhoScoredScrapingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ScrapingApiKeyE2ETest {
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
    private ApiKeyService apiKeyService;

    @MockitoBean
    private WhoScoredScrapingService whoScoredScrapingService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        apiKeyService.rotateScrapingApiKey();
    }

    @Test
    void shouldAllowScrapingWithoutApiKey() throws Exception {
        mockMvc.perform(post("/quotes/scrape")
                        .param("numberOfPlayers", "1"))
                .andExpect(status().isOk());

        verify(whoScoredScrapingService).scrape("1");
    }

    @Test
    void shouldIgnoreAnyApiKeyValue() throws Exception {
        String apiKey = apiKeyService.rotateScrapingApiKey();

        for (String value : new String[]{apiKey, "bad-key", "arbitrary", ""}) {
            mockMvc.perform(post("/quotes/scrape")
                            .param("numberOfPlayers", "1")
                            .header("X-API-Key", value))
                    .andExpect(status().isOk());
        }

        verify(whoScoredScrapingService, times(4)).scrape("1");
    }

    @Test
    void shouldUseDefaultWhenNumberOfPlayersIsMissing() throws Exception {
        mockMvc.perform(post("/quotes/scrape"))
                .andExpect(status().isOk());

        verify(whoScoredScrapingService).scrape("50");
    }

    @Test
    void shouldReturn400AndNotScrapeForInvalidNumberOfPlayers() throws Exception {
        for (String value : new String[]{"texto", "0", "-1"}) {
            mockMvc.perform(post("/quotes/scrape")
                            .param("numberOfPlayers", value))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(whoScoredScrapingService);
    }

    @Test
    void shouldScrapeForPositiveNumberOfPlayers() throws Exception {
        mockMvc.perform(post("/quotes/scrape")
                        .param("numberOfPlayers", "25"))
                .andExpect(status().isOk());

        verify(whoScoredScrapingService).scrape("25");
    }
}
