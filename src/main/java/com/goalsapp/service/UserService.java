package com.goalsapp.service;

import com.goalsapp.entity.User;
import com.goalsapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service responsible for user registration and user-related business logic.
 *
 * <p>
 * IMPORTANT:
 * Usernames are treated as exact values.
 * Leading and trailing whitespace is NOT trimmed or normalized.
 * Validation and uniqueness checks are performed on the raw input.
 * </p>
 */

@Service
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    /**
     * Registers a new user.
     *
     * <p>
     * The username is used exactly as provided.
     * No trimming or normalization is applied.
     * </p>
     *
     * @param username the username as entered by the user; must not be null or blank
     * @param rawPassword the plain text password; must not be null or blank
     * @return the persisted {@link User}
     *
     * @throws IllegalArgumentException if the username or password is null or blank
     * @throws IllegalArgumentException if a user with the same username already exists
     */

    public User register(String username, String rawPassword) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        if (userRepo.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists.");
        }
        return userRepo.save(new User(username.trim(), encoder.encode(rawPassword)));
    }

    public User findByUsernameOrThrow(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }
}
