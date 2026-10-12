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
}
