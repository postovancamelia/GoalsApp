package com.goalsapp.repository;

import com.goalsapp.entity.Category;
import com.goalsapp.entity.GoalItem;
import com.goalsapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for accessing {@link GoalItem} entities.
 *
 * Provides query methods for retrieving user goals
 * by category and creation time.
 */
public interface GoalItemRepository extends JpaRepository<GoalItem, Long> {
    List<GoalItem> findByUserAndCategoryOrderByCreatedAtDesc(User user, Category category);
}
