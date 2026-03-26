package com.elite.controller;

import com.elite.model.User;
import com.elite.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String showHomePage(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "index";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/loginUser")
    public String loginUser(@RequestParam String email, @RequestParam String password, Model model) {
        User user = userRepository.findByEmailAndPassword(email, password);
        if (user != null) {
            return "redirect:/?loginSuccess=true";
        } else {
            model.addAttribute("error", "Bhai, ya toh email galat hai ya password. Pehle register toh kar lo!");
            return "login";
        }
    }

    @PostMapping("/save")
    public String save(User user, Model model) {
        if (userRepository.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "Bhai, ye email toh pehle se registered hai!");
            model.addAttribute("user", user);
            return "register";
        }
        userRepository.save(user);
        return "redirect:/login?registered=true";
    }
}