package com.dappunq.unit;

import com.dappunq.exception.InvalidUserDataException;
import com.dappunq.model.User;
import com.dappunq.persistence.UserEntity;
import com.dappunq.persistence.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Nested
    @DisplayName("Mapping to Entity (toEntity)")
    class ToEntityTests {

        @Test
        @DisplayName("Should return null when user is null")
        void shouldReturnNullWhenUserIsNull() {
            assertThat(userMapper.toEntity(null)).isNull();
        }

        @Test
        @DisplayName("Should map valid user with ID to UserEntity")
        void shouldMapUserWithIdToEntity() {
            User user = new User(1L, "usuarioTest", "hash123");

            UserEntity entity = userMapper.toEntity(user);

            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getNombre()).isEqualTo("usuarioTest");
            assertThat(entity.getPasswordHash()).isEqualTo("hash123");
            assertThat(entity.getCreatedAt()).isNull();
        }

        @Test
        @DisplayName("Should map valid user without ID to UserEntity")
        void shouldMapUserWithoutIdToEntity() {
            User user = new User("nuevoUsuario", "secretHash");

            UserEntity entity = userMapper.toEntity(user);

            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isNull();
            assertThat(entity.getNombre()).isEqualTo("nuevoUsuario");
            assertThat(entity.getPasswordHash()).isEqualTo("secretHash");
            assertThat(entity.getCreatedAt()).isNull();
        }

        @Test
        @DisplayName("Should preserve normalized nombre in entity")
        void shouldPreserveNormalizedNombreInEntity() {
            User user = new User("  usuarioConEspacios  ", "hash");

            UserEntity entity = userMapper.toEntity(user);

            assertThat(entity.getNombre()).isEqualTo("usuarioConEspacios");
        }
    }

    @Nested
    @DisplayName("Mapping to Domain (toDomain)")
    class ToDomainTests {

        @Test
        @DisplayName("Should return null when entity is null")
        void shouldReturnNullWhenEntityIsNull() {
            assertThat(userMapper.toDomain(null)).isNull();
        }

        @Test
        @DisplayName("Should map valid UserEntity to domain User and not expose createdAt")
        void shouldMapUserEntityToDomainUser() {
            LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
            UserEntity entity = new UserEntity(10L, "usuarioPersistido", "hashValido", createdAt);

            User user = userMapper.toDomain(entity);

            assertThat(user).isNotNull();
            assertThat(user.getId()).isEqualTo(10L);
            assertThat(user.getNombre()).isEqualTo("usuarioPersistido");
            assertThat(user.getPasswordHash()).isEqualTo("hashValido");
        }

        @Test
        @DisplayName("Should map UserEntity without ID to domain User")
        void shouldMapUserEntityWithoutIdToDomainUser() {
            UserEntity entity = new UserEntity(null, "usuarioSinId", "hashValido");

            User user = userMapper.toDomain(entity);

            assertThat(user).isNotNull();
            assertThat(user.getId()).isNull();
            assertThat(user.getNombre()).isEqualTo("usuarioSinId");
            assertThat(user.getPasswordHash()).isEqualTo("hashValido");
        }

        @Test
        @DisplayName("Should normalize surrounding whitespace from entity nombre")
        void shouldNormalizeSurroundingWhitespaceFromEntityNombre() {
            UserEntity entity = new UserEntity(2L, "  nombreConEspacios  ", "hashValido");

            User user = userMapper.toDomain(entity);

            assertThat(user.getNombre()).isEqualTo("nombreConEspacios");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("Should enforce domain invariant and reject corrupted entity with blank or null nombre")
        void shouldRejectCorruptedEntityWithBlankOrNullNombre(String invalidNombre) {
            UserEntity entity = new UserEntity(1L, invalidNombre, "validHash");

            assertThatThrownBy(() -> userMapper.toDomain(entity))
                    .isInstanceOf(InvalidUserDataException.class)
                    .hasMessage("El nombre es obligatorio");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("Should enforce domain invariant and reject corrupted entity with blank or null passwordHash")
        void shouldRejectCorruptedEntityWithBlankOrNullPasswordHash(String invalidPasswordHash) {
            UserEntity entity = new UserEntity(1L, "nombreValido", invalidPasswordHash);

            assertThatThrownBy(() -> userMapper.toDomain(entity))
                    .isInstanceOf(InvalidUserDataException.class)
                    .hasMessage("La contraseña es obligatoria");
        }

        @Test
        @DisplayName("Should enforce domain invariant and reject entity with negative or zero ID")
        void shouldRejectEntityWithNonPositiveId() {
            UserEntity zeroIdEntity = new UserEntity(0L, "nombre", "hash");
            UserEntity negativeIdEntity = new UserEntity(-1L, "nombre", "hash");

            assertThatThrownBy(() -> userMapper.toDomain(zeroIdEntity))
                    .isInstanceOf(InvalidUserDataException.class)
                    .hasMessage("El identificador del usuario debe ser positivo");

            assertThatThrownBy(() -> userMapper.toDomain(negativeIdEntity))
                    .isInstanceOf(InvalidUserDataException.class)
                    .hasMessage("El identificador del usuario debe ser positivo");
        }
    }

    @Nested
    @DisplayName("Bidirectional Mapping Consistency")
    class RoundTripTests {

        @Test
        @DisplayName("Should preserve domain User through toEntity and back toDomain")
        void shouldPreserveUserThroughRoundTrip() {
            User original = new User(5L, "roundtripUser", "hash456");

            UserEntity entity = userMapper.toEntity(original);
            User reconstructed = userMapper.toDomain(entity);

            assertThat(reconstructed).isEqualTo(original);
            assertThat(reconstructed.getId()).isEqualTo(original.getId());
            assertThat(reconstructed.getNombre()).isEqualTo(original.getNombre());
            assertThat(reconstructed.getPasswordHash()).isEqualTo(original.getPasswordHash());
        }

        @Test
        @DisplayName("Should preserve UserEntity through toDomain and back toEntity")
        void shouldPreserveUserEntityThroughRoundTrip() {
            UserEntity originalEntity = new UserEntity(7L, "entityRoundTrip", "hash789");

            User domainUser = userMapper.toDomain(originalEntity);
            UserEntity reconstructedEntity = userMapper.toEntity(domainUser);

            assertThat(reconstructedEntity.getId()).isEqualTo(originalEntity.getId());
            assertThat(reconstructedEntity.getNombre()).isEqualTo(originalEntity.getNombre());
            assertThat(reconstructedEntity.getPasswordHash()).isEqualTo(originalEntity.getPasswordHash());
        }
    }
}
