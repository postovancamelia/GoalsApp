package com.goalsapp.repository;

import com.goalsapp.entity.Category;
import com.goalsapp.entity.GoalItem;
import com.goalsapp.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class GoalItemRepositoryTest {

    @Autowired
    GoalItemRepository goalItemRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void findByUserAndCategoryOrderByCreatedAtDesc_returnsOnlyThatUserAndCategory_sortedDesc() {
        User alice = userRepository.save(new User("alice", "hash"));
        User bob   = userRepository.save(new User("bob", "hash"));

        GoalItem a1 = new GoalItem(alice, Category.TODO, "older");
        GoalItem a2 = new GoalItem(alice, Category.TODO, "newer");

        a1.setText("older");
        a2.setText("newer");

        goalItemRepository.save(a1);
        goalItemRepository.save(a2);

        goalItemRepository.save(new GoalItem(alice, Category.WISH, "wish"));

        goalItemRepository.save(new GoalItem(bob, Category.TODO, "bob task"));

        List<GoalItem> result =
                goalItemRepository.findByUserAndCategoryOrderByCreatedAtDesc(alice, Category.TODO);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(g -> g.getUser().getId().equals(alice.getId()));
        assertThat(result).allMatch(g -> g.getCategory() == Category.TODO);

        assertThat(result.get(0).getCreatedAt()).isAfterOrEqualTo(result.get(1).getCreatedAt());
    }
}
