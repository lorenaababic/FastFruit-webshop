package hr.java.web.webshop.controller;

import hr.java.web.webshop.model.User;
import hr.java.web.webshop.security.CustomUserDetails;
import hr.java.web.webshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        User user = userService.findByUsername(currentUser.getUsername()).orElse(null);

        model.addAttribute("user", user);
        return "user/dashboard";
    }

    @GetMapping("/profile")
    public String viewProfile(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        User user = userService.findByUsername(currentUser.getUsername()).orElse(null);
        model.addAttribute("user", user);
        return "user/profile";
    }
}