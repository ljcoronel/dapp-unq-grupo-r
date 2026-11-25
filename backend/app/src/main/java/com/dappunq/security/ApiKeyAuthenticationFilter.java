package com.dappunq.security;

import com.dappunq.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final Pattern API_KEY_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{16,128}$");

    private final ApiKeyService apiKeyService;

    public ApiKeyAuthenticationFilter(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String requestPath = request.getRequestURI();
        if (requestPath == null || requestPath.isBlank()) {
            requestPath = request.getServletPath();
        }

        return requestPath == null
                || (!requestPath.endsWith("/scrape") && !requestPath.endsWith("/recalculate"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (isRecalculateRequest(request)) {
            String correlationId = correlationId(request);
            try {
                MDC.put("correlationId", correlationId);
                authorizeRecalculateRequest(request, response, filterChain, correlationId);
            } finally {
                SecurityContextHolder.clearContext();
                MDC.remove("correlationId");
            }
            return;
        }

        String apiKey = request.getHeader("X-API-Key");

        if (apiKey == null || apiKey.isBlank()) {
            denyAccess(response);
            return;
        }

        String normalizedKey = apiKey.trim();
        if (!API_KEY_PATTERN.matcher(normalizedKey).matches() || !apiKeyService.isValidScrapingApiKey(normalizedKey)) {
            denyAccess(response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "scraping-api-key",
                null,
                List.of(new SimpleGrantedAuthority("scraping"))
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private void authorizeRecalculateRequest(HttpServletRequest request,
                                             HttpServletResponse response,
                                             FilterChain filterChain,
                                             String correlationId) throws ServletException, IOException {
        ApiKeyService.RecalculateApiKeyResult result =
                apiKeyService.validateRecalculateApiKey(request.getHeader("X-API-Key"));

        switch (result) {
            case ACTIVE_CREDENTIAL_MISSING -> {
                logAttempt(request, correlationId, "CONFIGURACION_AUSENTE");
                denyRecalculate(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Error interno de autenticacion.");
            }
            case HEADER_MISSING -> {
                logAttempt(request, correlationId, "CLAVE_AUSENTE");
                denyRecalculate(response, HttpServletResponse.SC_UNAUTHORIZED, "No autorizado.");
            }
            case INVALID -> {
                logAttempt(request, correlationId, "CLAVE_INVALIDA");
                denyRecalculate(response, HttpServletResponse.SC_FORBIDDEN, "Acceso denegado.");
            }
            case VALID -> {
                logAttempt(request, correlationId, "AUTORIZADO");
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        "scraping-api-key",
                        null,
                        List.of(new SimpleGrantedAuthority("scraping"))
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
            }
        }
    }

    private boolean isRecalculateRequest(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        if (requestPath == null || requestPath.isBlank()) {
            requestPath = request.getServletPath();
        }
        return "POST".equalsIgnoreCase(request.getMethod())
                && requestPath != null
                && requestPath.endsWith("/recalculate");
    }

    private String correlationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return correlationId;
    }

    private void logAttempt(HttpServletRequest request, String correlationId, String result) {
        log.info("timestamp={}, ruta={}, resultado={}, correlationId={}",
                Instant.now(), request.getRequestURI(), result, correlationId);
    }

    private void denyRecalculate(HttpServletResponse response, int status, String message) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }

    private void denyAccess(HttpServletResponse response) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"No autorizado. Se requiere una API key válida para scraping.\"}");
    }
}
