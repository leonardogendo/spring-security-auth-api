package io.github.leonardogendo.authapi.user.service;

import io.github.leonardogendo.authapi.user.dto.CurrentUserResponse;
import io.github.leonardogendo.authapi.user.entity.User;
import io.github.leonardogendo.authapi.user.exception.UserNotFoundException;
import io.github.leonardogendo.authapi.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return new CurrentUserResponse(
                user.getId(),
                user.getEmail(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}
