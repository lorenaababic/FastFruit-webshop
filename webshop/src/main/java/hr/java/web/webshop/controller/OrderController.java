package hr.java.web.webshop.controller;

import hr.java.web.webshop.model.Order;
import hr.java.web.webshop.model.User;
import hr.java.web.webshop.security.CustomUserDetails;
import hr.java.web.webshop.service.OrderService;
import hr.java.web.webshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @GetMapping()
    public String getOrders(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        User user = userService.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Order> orders = orderService.getOrdersByUser(user);
        model.addAttribute("orders", orders);

        return "/order/order-history";
    }

    @GetMapping("/{id}")
    public String getOrderDetails(@PathVariable Long id,
                                  @AuthenticationPrincipal CustomUserDetails currentUser,
                                  Model model) {
        User user = userService.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Order> orderOpt = orderService.getOrderById(id);

        if (orderOpt.isEmpty() || !orderOpt.get().getUser().getId().equals(user.getId())) {
            return "redirect:/order/order-history";
        }

        model.addAttribute("order", orderOpt.get());
        return "/order/order-detail";
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id,
                              @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userService.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Order> orderOpt = orderService.getOrderById(id);

        if (orderOpt.isEmpty() || !orderOpt.get().getUser().getId().equals(user.getId())) {
            return "redirect:/order";
        }

        orderService.cancelOrderById(id);

        return "redirect:/order";
    }

    @GetMapping("/confirmation/{id}")
    public String confirmOrder(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails currentUser,
                               Model model) {
        User user = userService.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Order> orderOpt = orderService.getOrderById(id);

        if (orderOpt.isEmpty() || !orderOpt.get().getUser().getId().equals(user.getId())) {
            return "redirect:/order";
        }

        model.addAttribute("order", orderOpt.get());
        return "/order/order-confirmation";
    }
}