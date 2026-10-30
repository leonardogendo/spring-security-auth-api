package io.github.leonardogendo.authapi.auth.controller;

import io.github.leonardogendo.authapi.auth.dto.LoginRequest;
import io.github.leonardogendo.authapi.auth.dto.LoginResponse;
import io.github.leonardogendo.authapi.auth.dto.RegisterRequest;
import io.github.leonardogendo.authapi.auth.dto.RegisterResponse;
import io.github.leonardogendo.authapi.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor 
public class AuthController {

    private final AuthService authService;

    // login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    // register
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(request));
    }
}

