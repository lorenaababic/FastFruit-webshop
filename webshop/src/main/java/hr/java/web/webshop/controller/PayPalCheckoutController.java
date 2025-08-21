package hr.java.web.webshop.controller;

import hr.java.web.webshop.response.PayPalCreateOrderResponse;
import hr.java.web.webshop.response.PayPalCaptureOrderResponse;
import hr.java.web.webshop.model.Cart;
import hr.java.web.webshop.model.Order;
import hr.java.web.webshop.model.PaymentMethod;
import hr.java.web.webshop.model.User;
import hr.java.web.webshop.request.CheckoutRequest;
import hr.java.web.webshop.response.PayPalCaptureResponse;
import hr.java.web.webshop.response.PayPalOrderResponse;
import hr.java.web.webshop.service.CartService;
import hr.java.web.webshop.service.OrderService;
import hr.java.web.webshop.service.PayPalService;
import hr.java.web.webshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/checkout/paypal")
@RequiredArgsConstructor
@Validated
public class PayPalCheckoutController {

    private static final Logger logger = LoggerFactory.getLogger(PayPalCheckoutController.class);


    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("50.00");
    private static final BigDecimal SHIPPING_COST = new BigDecimal("5.00");
    private static final String EUR_CURRENCY = "EUR";

    private final CartService cartService;
    private final OrderService orderService;
    private final UserService userService;
    private final PayPalService paypalService;


    @PostMapping("/create-order")
    @ResponseBody
    @Transactional
    public ResponseEntity<PayPalCreateOrderResponse> createPayPalOrder(@Valid @RequestBody CheckoutRequest request) {
        try {
            logger.info("🚀 PayPal CREATE ORDER - Start");
            logger.info("📝 Request: name='{}', email='{}', address='{}'",
                    request.getName(), request.getEmail(), request.getShippingAddress());

            Cart cart = cartService.getCart();
            if (cart == null || cart.isEmpty()) {
                logger.warn("❌ Cart is empty");
                return ResponseEntity.badRequest()
                        .body(PayPalCreateOrderResponse.error("Košarica je prazna"));
            }

            logger.info("🛒 Cart total: {} EUR", cart.getTotalAmount());

            BigDecimal finalAmount = calculateFinalAmount(cart.getTotalAmount());
            logger.info("💰 Final amount (with shipping): {} EUR", finalAmount);

            validateCheckoutRequest(request);

            logger.info("📞 Calling PayPal API...");
            PayPalOrderResponse paypalServiceResponse = paypalService.createOrder(request, finalAmount, EUR_CURRENCY);

            if (paypalServiceResponse.hasError()) {
                logger.error("❌ PayPal Service Error: {}", paypalServiceResponse.getError());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(PayPalCreateOrderResponse.error(paypalServiceResponse.getError()));
            }

            logger.info("✅ PayPal Order created: {}", paypalServiceResponse.getId());

            User currentUser = getCurrentUser();
            logger.info("👤 User: {}", currentUser != null ? currentUser.getUsername() : "Guest");

            Order databaseOrder = orderService.createOrderWithPayPalId(
                    currentUser,
                    PaymentMethod.PAYPAL,
                    request.getShippingAddress(),
                    request.getName(),
                    request.getEmail(),
                    paypalServiceResponse.getId()
            );

            logger.info("✅ Database order created: {}", databaseOrder.getId());
            logger.info("🎉 CREATE ORDER SUCCESS");

            return ResponseEntity.ok(PayPalCreateOrderResponse.success(
                    paypalServiceResponse.getId(),
                    databaseOrder.getId()
            ));

        } catch (IllegalArgumentException e) {
            logger.warn("🚫 Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(PayPalCreateOrderResponse.error("Neispravni podaci: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("💥 Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PayPalCreateOrderResponse.error("Greška: " + e.getMessage()));
        }
    }


    @PostMapping("/capture-order")
    @ResponseBody
    @Transactional
    public ResponseEntity<PayPalCaptureOrderResponse> capturePayPalOrder(
            @RequestBody Map<String, String> request) {
        try {
            String paypalOrderId = request.get("orderID");
            logger.info("🎯 PayPal CAPTURE ORDER: {}", paypalOrderId);

            if (paypalOrderId == null || paypalOrderId.trim().isEmpty()) {
                logger.warn("❌ No PayPal Order ID");
                return ResponseEntity.badRequest()
                        .body(PayPalCaptureOrderResponse.error("PayPal Order ID je obavezan"));
            }

            logger.info("📞 Calling PayPal Capture API...");
            PayPalCaptureResponse paypalServiceResponse = paypalService.captureOrder(paypalOrderId);

            if (paypalServiceResponse.hasError()) {
                logger.error("❌ PayPal Capture Error: {}", paypalServiceResponse.getError());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(PayPalCaptureOrderResponse.error(paypalServiceResponse.getError()));
            }

            logger.info("✅ PayPal capture successful");

            Optional<Order> orderOpt = orderService.findByPayPalOrderId(paypalOrderId);
            if (!orderOpt.isPresent()) {
                logger.error("❌ Database order not found for PayPal ID: {}", paypalOrderId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(PayPalCaptureOrderResponse.error("Narudžba nije pronađena"));
            }

            Order databaseOrder = orderOpt.get();
            logger.info("✅ Found database order: {}", databaseOrder.getId());

            orderService.completePayment(databaseOrder.getId(), paypalServiceResponse);

            cartService.clearCart();

            logger.info("🎉 CAPTURE SUCCESS for order: {}", databaseOrder.getId());

            String redirectUrl = "/checkout/confirmation?orderId=" + databaseOrder.getId();
            return ResponseEntity.ok(PayPalCaptureOrderResponse.success(
                    databaseOrder.getId(),
                    redirectUrl
            ));

        } catch (Exception e) {
            logger.error("💥 Capture error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PayPalCaptureOrderResponse.error("Greška: " + e.getMessage()));
        }
    }

    @GetMapping("/test")
    @ResponseBody
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("✅ PayPal Controller works! " + java.time.LocalDateTime.now());
    }

    @GetMapping("/test-create")
    @ResponseBody
    public ResponseEntity<PayPalCreateOrderResponse> testCreateResponse() {
        return ResponseEntity.ok(PayPalCreateOrderResponse.success("TEST-PAYPAL-ID", 999L));
    }

    private void validateCheckoutRequest(CheckoutRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Ime je obavezno");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email je obavezan");
        }
        if (request.getShippingAddress() == null || request.getShippingAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Adresa je obavezna");
        }
        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Neispravni email format");
        }
    }

    private BigDecimal calculateFinalAmount(BigDecimal subtotal) {
        if (subtotal == null) return SHIPPING_COST;

        boolean freeShipping = subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0;
        BigDecimal shipping = freeShipping ? BigDecimal.ZERO : SHIPPING_COST;

        return subtotal.add(shipping);
    }

    private User getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                return userService.findByUsername(auth.getName()).orElse(null);
            }
            return null;
        } catch (Exception e) {
            logger.warn("⚠️ Error getting user: {}", e.getMessage());
            return null;
        }
    }
}