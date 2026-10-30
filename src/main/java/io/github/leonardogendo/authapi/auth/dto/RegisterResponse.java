package io.github.leonardogendo.authapi.auth.dto;

import java.util.UUID;

public record RegisterResponse(
    UUID id,
    String email
) {}
