package com.returnhub.controller;

import com.returnhub.model.Category;
import com.returnhub.model.User;
import com.returnhub.service.UserService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final UserService userService;

    public GlobalModelAttributes(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("currentUser")
    public User getCurrentUser() {
        return userService.getCurrentUser();
    }

    @ModelAttribute("allCategories")
    public Category[] getAllCategories() {
        return Category.values();
    }
}
