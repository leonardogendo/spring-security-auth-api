package io.github.leonardogendo.authstarter.user.service;

import io.github.leonardogendo.authstarter.user.dto.CurrentUserResponse;
import io.github.leonardogendo.authstarter.user.entity.User;
import io.github.leonardogendo.authstarter.user.exception.UserNotFoundException;
import io.github.leonardogendo.authstarter.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getCurrentUser_whenUserExists_shouldReturnCurrentUserResponse() {

        // Arrange
        UUID userId = UUID.fromString(
                "550e8400-e29b-41d4-a716-446655440000"
        );

        Instant createdAt = Instant.parse(
                "2026-10-01T10:00:00Z"
        );

        User user = org.mockito.Mockito.mock(User.class);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(user.getId())
                .thenReturn(userId);

        when(user.getEmail())
                .thenReturn("user@example.com");

        when(user.isEnabled())
                .thenReturn(true);

        when(user.getCreatedAt())
                .thenReturn(createdAt);

        // Act
        CurrentUserResponse response =
                userService.getCurrentUser(userId);

        // Assert
        assertThat(response.id())
                .isEqualTo(userId);

        assertThat(response.email())
                .isEqualTo("user@example.com");

        assertThat(response.enabled())
                .isTrue();

        assertThat(response.createdAt())
                .isEqualTo(createdAt);

        verify(userRepository)
                .findById(userId);
    }

    @Test
    void getCurrentUser_whenUserDoesNotExist_shouldThrowUserNotFoundException() {

        // Arrange
        UUID userId = UUID.fromString(
                "550e8400-e29b-41d4-a716-446655440099"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(
                () -> userService.getCurrentUser(userId)
        )
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository)
                .findById(userId);
    }
}