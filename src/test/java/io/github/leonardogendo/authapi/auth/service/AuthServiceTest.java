package io.github.leonardogendo.authapi.auth.service;

import io.github.leonardogendo.authapi.auth.dto.LoginRequest;
import io.github.leonardogendo.authapi.auth.dto.LoginResponse;
import io.github.leonardogendo.authapi.auth.dto.RegisterRequest;
import io.github.leonardogendo.authapi.auth.dto.RegisterResponse;
import io.github.leonardogendo.authapi.auth.exception.EmailAlreadyExistsException;
import io.github.leonardogendo.authapi.user.entity.Role;
import io.github.leonardogendo.authapi.user.entity.User;
import io.github.leonardogendo.authapi.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;


    @Test
    void register_withValidRequest_shouldCreateUser() {

        // Arrange
        RegisterRequest request =
                new RegisterRequest(
                        "  USER@EXAMPLE.COM ",
                        "SecurePassword123!"
                );

        UUID userId = UUID.randomUUID();

        User savedUser = mock(User.class);

        when(
                userRepository.existsByEmail(
                        "user@example.com"
                )
        ).thenReturn(false);

        when(
                passwordEncoder.encode(
                        "SecurePassword123!"
                )
        ).thenReturn(
                "{bcrypt}encoded-password"
        );

        when(
                userRepository.save(any(User.class))
        ).thenReturn(savedUser);

        when(savedUser.getId())
                .thenReturn(userId);

        when(savedUser.getEmail())
                .thenReturn("user@example.com");


        // Act
        RegisterResponse response =
                authService.register(request);


        // Assert
        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository)
                .save(userCaptor.capture());

        User userToSave =
                userCaptor.getValue();

        assertThat(userToSave.getEmail())
                .isEqualTo("user@example.com");

        assertThat(userToSave.getPasswordHash())
                .isEqualTo("{bcrypt}encoded-password");

        assertThat(userToSave.getRoles())
                .containsExactly(Role.USER);

        assertThat(response.id())
                .isEqualTo(userId);

        assertThat(response.email())
                .isEqualTo("user@example.com");
    }


    @Test
    void register_whenEmailAlreadyExists_shouldThrowException() {

        // Arrange
        RegisterRequest request =
                new RegisterRequest(
                        "user@example.com",
                        "SecurePassword123!"
                );

        when(
                userRepository.existsByEmail(
                        "user@example.com"
                )
        ).thenReturn(true);


        // Act + Assert
        assertThatThrownBy(
                () -> authService.register(request)
        )
                .isInstanceOf(
                        EmailAlreadyExistsException.class
                );

        verify(userRepository, never())
                .save(any(User.class));

        verify(passwordEncoder, never())
                .encode(anyString());
    }


    @Test
    void login_withValidCredentials_shouldReturnAccessToken() {

        // Arrange
        LoginRequest request =
                new LoginRequest(
                        "user@example.com",
                        "SecurePassword123!"
                );

        Authentication authentication =
                mock(Authentication.class);

        when(
                authenticationManager.authenticate(
                        any(UsernamePasswordAuthenticationToken.class)
                )
        ).thenReturn(authentication);

        when(
                jwtService.generateAccessToken(authentication)
        ).thenReturn(
                "generated-access-token"
        );


        // Act
        LoginResponse response =
                authService.login(request);


        // Assert
        assertThat(response.accessToken())
                .isEqualTo(
                        "generated-access-token"
                );

        ArgumentCaptor<Authentication> authenticationCaptor =
                ArgumentCaptor.forClass(Authentication.class);

        verify(authenticationManager)
                .authenticate(
                        authenticationCaptor.capture()
                );

        Authentication submittedAuthentication =
                authenticationCaptor.getValue();

        assertThat(submittedAuthentication.getPrincipal())
                .isEqualTo("user@example.com");

        assertThat(submittedAuthentication.getCredentials())
                .isEqualTo("SecurePassword123!");

        verify(jwtService)
                .generateAccessToken(authentication);
    }


    @Test
    void login_withInvalidCredentials_shouldPropagateAuthenticationFailure() {

        // Arrange
        LoginRequest request =
                new LoginRequest(
                        "user@example.com",
                        "wrong-password"
                );

        when(
                authenticationManager.authenticate(
                        any(UsernamePasswordAuthenticationToken.class)
                )
        ).thenThrow(
                new BadCredentialsException(
                        "Bad credentials"
                )
        );


        // Act + Assert
        assertThatThrownBy(
                () -> authService.login(request)
        )
                .isInstanceOf(
                        BadCredentialsException.class
                );

        verify(jwtService, never())
                .generateAccessToken(any());
    }
}
