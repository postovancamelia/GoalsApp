package com.goalsapp.controller;

import com.goalsapp.entity.Category;
import com.goalsapp.entity.User;
import com.goalsapp.service.GoalService;
import com.goalsapp.service.GuidanceService;
import com.goalsapp.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * Controller responsible for managing goal-related pages and actions.
 *
 * <p>
 * Handles listing goals by category, adding new goals,
 * and displaying AI-generated guidance.
 * </p>
 */
@Controller
@RequestMapping("/goals")
public class GoalsController {


    private final GoalService goalService;
    private final GuidanceService guidanceService;
    private final UserService userService;

    public GoalsController(GoalService goalService,
                           GuidanceService guidanceService,
                           UserService userService) {
        this.goalService = goalService;
        this.guidanceService = guidanceService;
        this.userService = userService;
    }

    @GetMapping("/{category}")
    public String categoryPage(@PathVariable Category category,
                               Principal principal,
                               Model model) {
        User user = userService.findByUsernameOrThrow(principal.getName());
        model.addAttribute("category", category);
        model.addAttribute("items", goalService.list(user, category));
        return "category";
    }

    @PostMapping("/{category}/add")
    public String addItem(@PathVariable Category category,
                          @RequestParam String text,
                          Principal principal) {
        User user = userService.findByUsernameOrThrow(principal.getName());
        goalService.add(user, category, text);
        return "redirect:/goals/" + category.name();
    }

    @PostMapping("/{category}/guidance")
    public String guidanceOnSamePage(@PathVariable Category category,
                                     Principal principal,
                                     Model model) {
        User user = userService.findByUsernameOrThrow(principal.getName());

        model.addAttribute("category", category);
        model.addAttribute("items", goalService.list(user, category));
        model.addAttribute("guidance", guidanceService.getGuidance(user, category));

        return "category";
    }
}
