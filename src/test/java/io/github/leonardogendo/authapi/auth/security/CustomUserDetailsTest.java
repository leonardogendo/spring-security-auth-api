package io.github.leonardogendo.authapi.auth.security;

import io.github.leonardogendo.authapi.user.entity.Role;
import io.github.leonardogendo.authapi.user.entity.User;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

    @Test
    void getAuthorities_shouldConvertRolesToSpringAuthorities() {

        // Arrange
        User user = new User(
                "admin@example.com",
                "hashed-password"
        );

        user.addRole(Role.USER);
        user.addRole(Role.ADMIN);

        CustomUserDetails userDetails =
                new CustomUserDetails(user);

        // Act
        Collection<? extends GrantedAuthority> authorities =
                userDetails.getAuthorities();

        // Assert
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder(
                        "ROLE_USER",
                        "ROLE_ADMIN"
                );
    }
}