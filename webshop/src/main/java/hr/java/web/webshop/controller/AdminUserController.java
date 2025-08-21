package hr.java.web.webshop.controller;

import hr.java.web.webshop.model.Role;
import hr.java.web.webshop.model.User;
import hr.java.web.webshop.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/user/list";
    }

    @GetMapping("/new")
    public String newUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", Role.values());
        return "admin/user/form";
    }

    @PostMapping("/save")
    public String saveUser(@Valid @ModelAttribute("user") User user,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/user/form";
        }

        try {
            if (user.getId() == null) {
                if (userService.usernameExists(user.getUsername())) {
                    result.rejectValue("username", "error.user", "Username already exists");
                    return "admin/user/form";
                }
                if (userService.emailExists(user.getEmail())) {
                    result.rejectValue("email", "error.user", "Email already exists");
                    return "admin/user/form";
                }
                userService.registerNewUser(user);
                redirectAttributes.addFlashAttribute("message", "User created successfully");
            } else {
                User existingUser = userService.findByUsername(user.getUsername())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                if (user.getPassword() == null || user.getPassword().isEmpty()) {
                    user.setPassword(existingUser.getPassword());
                }

                userService.save(user);
                redirectAttributes.addFlashAttribute("message", "User updated successfully");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving user: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @GetMapping("/edit/{username}")
    public String editUserForm(@PathVariable String username, Model model) {
        Optional<User> user = userService.findByUsername(username);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            model.addAttribute("roles", Role.values());
            return "admin/user/form";
        }
        return "redirect:/admin/users";
    }
}