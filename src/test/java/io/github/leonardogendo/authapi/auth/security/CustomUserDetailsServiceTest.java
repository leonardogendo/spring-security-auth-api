package io.github.leonardogendo.authapi.auth.security;

import io.github.leonardogendo.authapi.user.entity.Role;
import io.github.leonardogendo.authapi.user.entity.User;
import io.github.leonardogendo.authapi.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_whenUserExists_shouldReturnUserDetails() {

        // Arrange
        User user = new User(
                "user@example.com",
                "hashed-password"
        );

        user.addRole(Role.USER);

        when(
                userRepository.findWithRolesByEmail(
                        "user@example.com"
                )
        ).thenReturn(
                Optional.of(user)
        );

        // Act
        UserDetails result =
                userDetailsService.loadUserByUsername(
                        "user@example.com"
                );

        // Assert
        assertThat(result.getUsername())
                .isEqualTo("user@example.com");

        assertThat(result.getPassword())
                .isEqualTo("hashed-password");

        assertThat(result.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    void loadUserByUsername_whenUserDoesNotExist_shouldThrowException() {

        // Arrange
        String email = "missing@example.com";

        when(
                userRepository.findWithRolesByEmail(email)
        ).thenReturn(
                Optional.empty()
        );

        // Act + Assert
        assertThatThrownBy(
                () -> userDetailsService
                        .loadUserByUsername(email)
        )
                .isInstanceOf(
                        UsernameNotFoundException.class
                )
                .hasMessage(
                        "Invalid email or password"
                );
    }
}
