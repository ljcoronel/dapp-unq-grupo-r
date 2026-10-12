package com.dappunq.e2e;

import com.dappunq.model.User;
import com.dappunq.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class UserAuthE2ETest {
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
    void shouldSupportProfileLookupAndLoginFlow() throws Exception {
        // Save through the new domain repository boundary and capture the assigned ID
        User saved = userRepository.save(new User("usuarioE2E", passwordEncoder.encode("secret")));
        Long id = saved.getId();

        // Profile lookup returns the correct domain user through UserService → UserRepository → UserMapper
        mockMvc.perform(get("/users/" + id + "/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("usuarioE2E"));

        // Login returns the same name and a non-empty JWT Authorization header
        mockMvc.perform(post("/login")
                        .contentType("application/json")
                        .content("{\"nombre\":\"usuarioE2E\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, not(blankOrNullString())))
                .andExpect(jsonPath("$.nombre").value("usuarioE2E"));
    }

    @Test
    void shouldSupportFullRegistrationLoginProfileFlow() throws Exception {
        // Register a new user via /register endpoint
        mockMvc.perform(post("/register")
                        .contentType("application/json")
                        .content("{\"nombre\":\"e2eFlowUser\",\"password\":\"pass123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("e2eFlowUser"));

        // Login the registered user and confirm JWT returned
        mockMvc.perform(post("/login")
                        .contentType("application/json")
                        .content("{\"nombre\":\"e2eFlowUser\",\"password\":\"pass123\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, not(blankOrNullString())))
                .andExpect(jsonPath("$.nombre").value("e2eFlowUser"));
    }

    @Test
    void shouldReturnSameResponseForDuplicateRegistration() throws Exception {
        // First registration succeeds
        mockMvc.perform(post("/register")
                        .contentType("application/json")
                        .content("{\"nombre\":\"dupE2EUser\",\"password\":\"pass\"}"))
                .andExpect(status().isOk());

        // Second registration with same name returns 400 with Spanish error
        mockMvc.perform(post("/register")
                        .contentType("application/json")
                        .content("{\"nombre\":\"dupE2EUser\",\"password\":\"pass\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Usuario existente"));
    }
}
