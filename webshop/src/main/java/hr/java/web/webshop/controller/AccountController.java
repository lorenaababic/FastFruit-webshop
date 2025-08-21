package hr.java.web.webshop.controller;

import hr.java.web.webshop.dto.PasswordChangeDto;
import hr.java.web.webshop.model.Order;
import hr.java.web.webshop.model.User;
import hr.java.web.webshop.security.CustomUserDetails;
import hr.java.web.webshop.service.OrderService;
import hr.java.web.webshop.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String accountDashboard(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        User user = userService.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Order> recentOrders = orderService.getOrdersByUser(user);
        if (recentOrders.size() > 5) {
            recentOrders = recentOrders.subList(0, 5);
        }

        model.addAttribute("user", user);
        model.addAttribute("recentOrders", recentOrders);

        return "account/dashboard";
    }

    @GetMapping("/profile")
    public String viewProfile(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        User user = userService.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        return "account/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails currentUser,
                                @ModelAttribute User updatedUser,
                                RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmail(updatedUser.getEmail());

        userService.save(user);

        redirectAttributes.addFlashAttribute("success", "Profil je uspješno ažuriran.");
        return "redirect:/account/profile";
    }

    @GetMapping("/change-password")
    public String changePasswordForm(Model model) {
        model.addAttribute("passwordChangeDto", new PasswordChangeDto());
        return "account/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal CustomUserDetails currentUser,
                                 @Valid @ModelAttribute PasswordChangeDto passwordChangeDto,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(passwordChangeDto.getCurrentPassword(), user.getPassword())) {
            result.rejectValue("currentPassword", null, "Trenutna lozinka nije ispravna");
        }

        if (!passwordChangeDto.getNewPassword().equals(passwordChangeDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", null, "Lozinke se ne podudaraju");
        }

        if (result.hasErrors()) {
            return "account/change-password";
        }

        user.setPassword(passwordEncoder.encode(passwordChangeDto.getNewPassword()));
        userService.save(user);

        redirectAttributes.addFlashAttribute("success", "Lozinka je uspješno promijenjena.");
        return "redirect:/account/profile";
    }
}