package com.dappunq.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {
    private final JwtService jwtService = new JwtService();

    @Test
    void shouldGenerateAndValidateTokenForUser() {
        ReflectionTestUtils.setField(jwtService, "secret", "dapp-unq-grupo-r-secret-key-2026-09-08-must-be-at-least-256-bit-long");
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
    void shouldRejectMalformedOrExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "secret", "dapp-unq-grupo-r-secret-key-2026-09-08-must-be-at-least-256-bit-long");
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
