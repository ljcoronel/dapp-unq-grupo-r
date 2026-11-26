package com.dappunq.security;

import com.dappunq.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiKeyAuthenticationFilterTest {
    @Mock
    private ApiKeyService apiKeyService;

    private ApiKeyAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        filter = new ApiKeyAuthenticationFilter(apiKeyService);
    }

    @Test
    void shouldAuthenticateValidScrapingKey() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/scrape");
        request.addHeader("X-API-Key", "validScrapingKey-1234567890");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        when(apiKeyService.isValidScrapingApiKey("validScrapingKey-1234567890")).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .contains("scraping");
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void shouldRejectMissingOrMalformedScrapingKey() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/scrape");
        request.addHeader("X-API-Key", "  ");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldAuthenticateValidRecalculateKeyWithoutJwt() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/quotes/recalculate");
        request.addHeader("X-API-Key", "validScrapingKey-1234567890");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        when(apiKeyService.validateRecalculateApiKey("validScrapingKey-1234567890"))
                .thenReturn(ApiKeyService.RecalculateApiKeyResult.VALID);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void shouldRejectMissingRecalculateKeyWith401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/quotes/recalculate");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        when(apiKeyService.validateRecalculateApiKey(null))
                .thenReturn(ApiKeyService.RecalculateApiKeyResult.HEADER_MISSING);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("No autorizado");
    }

    @Test
    void shouldRejectInvalidRecalculateKeyWith403() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/quotes/recalculate");
        request.addHeader("X-API-Key", "Invalid-Key");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        when(apiKeyService.validateRecalculateApiKey("Invalid-Key"))
                .thenReturn(ApiKeyService.RecalculateApiKeyResult.INVALID);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("Acceso denegado");
    }

    @Test
    void shouldRejectMissingRecalculateConfigurationWith500() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/quotes/recalculate");
        request.addHeader("X-API-Key", "validScrapingKey-1234567890");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        when(apiKeyService.validateRecalculateApiKey("validScrapingKey-1234567890"))
                .thenReturn(ApiKeyService.RecalculateApiKeyResult.ACTIVE_CREDENTIAL_MISSING);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getContentAsString()).contains("Error interno");
    }

    @Test
    void shouldSkipOnlyExactPublicScrapePost() {
        MockHttpServletRequest scrape = new MockHttpServletRequest("POST", "/quotes/scrape");
        MockHttpServletRequest scrapeGet = new MockHttpServletRequest("GET", "/quotes/scrape");
        MockHttpServletRequest scrapeOther = new MockHttpServletRequest("POST", "/quotes/scrape/other");
        MockHttpServletRequest recalculate = new MockHttpServletRequest("POST", "/quotes/recalculate");

        assertThat(filter.shouldNotFilter(scrape)).isTrue();
        assertThat(filter.shouldNotFilter(scrapeGet)).isTrue();
        assertThat(filter.shouldNotFilter(scrapeOther)).isFalse();
        assertThat(filter.shouldNotFilter(recalculate)).isFalse();
    }
}
