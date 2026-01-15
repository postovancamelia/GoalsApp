package com.goalsapp.service;

import com.goalsapp.entity.Category;
import com.goalsapp.entity.GoalItem;
import com.goalsapp.entity.User;
import com.goalsapp.repository.GoalItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service responsible for managing user goals.
 *
 * <p>
 * Encapsulates business rules related to creating
 * and retrieving goal items.
 * </p>
 */
@Service
public class GoalService {

    private final GoalItemRepository goalRepo;

    public GoalService(GoalItemRepository goalRepo) {
        this.goalRepo = goalRepo;
    }

    public List<GoalItem> list(User user, Category category) {
        return goalRepo.findByUserAndCategoryOrderByCreatedAtDesc(user, category);
    }

    //TODO: handle exception when empty is sent in the form
    public GoalItem add(User user, Category category, String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be empty.");
        }
        return goalRepo.save(new GoalItem(user, category, text.trim()));
    }
}
