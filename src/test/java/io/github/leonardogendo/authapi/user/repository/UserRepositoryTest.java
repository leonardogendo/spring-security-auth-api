package io.github.leonardogendo.authapi.user.repository;

import io.github.leonardogendo.authapi.user.entity.Role;
import io.github.leonardogendo.authapi.user.entity.User;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;

import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@DataJpaTest
class UserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findByEmail_whenUserExists_shouldReturnUser() {

        User user = new User(
                "user@example.com",
                "{bcrypt}password-hash"
        );

        user.addRole(Role.USER);

        userRepository.saveAndFlush(user);

        entityManager.clear();

        User found = userRepository
                .findByEmail("user@example.com")
                .orElseThrow();

        assertThat(found.getEmail())
                .isEqualTo("user@example.com");

        assertThat(found.getPasswordHash())
                .isEqualTo("{bcrypt}password-hash");
    }

    @Test
    void existsByEmail_whenUserExists_shouldReturnTrue() {

        User user = new User(
                "user@example.com",
                "{bcrypt}password-hash"
        );

        user.addRole(Role.USER);

        userRepository.saveAndFlush(user);

        assertThat(
                userRepository.existsByEmail(
                        "user@example.com"
                )
        ).isTrue();
    }

    @Test
    void findWithRolesByEmail_shouldLoadRolesUsingEntityGraph() {

        User user = new User(
                "admin@example.com",
                "{bcrypt}password-hash"
        );

        user.addRole(Role.USER);
        user.addRole(Role.ADMIN);

        userRepository.saveAndFlush(user);

        entityManager.clear();

        User found = userRepository
                .findWithRolesByEmail(
                        "admin@example.com"
                )
                .orElseThrow();

        PersistenceUnitUtil persistenceUnitUtil =
                entityManager
                        .getEntityManagerFactory()
                        .getPersistenceUnitUtil();

        assertThat(
                persistenceUnitUtil.isLoaded(
                        found,
                        "roles"
                )
        ).isTrue();

        assertThat(found.getRoles())
                .containsExactlyInAnyOrder(
                        Role.USER,
                        Role.ADMIN
                );
    }

    @Test
    void save_whenEmailIsDuplicated_shouldViolateUniqueConstraint() {

        User first = new User(
                "user@example.com",
                "{bcrypt}first-hash"
        );

        first.addRole(Role.USER);

        userRepository.saveAndFlush(first);

        User duplicate = new User(
                "user@example.com",
                "{bcrypt}second-hash"
        );

        duplicate.addRole(Role.USER);

        assertThatThrownBy(
                () -> userRepository.saveAndFlush(
                        duplicate
                )
        ).isInstanceOf(
                DataIntegrityViolationException.class
        );
    }
}