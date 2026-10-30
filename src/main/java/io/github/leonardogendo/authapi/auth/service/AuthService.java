package io.github.leonardogendo.authapi.auth.service;

import io.github.leonardogendo.authapi.auth.dto.LoginRequest;
import io.github.leonardogendo.authapi.auth.dto.LoginResponse;
import io.github.leonardogendo.authapi.auth.dto.RegisterRequest;
import io.github.leonardogendo.authapi.auth.dto.RegisterResponse;
import io.github.leonardogendo.authapi.user.entity.Role;
import io.github.leonardogendo.authapi.user.entity.User;
import io.github.leonardogendo.authapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.github.leonardogendo.authapi.auth.exception.EmailAlreadyExistsException;

import java.util.Locale;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // login
    public LoginResponse login(LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.email(),
                                request.password()
                        )
                );

        String accessToken = jwtService.generateAccessToken(authentication);

        return new LoginResponse(accessToken);
    }

    // registration
    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException();
        }

        String passwordHash =
                passwordEncoder.encode(request.password());

        User user = new User(email, passwordHash);
        user.addRole(Role.USER); 

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
