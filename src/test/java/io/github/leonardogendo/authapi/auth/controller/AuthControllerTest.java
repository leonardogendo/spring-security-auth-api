package io.github.leonardogendo.authapi.auth.controller;

import io.github.leonardogendo.authapi.auth.dto.LoginResponse;
import io.github.leonardogendo.authapi.auth.dto.RegisterResponse;
import io.github.leonardogendo.authapi.auth.exception.EmailAlreadyExistsException;
import io.github.leonardogendo.authapi.auth.service.AuthService;
import io.github.leonardogendo.authapi.infrastructure.web.error.GlobalExceptionHandler;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void register_withValidRequest_shouldReturn201() throws Exception {

        UUID userId = UUID.randomUUID();

        when(authService.register(any()))
                .thenReturn(
                        new RegisterResponse(
                                userId,
                                "user@example.com"
                        )
                );

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
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id")
                        .value(userId.toString()))
                .andExpect(jsonPath("$.email")
                        .value("user@example.com"));

        verify(authService)
                .register(any());
    }

    @Test
    void login_withValidRequest_shouldReturn200AndAccessToken()
            throws Exception {

        when(authService.login(any()))
                .thenReturn(
                        new LoginResponse(
                                "generated-access-token"
                        )
                );

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
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.accessToken")
                        .value("generated-access-token"));

        verify(authService)
                .login(any());
    }

    @Test
    void register_whenEmailAlreadyExists_shouldReturn409ProblemDetail()
            throws Exception {

        when(authService.register(any()))
                .thenThrow(
                        new EmailAlreadyExistsException()
                );

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
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.title")
                        .value("Conflict"))
                .andExpect(jsonPath("$.code")
                        .value("AUTH_EMAIL_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.instance")
                        .value("/api/auth/register"));
    }

    @Test
    void register_withInvalidRequest_shouldReturn400ProblemDetail()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "not-an-email",
                                          "password": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.title")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors")
                        .isMap())
                .andExpect(jsonPath("$.errors.email")
                        .exists())
                .andExpect(jsonPath("$.errors.password")
                        .exists());

        verify(authService, never())
                .register(any());
    }

    @Test
    void register_withMalformedJson_shouldReturn400()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "user@example.com",
                                          "password":
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verify(authService, never())
                .register(any());
    }

    @Test
    void login_withInvalidRequest_shouldReturn400ProblemDetail()
                throws Exception {

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                        "email": "not-an-email",
                                        "password": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.title")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors")
                        .isMap())
                .andExpect(jsonPath("$.errors.email")
                        .exists())
                .andExpect(jsonPath("$.errors.password")
                        .exists());

        verify(authService, never())
                .login(any());
    }
}