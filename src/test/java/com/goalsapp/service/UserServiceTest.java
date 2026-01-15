package com.goalsapp.service;

import com.goalsapp.entity.User;
import com.goalsapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepo;

    @Mock
    PasswordEncoder encoder;

    @InjectMocks
    UserService userService;

    @Test
    void register_encodesPassword_andSaves() {
        when(userRepo.existsByUsername("alice")).thenReturn(false);
        when(encoder.encode("pw")).thenReturn("encoded");
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.register("alice", "pw");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(captor.capture());
        User toSave = captor.getValue();

        assertThat(toSave.getUsername()).isEqualTo("alice");
        assertThat(toSave.getPasswordHash()).isEqualTo("encoded");
        assertThat(saved.getUsername()).isEqualTo("alice");
    }

    @Test
    void register_throwsOnBlankUsernameOrPassword() {
        assertThatThrownBy(() -> userService.register("  ", "pw"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username cannot be empty");

        assertThatThrownBy(() -> userService.register("alice", " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password cannot be empty");

        verifyNoInteractions(userRepo, encoder);
    }

    @Test
    void register_throwsWhenUsernameAlreadyExists() {
        when(userRepo.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> userService.register("alice", "pw"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepo).existsByUsername("alice");
        verifyNoMoreInteractions(userRepo);
        verifyNoInteractions(encoder);
    }

    @Test
    void findByUsernameOrThrow_returnsUserWhenPresent() {
        User u = new User("alice", "hash");
        when(userRepo.findByUsername("alice")).thenReturn(Optional.of(u));

        User result = userService.findByUsernameOrThrow("alice");

        assertThat(result).isSameAs(u);
        verify(userRepo).findByUsername("alice");
    }

    @Test
    void findByUsernameOrThrow_throwsWhenMissing() {
        when(userRepo.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByUsernameOrThrow("missing"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");

        verify(userRepo).findByUsername("missing");
    }
}