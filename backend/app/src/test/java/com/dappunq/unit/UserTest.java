package com.dappunq.unit;

import com.dappunq.exception.InvalidUserDataException;
import com.dappunq.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Nested
    @DisplayName("Valid User Creation and State")
    class ValidCreationTests {

        @Test
        @DisplayName("Should create user with id, normalized name, and password hash")
        void shouldCreateUserWithIdAndNormalizedName() {
            User user = new User(1L, "  usuario1  ", "hash123");

            assertThat(user.getId()).isEqualTo(1L);
            assertThat(user.getNombre()).isEqualTo("usuario1");
            assertThat(user.getPasswordHash()).isEqualTo("hash123");
        }

        @Test
        @DisplayName("Should create user without id, normalizing name")
        void shouldCreateUserWithoutId() {
            User user = new User("  usuarioNuevo  ", "encodedPassword");

            assertThat(user.getId()).isNull();
            assertThat(user.getNombre()).isEqualTo("usuarioNuevo");
            assertThat(user.getPasswordHash()).isEqualTo("encodedPassword");
        }

        @Test
        @DisplayName("Should allow setting positive id later")
        void shouldAllowSettingPositiveIdLater() {
            User user = new User("usuario", "hash");
            user.setId(5L);

            assertThat(user.getId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("Should allow setting id to null")
        void shouldAllowSettingIdToNull() {
            User user = new User(5L, "usuario", "hash");
            user.setId(null);

            assertThat(user.getId()).isNull();
        }

        @Test
        @DisplayName("Should update nombre and normalize whitespace")
        void shouldUpdateNombreAndNormalizeWhitespace() {
            User user = new User("usuario", "hash");
            user.setNombre("  nuevoNombre  ");

            assertThat(user.getNombre()).isEqualTo("nuevoNombre");
        }

        @Test
        @DisplayName("Should update passwordHash")
        void shouldUpdatePasswordHash() {
            User user = new User("usuario", "hash");
            user.setPasswordHash("nuevoHash");

            assertThat(user.getPasswordHash()).isEqualTo("nuevoHash");
        }
    }

    @Nested
    @DisplayName("Invariant Validations")
    class InvariantTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("Should reject null or blank nombre in constructor")
        void shouldRejectNullOrBlankNombreInConstructor(String invalidNombre) {
            assertThatThrownBy(() -> new User(invalidNombre, "validHash"))
                    .isInstanceOf(InvalidUserDataException.class)
                    .hasMessage("El nombre es obligatorio");

            assertThatThrownBy(() -> new User(1L, invalidNombre, "validHash"))
                    .isInstanceOf(InvalidUserDataException.class)
                    .hasMessage("El nombre es obligatorio");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("Should reject null or blank nombre in setter")
        void shouldRejectNullOrBlankNombreInSetter(String invalidNombre) {
            User user = new User("usuarioValido", "validHash");

            assertThatThrownBy(() -> user.setNombre(invalidNombre))
                    .isInstanceOf(InvalidUserDataException.class)
                    .hasMessage("El nombre es obligatorio");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("Should reject null or blank passwordHash in constructor")
        void shouldRejectNullOrBlankPasswordHashInConstructor(String invalidPasswordHash) {
            assertThatThrownBy(() -> new User("usuarioValido", invalidPasswordHash))
                    .isInstanceOf(InvalidUserDataException.class)
                    .hasMessage("La contraseña es obligatoria");

            assertThatThrownBy(() -> new User(1L, "usuarioValido", invalidPasswordHash))
                    .isInstanceOf(InvalidUserDataException.class)
                    .hasMessage("La contraseña es obligatoria");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("Should reject null or blank passwordHash in setter")
        void shouldRejectNullOrBlankPasswordHashInSetter(String invalidPasswordHash) {
            User user = new User("usuarioValido", "validHash");

            assertThatThrownBy(() -> user.setPasswordHash(invalidPasswordHash))
                    .isInstanceOf(InvalidUserDataException.class)
                    .hasMessage("La contraseña es obligatoria");
        }

        @Test
        @DisplayName("Should reject non-positive id in constructor")
        void shouldRejectNonPositiveIdInConstructor() {
            assertThatThrownBy(() -> new User(0L, "usuario", "hash"))
                    .isInstanceOf(InvalidUserDataException.class)
                    .hasMessage("El identificador del usuario debe ser positivo");

            assertThatThrownBy(() -> new User(-1L, "usuario", "hash"))
                    .isInstanceOf(InvalidUserDataException.class)
                    .hasMessage("El identificador del usuario debe ser positivo");
        }

        @Test
        @DisplayName("Should reject non-positive id in setter")
        void shouldRejectNonPositiveIdInSetter() {
            User user = new User("usuario", "hash");

            assertThatThrownBy(() -> user.setId(0L))
                    .isInstanceOf(InvalidUserDataException.class)
                    .hasMessage("El identificador del usuario debe ser positivo");

            assertThatThrownBy(() -> user.setId(-10L))
                    .isInstanceOf(InvalidUserDataException.class)
                    .hasMessage("El identificador del usuario debe ser positivo");
        }
    }

    @Nested
    @DisplayName("Framework and Persistence Independence")
    class DecouplingTests {

        @Test
        @DisplayName("User should have no jakarta.persistence annotations")
        void userShouldHaveNoPersistenceAnnotations() {
            boolean hasPersistenceClassAnnotation = Arrays.stream(User.class.getAnnotations())
                    .anyMatch(a -> a.annotationType().getName().startsWith("jakarta.persistence"));
            assertThat(hasPersistenceClassAnnotation).isFalse();

            for (Field field : User.class.getDeclaredFields()) {
                boolean hasPersistenceFieldAnnotation = Arrays.stream(field.getAnnotations())
                        .anyMatch(a -> a.annotationType().getName().startsWith("jakarta.persistence"));
                assertThat(hasPersistenceFieldAnnotation)
                        .withFailMessage("Field %s has persistence annotations", field.getName())
                        .isFalse();
            }
        }

        @Test
        @DisplayName("User should not implement Spring Security UserDetails")
        void userShouldNotImplementUserDetails() {
            Class<?>[] interfaces = User.class.getInterfaces();
            boolean implementsUserDetails = Arrays.stream(interfaces)
                    .anyMatch(i -> i.getName().equals("org.springframework.security.core.userdetails.UserDetails"));

            assertThat(implementsUserDetails).isFalse();
        }

        @Test
        @DisplayName("User should not have persistence-only fields like createdAt")
        void userShouldNotHaveCreatedAtField() {
            boolean hasCreatedAt = Arrays.stream(User.class.getDeclaredFields())
                    .anyMatch(f -> f.getName().equals("createdAt"));

            assertThat(hasCreatedAt).isFalse();
        }
    }

    @Nested
    @DisplayName("Equality and Identity")
    class EqualityTests {

        @Test
        @DisplayName("Users with same id and nombre should be equal")
        void usersWithSameIdAndNombreShouldBeEqual() {
            User user1 = new User(1L, "usuario", "hash1");
            User user2 = new User(1L, "usuario", "hash2");

            assertThat(user1).isEqualTo(user2);
            assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
        }

        @Test
        @DisplayName("Users with different id or nombre should not be equal")
        void usersWithDifferentIdOrNombreShouldNotBeEqual() {
            User user1 = new User(1L, "usuario1", "hash");
            User user2 = new User(2L, "usuario1", "hash");
            User user3 = new User(1L, "usuario2", "hash");

            assertThat(user1).isNotEqualTo(user2);
            assertThat(user1).isNotEqualTo(user3);
        }
    }
}
