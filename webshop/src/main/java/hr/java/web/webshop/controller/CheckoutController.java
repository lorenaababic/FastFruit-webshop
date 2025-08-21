package hr.java.web.webshop.controller;

import hr.java.web.webshop.model.Cart;
import hr.java.web.webshop.model.Order;
import hr.java.web.webshop.model.PaymentMethod;
import hr.java.web.webshop.model.User;
import hr.java.web.webshop.service.CartService;
import hr.java.web.webshop.service.OrderService;
import hr.java.web.webshop.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private static final Logger logger = LoggerFactory.getLogger(CheckoutController.class);
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("50");
    private static final BigDecimal SHIPPING_COST = new BigDecimal("5.00");

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Value("${paypal.client.id}")
    private String paypalClientId;

    @GetMapping()
    public String showCheckoutPage(Model model) {
        Cart cart = cartService.getCart();
        logger.debug("Checkout - cart items count: {}", cart != null ? cart.getTotalQuantity() : 0);

        if (cart == null || cart.getItems().isEmpty()) {
            return "redirect:/cart?error=Cart is empty";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> user = userService.findByUsername(auth.getName());

        boolean freeShipping = isFreeShipping(cart.getTotalAmount());
        BigDecimal finalAmount = calculateFinalAmount(cart.getTotalAmount(), freeShipping);

        model.addAttribute("user", user.orElse(null));
        model.addAttribute("cart", cart);
        model.addAttribute("cartItems", cart.getItems().values());
        model.addAttribute("total", cart.getTotalAmount());
        model.addAttribute("freeShipping", freeShipping);
        model.addAttribute("shippingCost", freeShipping ? BigDecimal.ZERO : SHIPPING_COST);
        model.addAttribute("finalAmount", finalAmount);
        model.addAttribute("paymentMethods", PaymentMethod.values());

        model.addAttribute("paypalClientId", paypalClientId);

        logger.info("🔍 PayPal Client ID sent to template: {}", paypalClientId);

        return "checkout/view";
    }

    @PostMapping("/process")
    public String processCheckout(
            @RequestParam("paymentMethod") PaymentMethod paymentMethod,
            @RequestParam("shippingAddress") String shippingAddress,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {

        try {
            if (paymentMethod == PaymentMethod.PAYPAL) {
                redirectAttributes.addFlashAttribute("error", "Koristite PayPal dugme za plaćanje");
                return "redirect:/checkout";
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Optional<User> user = userService.findByUsername(auth.getName());

            Order order = orderService.createOrder(user.orElse(null), paymentMethod, shippingAddress);

            redirectAttributes.addFlashAttribute("orderId", order.getId());
            return "redirect:/checkout/confirmation";

        } catch (Exception e) {
            logger.error("Error processing checkout: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/confirmation")
    public String showConfirmationPage(@RequestParam(required = false) Long orderId, Model model) {
        if (orderId == null && !model.containsAttribute("orderId")) {
            logger.warn("Attempted to access confirmation page without an orderId");
            return "redirect:/";
        }

        if (orderId == null) {
            orderId = (Long) model.asMap().get("orderId");
        }

        Optional<Order> orderOpt = orderService.getOrderById(orderId);

        if (!orderOpt.isPresent()) {
            logger.warn("Order with ID {} not found", orderId);
            return "redirect:/";
        }

        model.addAttribute("order", orderOpt.get());
        return "checkout/confirmation";
    }

    private boolean isFreeShipping(BigDecimal subtotal) {
        return subtotal != null && subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0;
    }

    private BigDecimal calculateFinalAmount(BigDecimal subtotal, boolean freeShipping) {
        if (subtotal == null) {
            return SHIPPING_COST;
        }
        return subtotal.add(freeShipping ? BigDecimal.ZERO : SHIPPING_COST);
    }
}