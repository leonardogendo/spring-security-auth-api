package io.github.leonardogendo.authstarter.user.controller;

import io.github.leonardogendo.authstarter.user.dto.CurrentUserResponse;
import io.github.leonardogendo.authstarter.user.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> getMe(Principal principal) {
        return ResponseEntity.ok(
                userService.getCurrentUser(principal.getName())
        );
    }
}
