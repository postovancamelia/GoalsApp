package com.goalsapp.controller;

import com.goalsapp.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsible for authentication-related endpoints,
 * including login and user registration.
 */
@Controller
public class AuthController {

    private final UserService userService;

    /**
     * Creates a new authentication controller.
     *
     * @param userService service used for user registration
     */
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String registerForm() { return "register"; }

    /**
     * Handles user registration requests.
     *
     * @param username the username provided by the user
     * @param password the plain text password provided by the user
     * @param model the UI model used to expose error messages
     * @return a redirect to the login page on success, or the registration view on error
     */
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           Model model) {
        try {
            userService.register(username, password);
            return "redirect:/login?registered";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}
