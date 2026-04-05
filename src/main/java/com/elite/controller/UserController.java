package com.elite.controller;

import com.elite.model.User;
import com.elite.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String index(@RequestParam(required = false) String search, Model model) {
        if (search != null && !search.isEmpty()) {
            model.addAttribute("users",
                    userRepository.findByNameContainingIgnoreCaseOrHeadlineContainingIgnoreCase(search, search));
        } else {
            model.addAttribute("users", userRepository.findAll());
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/loginUser")
    public String doLogin(String email, String password, HttpSession session, Model model) {
        User user = userRepository.findByEmailAndPassword(email, password);

        if (user != null) {
            session.setAttribute("loggedInUser", user);
            return "redirect:/dashboard";
        }
        
        model.addAttribute("error", "Invalid email or password. Please try again.");
        return "login";
    }

    @PostMapping("/save")
    public String signup(User user, Model model) {
        if (userRepository.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "This email is already registered!");
            return "register";
        }
        userRepository.save(user);
        return "redirect:/login?registered=true";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @PostMapping("/updateProfile")
    public String update(String headline, String bio, String githubLink, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        
        if (user != null) {
            user.setHeadline(headline);
            user.setBio(bio);
            user.setGithubLink(githubLink);
            
            userRepository.save(user);
            session.setAttribute("loggedInUser", user);
        }
        return "redirect:/dashboard?updated=true";
    }

    @GetMapping("/edit-profile")
    public String editProfile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        return "edit-profile";
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null) {
            model.addAttribute("sessionUser", user);
        }
    }
}