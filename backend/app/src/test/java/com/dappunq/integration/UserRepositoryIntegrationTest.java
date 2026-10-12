package com.dappunq.integration;

import com.dappunq.model.User;
import com.dappunq.persistence.JpaUserEntityRepository;
import com.dappunq.persistence.UserEntity;
import com.dappunq.persistence.UserMapper;
import com.dappunq.persistence.UserRepository;
import com.dappunq.persistence.UserRepositoryImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
@Import({UserRepositoryImpl.class, UserMapper.class})
class UserRepositoryIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JpaUserEntityRepository jpaUserEntityRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Should save a new User, assign database ID, and retrieve it by ID")
    void shouldSaveAndRetrieveNewUser() {
        User user = new User("juanPerez", "hashedPassword123");

        User saved = userRepository.save(user);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull().isPositive();
        assertThat(user.getId()).isEqualTo(saved.getId());
        assertThat(saved.getNombre()).isEqualTo("juanPerez");
        assertThat(saved.getPasswordHash()).isEqualTo("hashedPassword123");

        Optional<User> found = userRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getNombre()).isEqualTo("juanPerez");
        assertThat(found.get().getPasswordHash()).isEqualTo("hashedPassword123");
    }

    @Test
    @DisplayName("Should find user by nombre case-insensitively and handle whitespace")
    void shouldFindUserByNombreIgnoreCase() {
        User user = new User("MariaLopez", "passHash456");
        userRepository.save(user);
        entityManager.flush();

        Optional<User> exactMatch = userRepository.findByNombreIgnoreCase("MariaLopez");
        Optional<User> lowerMatch = userRepository.findByNombreIgnoreCase("marialopez");
        Optional<User> upperMatch = userRepository.findByNombreIgnoreCase("MARIALOPEZ");
        Optional<User> paddedMatch = userRepository.findByNombreIgnoreCase("  marialopez  ");

        assertThat(exactMatch).isPresent();
        assertThat(lowerMatch).isPresent();
        assertThat(upperMatch).isPresent();
        assertThat(paddedMatch).isPresent();
        assertThat(lowerMatch.get().getNombre()).isEqualTo("MariaLopez");
    }

    @Test
    @DisplayName("Should return empty optional when finding by unknown id or non-existent name")
    void shouldReturnEmptyWhenNotFound() {
        assertThat(userRepository.findById(999999L)).isEmpty();
        assertThat(userRepository.findById(null)).isEmpty();
        assertThat(userRepository.findByNombreIgnoreCase("noExiste")).isEmpty();
        assertThat(userRepository.findByNombreIgnoreCase(null)).isEmpty();
        assertThat(userRepository.findByNombreIgnoreCase("   ")).isEmpty();
    }

    @Test
    @DisplayName("Should preserve persistence-only metadata (createdAt) on UserEntity without leaking to domain")
    void shouldPreservePersistenceMetadataOnUserEntityWithoutLeakingToDomain() {
        LocalDateTime beforeSave = LocalDateTime.now().minusSeconds(1);

        User saved = userRepository.save(new User("metaUser", "metaHash"));
        entityManager.flush();

        UserEntity entity = jpaUserEntityRepository.findById(saved.getId()).orElseThrow();
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getCreatedAt()).isAfterOrEqualTo(beforeSave);
        assertThat(entity.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now().plusSeconds(1));
        assertThat(entity.getNombre()).isEqualTo("metaUser");
        assertThat(entity.getPasswordHash()).isEqualTo("metaHash");

        // The domain User has no reference to createdAt and no persistence annotations
        assertThat(saved.getClass().getAnnotations()).noneMatch(a -> a.annotationType().getName().startsWith("jakarta.persistence"));
    }

    @Test
    @DisplayName("Should update existing user and preserve original createdAt")
    void shouldUpdateExistingUserAndPreserveCreatedAt() {
        User saved = userRepository.save(new User("userBeforeUpdate", "oldHash"));
        entityManager.flush();

        UserEntity initialEntity = jpaUserEntityRepository.findById(saved.getId()).orElseThrow();
        LocalDateTime originalCreatedAt = initialEntity.getCreatedAt();
        assertThat(originalCreatedAt).isNotNull();

        saved.setNombre("userAfterUpdate");
        saved.setPasswordHash("newHash");
        User updated = userRepository.save(saved);
        entityManager.flush();

        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getNombre()).isEqualTo("userAfterUpdate");
        assertThat(updated.getPasswordHash()).isEqualTo("newHash");

        UserEntity reloadedEntity = jpaUserEntityRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloadedEntity.getNombre()).isEqualTo("userAfterUpdate");
        assertThat(reloadedEntity.getPasswordHash()).isEqualTo("newHash");
        assertThat(reloadedEntity.getCreatedAt()).isEqualTo(originalCreatedAt);
    }

    @Test
    @DisplayName("Should reject duplicate username with DataIntegrityViolationException")
    void shouldRejectDuplicateUsername() {
        userRepository.save(new User("uniqueUser", "hash1"));
        entityManager.flush();

        assertThatThrownBy(() -> userRepository.save(new User("uniqueUser", "hash2")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when saving null user")
    void shouldThrowWhenSavingNullUser() {
        assertThatThrownBy(() -> userRepository.save(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
