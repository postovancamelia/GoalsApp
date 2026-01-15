package com.goalsapp.service;

import com.goalsapp.entity.Category;
import com.goalsapp.entity.GoalItem;
import com.goalsapp.entity.User;
import com.goalsapp.repository.GoalItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    GoalItemRepository goalRepo;

    @InjectMocks
    GoalService goalService;

    @Test
    void list_delegatesToRepository() {
        User user = new User("alice", "hash");
        Category category = Category.TODO;

        GoalItem item = new GoalItem(user, category, "x");
        when(goalRepo.findByUserAndCategoryOrderByCreatedAtDesc(user, category))
                .thenReturn(List.of(item));

        List<GoalItem> result = goalService.list(user, category);

        assertThat(result).containsExactly(item);
        verify(goalRepo).findByUserAndCategoryOrderByCreatedAtDesc(user, category);
        verifyNoMoreInteractions(goalRepo);
    }

    @Test
    void add_trimsText_andSavesGoalItem() {
        User user = new User("alice", "hash");
        Category category = Category.SHORT_TERM;

        // Return the same entity so we can assert what was passed in
        ArgumentCaptor<GoalItem> captor = ArgumentCaptor.forClass(GoalItem.class);
        when(goalRepo.save(any(GoalItem.class))).thenAnswer(inv -> inv.getArgument(0));

        GoalItem saved = goalService.add(user, category, "  learn spring  ");

        verify(goalRepo).save(captor.capture());
        GoalItem toSave = captor.getValue();

        assertThat(toSave.getUser()).isSameAs(user);
        assertThat(toSave.getCategory()).isEqualTo(category);
        assertThat(toSave.getText()).isEqualTo("learn spring");

        // method return is whatever repository saved returned
        assertThat(saved.getText()).isEqualTo("learn spring");
    }

    @Test
    void add_throwsWhenTextNullOrBlank() {
        User user = new User("alice", "hash");

        assertThatThrownBy(() -> goalService.add(user, Category.WISH, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Text cannot be empty");

        assertThatThrownBy(() -> goalService.add(user, Category.WISH, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Text cannot be empty");

        verifyNoInteractions(goalRepo);
    }
}