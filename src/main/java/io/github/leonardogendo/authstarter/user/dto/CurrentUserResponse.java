package io.github.leonardogendo.authstarter.user.dto;

import java.time.Instant;
import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        String email,
        boolean enabled,
        Instant createdAt
) {
}
