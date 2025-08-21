package hr.java.web.webshop.controller;

import hr.java.web.webshop.model.Order;
import hr.java.web.webshop.model.User;
import hr.java.web.webshop.service.OrderService;
import hr.java.web.webshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String viewOrders(Model model,
                             @RequestParam(required = false) String username,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                             @RequestParam(required = false) String paymentMethod) {

        List<Order> orders;
        LocalDateTime startDateTime = startDate != null ?
                LocalDateTime.of(startDate, LocalTime.MIN) : null;
        LocalDateTime endDateTime = endDate != null ?
                LocalDateTime.of(endDate, LocalTime.MAX) : null;

        if (username != null && !username.isEmpty()) {
            Optional<User> user = userService.findByUsername(username);
            if (user.isPresent()) {
                if (startDateTime != null && endDateTime != null) {
                    if (paymentMethod != null && !paymentMethod.isEmpty()) {
                        orders = orderService.getOrdersByUserDateRangeAndPaymentMethod(
                                user.get(), startDateTime, endDateTime, paymentMethod);
                    } else {
                        orders = orderService.getOrdersByUserAndDateRange(
                                user.get(), startDateTime, endDateTime);
                    }
                } else if (paymentMethod != null && !paymentMethod.isEmpty()) {
                    orders = orderService.getOrdersByUserAndPaymentMethod(
                            user.get(), paymentMethod);
                } else {
                    orders = orderService.getOrdersByUser(user.get());
                }
            } else {
                orders = orderService.getAllOrders();
                model.addAttribute("error", "User not found: " + username);
            }
        } else if (startDateTime != null && endDateTime != null) {
            if (paymentMethod != null && !paymentMethod.isEmpty()) {
                orders = orderService.getOrdersByDateRangeAndPaymentMethod(
                        startDateTime, endDateTime, paymentMethod);
            } else {
                orders = orderService.getOrdersByDateRange(startDateTime, endDateTime);
            }
        } else if (paymentMethod != null && !paymentMethod.isEmpty()) {
            orders = orderService.getOrdersByPaymentMethod(paymentMethod);
        } else {
            orders = orderService.getAllOrders();
        }

        model.addAttribute("orders", orders);

        List<String> paymentMethods = orderService.getAllPaymentMethods();
        model.addAttribute("paymentMethods", paymentMethods);

        return "admin/orders";
    }

    @GetMapping("/{id}")
    public String viewOrderDetails(@PathVariable Long id, Model model) {
        Optional<Order> order = orderService.getOrderById(id);
        if (order.isPresent()) {
            model.addAttribute("order", order.get());
            return "admin/order-details";
        }
        return "redirect:/admin/orders";
    }
}