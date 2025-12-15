package com.vishwask.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.vishwask.chess.User;
import com.vishwask.chess.UserService;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                          @RequestParam String email,
                          @RequestParam String password,
                          Model model) {
        try {
            userService.registerUser(username, email, password);
            return "redirect:/login?registered";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(required = false) String registered,
                               @RequestParam(required = false) String error,
                               Model model,
                               HttpServletRequest request) {
        if (registered != null) {
            model.addAttribute("message", "Registration successful! Please login.");
        }
        if (error != null) {
            Object lastException = request.getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
            if (lastException instanceof Exception) {
                model.addAttribute("error", ((Exception) lastException).getMessage());
            } else {
                model.addAttribute("error", "Login failed. Please try again.");
            }
            request.getSession().removeAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        }
        return "login";
    }
}