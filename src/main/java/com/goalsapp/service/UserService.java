package com.goalsapp.service;

import com.goalsapp.entity.User;
import com.goalsapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

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
