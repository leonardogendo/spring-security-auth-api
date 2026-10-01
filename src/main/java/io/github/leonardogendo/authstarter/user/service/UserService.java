package io.github.leonardogendo.authstarter.user.service;

import io.github.leonardogendo.authstarter.user.dto.CurrentUserResponse;
import io.github.leonardogendo.authstarter.user.entity.User;
import io.github.leonardogendo.authstarter.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        return new CurrentUserResponse(
                user.getId(),
                user.getEmail(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}
