package com.dappunq.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {
    private static final String SECRET = "dapp-unq-grupo-r-secret-key-2026-09-08-must-be-at-least-256-bit-long";
    private final JwtService jwtService = new JwtService();

    @Test
    void shouldGenerateAndValidateTokenForUser() {
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3_600_000L);

        UserDetails user = User.withUsername("usuario1")
                .password("secret")
                .authorities(Collections.emptyList())
                .build();

        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("usuario1");
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void shouldIncludeNumericUserIdClaimForUserPrincipal() {
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3_600_000L);

        UserPrincipal principal = new UserPrincipal(
                new com.dappunq.model.User(42L, "usuario42", "encoded-secret"));

        String token = jwtService.generateToken(principal);
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo("usuario42");
        assertThat(claims.get("userId", Number.class).longValue()).isEqualTo(42L);
    }

    @Test
    void shouldRejectMalformedOrExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 0L);

        UserDetails user = User.withUsername("usuario2")
                .password("secret")
                .authorities(Collections.emptyList())
                .build();

        String expiredToken = jwtService.generateToken(user);
        assertThat(jwtService.isTokenValid("not-a-jwt", user)).isFalse();
        assertThat(jwtService.extractUsername("not-a-jwt")).isNull();
        assertThat(jwtService.isTokenValid(expiredToken, user)).isFalse();
    }
}
