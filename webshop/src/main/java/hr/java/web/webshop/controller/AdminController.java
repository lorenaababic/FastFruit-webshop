package hr.java.web.webshop.controller;

import hr.java.web.webshop.listener.SessionListener;
import hr.java.web.webshop.model.LoginLog;
import hr.java.web.webshop.model.User;
import hr.java.web.webshop.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private LoginLogService loginLogService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @GetMapping("")
    public String panel(Model model) {
        model.addAttribute("totalUsers", userService.findAll().size());
        model.addAttribute("totalProducts", productService.findAll().size());
        model.addAttribute("totalOrders", orderService.getAllOrders().size());

        model.addAttribute("activeSessions", SessionListener.getActiveSessionsCount());

        return "admin/panel";
    }

    @GetMapping("/login-logs")
    public String viewLoginLogs(Model model,
                                @RequestParam(required = false) String username,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<LoginLog> logs;
        LocalDateTime startDateTime = startDate != null ?
                LocalDateTime.of(startDate, LocalTime.MIN) : null;
        LocalDateTime endDateTime = endDate != null ?
                LocalDateTime.of(endDate, LocalTime.MAX) : null;

        if (username != null && !username.isEmpty()) {
            Optional<User> user = userService.findByUsername(username);
            if (user.isPresent()) {
                if (startDateTime != null && endDateTime != null) {
                    logs = loginLogService.getLogsByUserAndDateRange(user.get(), startDateTime, endDateTime);
                } else {
                    logs = loginLogService.getLogsByUser(user.get());
                }
            } else {
                logs = loginLogService.getAllLogs();
                model.addAttribute("error", "User not found: " + username);
            }
        } else if (startDateTime != null && endDateTime != null) {
            logs = loginLogService.getLogsByDateRange(startDateTime, endDateTime);
        } else {
            logs = loginLogService.getAllLogs();
        }

        model.addAttribute("logs", logs);

        model.addAttribute("activeSessions", SessionListener.getActiveSessionsCount());

        return "admin/login-logs";
    }

    @GetMapping("/sessions")
    public String viewActiveSessions(Model model) {
        model.addAttribute("activeSessions", SessionListener.getActiveSessionsCount());
        return "admin/sessions";
    }
}