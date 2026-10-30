package io.github.leonardogendo.authapi.infrastructure.security;

import org.junit.jupiter.api.Test;

import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AudienceValidatorTest {

    @Test
    void validate_whenRequiredAudiencePresent_shouldSucceed() {

        // Arrange
        AudienceValidator validator =
                new AudienceValidator("auth-api");

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("user@example.com")
                .audience(List.of("auth-api"))
                .build();

        // Act
        OAuth2TokenValidatorResult result =
                validator.validate(jwt);

        // Assert
        assertThat(result.hasErrors())
                .isFalse();
    }

    @Test
    void validate_whenRequiredAudienceMissing_shouldFailWithInvalidToken() {

        // Arrange
        AudienceValidator validator =
                new AudienceValidator("auth-api");

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("user@example.com")
                .audience(List.of("different-api"))
                .build();

        // Act
        OAuth2TokenValidatorResult result =
                validator.validate(jwt);

        // Assert
        assertThat(result.hasErrors())
                .isTrue();

        assertThat(result.getErrors())
                .singleElement()
                .satisfies(error ->
                        assertThat(error.getErrorCode())
                                .isEqualTo("invalid_token")
                );
    }
}