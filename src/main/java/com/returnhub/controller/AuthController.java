package com.returnhub.controller;

import com.returnhub.dto.UserRegistrationDto;
import com.returnhub.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginForm() {
        if (userService.getCurrentUser() != null) {
            return "redirect:/my-reports";
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (userService.getCurrentUser() != null) {
            return "redirect:/my-reports";
        }
        if (!model.containsAttribute("userDto")) {
            model.addAttribute("userDto", new UserRegistrationDto());
        }
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute("userDto") UserRegistrationDto userDto,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (userDto.getPassword() != null && !userDto.isPasswordMatching()) {
            result.rejectValue("confirmPassword", "error.userDto", "Passwords do not match");
        }

        if (userDto.getEmail() != null && userService.emailExists(userDto.getEmail())) {
            result.rejectValue("email", "error.userDto", "An account with this email already exists");
        }

        if (result.hasErrors()) {
            return "register";
        }

        try {
            userService.registerUser(userDto);
            redirectAttributes.addFlashAttribute("successMessage", "Account created successfully! Please log in.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }
}
