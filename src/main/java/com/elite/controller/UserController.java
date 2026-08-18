package com.elite.controller;

import com.elite.config.CustomUserDetails;
import com.elite.model.Education;
import com.elite.model.Project;
import com.elite.model.User;
import com.elite.repository.EducationRepository;
import com.elite.repository.ProjectRepository;
import com.elite.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Ye do nayi lines add karni hain
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EducationRepository educationRepository;


    // 1. Home Page - Hide the logged-in user from the feed
    @GetMapping("/")
    public String index(@RequestParam(required = false) String search, Principal principal, Model model) {
        List<User> users;

        if (search != null && !search.isEmpty()) {
            users = userRepository.findByNameContainingIgnoreCaseOrHeadlineContainingIgnoreCase(search, search);
        } else {
            users = userRepository.findAll();
        }

        // Filter out the currently logged-in user from the feed
        if (principal != null) {
            users = users.stream()
                    .filter(u -> !u.getEmail().equals(principal.getName()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("users", users);
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

    // 2. Handle Registration & Auto-Login
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, Model model) {
        if (userRepository.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "This email is already registered!");
            return "register";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        // Auto-login logic after successful registration
        UserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return "redirect:/dashboard"; // Direct to dashboard!
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        if (principal != null) {
            userRepository.findByEmail(principal.getName())
                    .ifPresent(user -> model.addAttribute("user", user));
        }
        return "dashboard";
    }

    @GetMapping("/edit-profile")
    public String editProfile(Principal principal, Model model) {
        if (principal != null) {
            userRepository.findByEmail(principal.getName())
                    .ifPresent(user -> model.addAttribute("user", user));
        }
        return "edit-profile";
    }

    @PostMapping("/updateProfile")
    public String update(String headline, String bio, String githubLink, Principal principal) {
        if (principal != null) {
            userRepository.findByEmail(principal.getName()).ifPresent(user -> {
                user.setHeadline(headline);
                user.setBio(bio);
                user.setGithubLink(githubLink);
                userRepository.save(user);
            });
        }
        return "redirect:/dashboard?updated=true";
    }

    // View Public Developer Profile
    @GetMapping("/developer/{id}")
    public String viewDeveloperProfile(@PathVariable Long id, Model model, Principal principal) {
        // Find the user from the database based on the ID
        Optional<User> devOptional = userRepository.findById(id);

        // If the user is not found (e.g. invalid ID), redirect to the home page
        if (devOptional.isEmpty()) {
            return "redirect:/";
        }

        User developer = devOptional.get();

        // If the logged-in user clicks on their own profile, redirect them to their Dashboard
        if (principal != null && developer.getEmail().equals(principal.getName())) {
            return "redirect:/dashboard";
        }

        // Add data to the model for the public profile
        model.addAttribute("dev", developer);
        return "developer-profile"; // We will create its frontend page later
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model, Principal principal) {
        if (principal != null) {
            userRepository.findByEmail(principal.getName())
                    .ifPresent(user -> model.addAttribute("sessionUser", user));
        }
    }

    // Handle adding a new project
    @PostMapping("/addProject")
    public String addProject(Project project, Principal principal) {
        if (principal != null) {
            userRepository.findByEmail(principal.getName()).ifPresent(user -> {
                // Project ko current logged-in user se link karo
                project.setUser(user);
                // Database me save kar do
                projectRepository.save(project);
            });
        }
        return "redirect:/dashboard?projectAdded=true";
    }

    // Handle adding new education
    @PostMapping("/addEducation")
    public String addEducation(Education education, Principal principal) {
        if (principal != null) {
            userRepository.findByEmail(principal.getName()).ifPresent(user -> {
                // Education record ko current user se link karo
                education.setUser(user);
                // Database me save kar do
                educationRepository.save(education);
            });
        }
        return "redirect:/dashboard?educationAdded=true";
    }
}