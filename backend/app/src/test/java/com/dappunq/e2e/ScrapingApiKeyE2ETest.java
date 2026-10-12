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
    void shouldAuthorizeValidScrapingApiKey() throws Exception {
        String apiKey = apiKeyService.rotateScrapingApiKey();

        mockMvc.perform(post("/scrape")
                        .param("numberOfPlayers", "1")
                        .header("X-API-Key", apiKey))
                .andExpect(status().isOk());

        verify(whoScoredScrapingService).scrape("1");
    }

    @Test
    void shouldRejectMissingOrInvalidScrapingApiKey() throws Exception {
        mockMvc.perform(post("/scrape")
                        .param("numberOfPlayers", "1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("No autorizado. Se requiere una API key válida para scraping."));

        mockMvc.perform(post("/scrape")
                        .param("numberOfPlayers", "1")
                        .header("X-API-Key", "bad-key"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("No autorizado. Se requiere una API key válida para scraping."));

        verifyNoInteractions(whoScoredScrapingService);
    }
}
