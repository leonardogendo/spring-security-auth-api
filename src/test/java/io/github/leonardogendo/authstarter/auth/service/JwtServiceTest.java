package io.github.leonardogendo.authstarter.auth.service;

import io.github.leonardogendo.authstarter.auth.security.CustomUserDetails;
import io.github.leonardogendo.authstarter.infrastructure.security.JwtProperties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private JwtService jwtService;

    @Test
    void generateAccessToken_shouldBuildExpectedClaimsAndReturnToken() {

        // Arrange
        UUID userId = UUID.fromString(
                "550e8400-e29b-41d4-a716-446655440000"
        );

        CustomUserDetails userDetails =
                mock(CustomUserDetails.class);

        when(userDetails.getUserId())
                .thenReturn(userId);

        when(authentication.getPrincipal())
                .thenReturn(userDetails);

        when(authentication.getAuthorities())
                .thenAnswer(invocation -> List.of(
                        new SimpleGrantedAuthority("ROLE_USER")
                ));

        when(jwtProperties.issuer())
                .thenReturn("auth-starter");

        when(jwtProperties.audience())
                .thenReturn("auth-starter");

        when(jwtProperties.accessTokenTtl())
                .thenReturn(Duration.ofMinutes(15));

        Jwt encodedJwt = mock(Jwt.class);

        when(encodedJwt.getTokenValue())
                .thenReturn("generated-token");

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(encodedJwt);

        // Act
        String token =
                jwtService.generateAccessToken(authentication);

        // Assert
        assertThat(token)
                .isEqualTo("generated-token");

        ArgumentCaptor<JwtEncoderParameters> captor =
                ArgumentCaptor.forClass(
                        JwtEncoderParameters.class
                );

        verify(jwtEncoder)
                .encode(captor.capture());

        JwtClaimsSet claims =
                captor.getValue().getClaims();

        assertThat(claims.getClaimAsString("iss"))
                .isEqualTo("auth-starter");

        assertThat(claims.getSubject())
                .isEqualTo(userId.toString());

        assertThat(claims.getSubject())
                .isNotEqualTo("user@example.com");

        assertThat(claims.getAudience())
                .containsExactly("auth-starter");

        assertThat(claims.getClaimAsStringList("authorities"))
                .containsExactly("ROLE_USER");

        assertThat(claims.getIssuedAt())
                .isNotNull();

        assertThat(claims.getExpiresAt())
                .isEqualTo(
                        claims.getIssuedAt()
                                .plus(Duration.ofMinutes(15))
                );
    }

    @Test
    void generateAccessToken_withMultipleAuthorities_shouldIncludeAllAuthorities() {

        // Arrange
        UUID userId = UUID.fromString(
                "550e8400-e29b-41d4-a716-446655440001"
        );

        CustomUserDetails userDetails =
                mock(CustomUserDetails.class);

        when(userDetails.getUserId())
                .thenReturn(userId);

        when(authentication.getPrincipal())
                .thenReturn(userDetails);

        when(authentication.getAuthorities())
                .thenAnswer(invocation -> List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                ));

        when(jwtProperties.issuer())
                .thenReturn("auth-starter");

        when(jwtProperties.audience())
                .thenReturn("auth-starter");

        when(jwtProperties.accessTokenTtl())
                .thenReturn(Duration.ofMinutes(15));

        Jwt encodedJwt = mock(Jwt.class);

        when(encodedJwt.getTokenValue())
                .thenReturn("generated-token");

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(encodedJwt);

        // Act
        jwtService.generateAccessToken(authentication);

        // Assert
        ArgumentCaptor<JwtEncoderParameters> captor =
                ArgumentCaptor.forClass(
                        JwtEncoderParameters.class
                );

        verify(jwtEncoder)
                .encode(captor.capture());

        JwtClaimsSet claims =
                captor.getValue().getClaims();

        assertThat(claims.getSubject())
                .isEqualTo(userId.toString());

        assertThat(claims.getClaimAsStringList("authorities"))
                .containsExactlyInAnyOrder(
                        "ROLE_USER",
                        "ROLE_ADMIN"
                );
    }
}