package io.github.leonardogendo.authstarter.integration.auth;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import io.github.leonardogendo.authstarter.user.entity.User;
import io.github.leonardogendo.authstarter.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(
        properties = {
                "jwt.secret=dGVzdC10ZXN0LXRlc3QtdGVzdC10ZXN0LXRlc3QtdGVzdC10ZXN0LTEyMzQ1Njc4OTA=",
                "jwt.issuer=auth-starter-test",
                "jwt.audience=auth-starter-test",
                "jwt.access-token-ttl=15m"
        }
)
@AutoConfigureMockMvc
class AuthenticationIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void register_shouldPersistNormalizedUserWithEncodedPassword()
            throws Exception {

        String rawPassword = "SecurePassword123!";

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "USER@example.com",
                                          "password": "SecurePassword123!"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.email")
                        .value("user@example.com"))
                .andExpect(jsonPath("$.id")
                        .isNotEmpty());

        User persistedUser =
                userRepository
                        .findWithRolesByEmail(
                                "user@example.com"
                        )
                        .orElseThrow();

        assertThat(persistedUser.getEmail())
                .isEqualTo("user@example.com");

        assertThat(persistedUser.getPasswordHash())
                .isNotEqualTo(rawPassword);

        assertThat(
                passwordEncoder.matches(
                        rawPassword,
                        persistedUser.getPasswordHash()
                )
        ).isTrue();

        assertThat(persistedUser.getRoles())
                .extracting(Enum::name)
                .containsExactly("USER");

        assertThat(persistedUser.getCreatedAt())
                .isNotNull();

        assertThat(persistedUser.getUpdatedAt())
                .isNotNull();
    }

    @Test
    void register_whenEmailAlreadyExists_shouldReturn409()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "user@example.com",
                                          "password": "SecurePassword123!"
                                        }
                                        """)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "USER@example.com",
                                          "password": "AnotherPassword123!"
                                        }
                                        """)
                )
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.code")
                        .value("AUTH_EMAIL_ALREADY_EXISTS"));

        assertThat(userRepository.count())
                .isEqualTo(1);
    }

    @Test
    void login_withRegisteredCredentials_shouldReturnRealJwtWithUuidSubject()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "user@example.com",
                                          "password": "SecurePassword123!"
                                        }
                                        """)
                )
                .andExpect(status().isCreated());

        User persistedUser =
                userRepository
                        .findWithRolesByEmail("user@example.com")
                        .orElseThrow();

        UUID expectedUserId = persistedUser.getId();

        String responseBody =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("""
                                                {
                                                  "email": "user@example.com",
                                                  "password": "SecurePassword123!"
                                                }
                                                """)
                        )
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.accessToken")
                                .isNotEmpty())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        JsonNode response =
                jsonMapper.readTree(responseBody);

        String accessToken =
                response.get("accessToken")
                        .asString();

        String[] tokenParts = accessToken.split("\\.");

        assertThat(tokenParts)
                .hasSize(3);

        String payloadJson =
                new String(
                        Base64.getUrlDecoder()
                                .decode(tokenParts[1]),
                        StandardCharsets.UTF_8
                );

        JsonNode payload =
                jsonMapper.readTree(payloadJson);

        assertThat(payload.get("sub").asString())
                .isEqualTo(expectedUserId.toString());

        assertThat(payload.get("iss").asString())
                .isEqualTo("auth-starter-test");

        assertThat(payload.get("aud").asString())
                .isEqualTo("auth-starter-test");

        assertThat(payload.get("authorities").get(0).asString())
                .isEqualTo("ROLE_USER");

        assertThat(payload.get("iat").asLong())
                .isPositive();

        assertThat(payload.get("exp").asLong())
                .isGreaterThan(payload.get("iat").asLong());
    }

    @Test
    void login_withWrongPassword_shouldReturn401AndNoToken()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "user@example.com",
                                          "password": "SecurePassword123!"
                                        }
                                        """)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "user@example.com",
                                          "password": "WrongPassword123!"
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.code")
                        .value("AUTH_INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.accessToken")
                        .doesNotExist());
    }

    @Test
    void getMe_withoutAccessToken_shouldReturn401()
            throws Exception {

        mockMvc.perform(
                        get("/api/users/me")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMe_withValidAccessToken_shouldReturnAuthenticatedUser()
            throws Exception {

        // Arrange: Register user
        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "user@example.com",
                                          "password": "SecurePassword123!"
                                        }
                                        """)
                )
                .andExpect(status().isCreated());

        User persistedUser =
                userRepository
                        .findWithRolesByEmail("user@example.com")
                        .orElseThrow();

        UUID expectedUserId = persistedUser.getId();

        // Act: Authenticate and obtain a real JWT
        String loginResponse =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("""
                                                {
                                                  "email": "user@example.com",
                                                  "password": "SecurePassword123!"
                                                }
                                                """)
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String accessToken =
                jsonMapper.readTree(loginResponse)
                        .get("accessToken")
                        .asString();

        // Assert: Access protected resource using real Bearer token
        mockMvc.perform(
                        get("/api/users/me")
                                .header(
                                        "Authorization",
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id")
                        .value(expectedUserId.toString()))
                .andExpect(jsonPath("$.email")
                        .value("user@example.com"))
                .andExpect(jsonPath("$.enabled")
                        .value(true))
                .andExpect(jsonPath("$.createdAt")
                        .isNotEmpty());
    }
}