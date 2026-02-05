package com.goalsapp.repository;

import com.goalsapp.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void existsByUsername_trueWhenUserExists() {
        userRepository.save(new User("alice", "hash"));

        boolean exists = userRepository.existsByUsername("alice");

        assertThat(exists).isTrue();
        assertThat(userRepository.existsByUsername("bob")).isFalse();
    }

    @Test
    void findByUsername_returnsUserWhenFound() {
        userRepository.save(new User("alice", "hash"));

        var opt = userRepository.findByUsername("alice");

        assertThat(opt).isPresent();
        assertThat(opt.get().getUsername()).isEqualTo("alice");
    }

    @Test
    void findByUsername_emptyWhenNotFound() {
        var opt = userRepository.findByUsername("nobody");

        assertThat(opt).isEmpty();
    }
}
