package io.github.leonardogendo.authapi.infrastructure.web.error;

import io.github.leonardogendo.authapi.auth.exception.EmailAlreadyExistsException;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // email already exists
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExists(
            EmailAlreadyExistsException ex,
            HttpServletRequest request
    ) {

            ProblemDetail problem =
                    ProblemDetail.forStatusAndDetail(
                            HttpStatus.CONFLICT,
                            ex.getMessage()
                    );

            problem.setTitle("Conflict");
            problem.setInstance(URI.create(request.getRequestURI()));
            problem.setProperty(
                    "code",
                    "AUTH_EMAIL_ALREADY_EXISTS"
            );

            return problem;
    }
    // bad credentials
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {

            ProblemDetail problem =
                    ProblemDetail.forStatusAndDetail(
                            HttpStatus.UNAUTHORIZED,
                            "Invalid email or password"
                    );

            problem.setTitle("Unauthorized");
            problem.setInstance(URI.create(request.getRequestURI()));
            problem.setProperty(
                    "code",
                    "AUTH_INVALID_CREDENTIALS"
            );

            return problem;
    }

    // validation error
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

            Map<String, String> errors =
                    ex.getBindingResult()
                            .getFieldErrors()
                            .stream()
                            .collect(Collectors.toMap(
                                    FieldError::getField,
                                    error -> error.getDefaultMessage() != null
                                            ? error.getDefaultMessage()
                                            : "Invalid value",
                                    (first, second) -> first
                            ));

            ProblemDetail problem =
                    ProblemDetail.forStatusAndDetail(
                            HttpStatus.BAD_REQUEST,
                            "One or more fields are invalid"
                    );

            problem.setTitle("Validation failed");
            problem.setInstance(
                    URI.create(request.getRequestURI())
            );
            problem.setProperty(
                    "code",
                    "VALIDATION_ERROR"
            );
            problem.setProperty(
                    "errors",
                    errors
            );

            return problem;
    }

    // malformed or unreadable request body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {

    ProblemDetail problem =
            ProblemDetail.forStatusAndDetail(
                    HttpStatus.BAD_REQUEST,
                    "Request body is malformed or unreadable"
            );

    problem.setTitle("Malformed request");
    problem.setInstance(
            URI.create(request.getRequestURI())
    );
    problem.setProperty(
            "code",
            "MALFORMED_REQUEST"
    );

    return problem;
    }
}
