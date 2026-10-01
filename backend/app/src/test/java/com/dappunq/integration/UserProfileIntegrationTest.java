package com.dappunq.integration;

import com.dappunq.model.User;
import com.dappunq.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class UserProfileIntegrationTest {
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
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void shouldReturnUserProfileWhenUserExists() throws Exception {
        User saved = userRepository.save(new User("perfilUsuario", passwordEncoder.encode("secret")));
        Long id = saved.getId();

        mockMvc.perform(get("/users/" + id + "/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("perfilUsuario"));
    }

    @Test
    void shouldReturnNormalizedNameThroughPersistenceBoundary() throws Exception {
        // Whitespace is trimmed by the domain model; verify the full stack preserves this
        User saved = userRepository.save(new User("  normalizado  ", passwordEncoder.encode("secret")));
        Long id = saved.getId();

        mockMvc.perform(get("/users/" + id + "/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("normalizado"));
    }

    @Test
    void shouldReturn404WhenUserDoesNotExist() throws Exception {
        mockMvc.perform(get("/users/999999/"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Usuario no encontrado")));
    }
}
