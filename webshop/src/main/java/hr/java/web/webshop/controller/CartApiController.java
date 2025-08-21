package hr.java.web.webshop.controller;

import hr.java.web.webshop.dto.CartDto;
import hr.java.web.webshop.model.Cart;
import hr.java.web.webshop.request.CartRequest;
import hr.java.web.webshop.request.RemoveCartRequest;
import hr.java.web.webshop.response.CartResponse;
import hr.java.web.webshop.service.CartService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/cart/api")
@Validated
public class CartApiController {
    private static final Logger logger = LoggerFactory.getLogger(CartApiController.class);
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("50");
    private static final BigDecimal SHIPPING_COST = new BigDecimal("5.00");

    private final CartService cartService;

    @Autowired
    public CartApiController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add/{productId}")
    public ResponseEntity<?> apiAddToCart(@PathVariable Long productId,
                                          @RequestParam(defaultValue = "1") Integer quantity) {
        try {
            logger.info("API: Dodajem proizvod ID: {} u količini: {} u košaricu", productId, quantity);
            Cart updatedCart = cartService.addToCart(productId, quantity);

            return createCartSummaryResponse(true, "Proizvod uspješno dodan u košaricu");
        } catch (Exception e) {
            logger.error("API: Greška prilikom dodavanja proizvoda u košaricu: {}", e.getMessage());
            return createErrorResponse(e.getMessage());
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> getCartCount() {
        Cart cart = cartService.getCart();
        int count = cart != null ? cart.getTotalQuantity() : 0;
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        logger.debug("API: Dohvaćam broj stavki u košarici: {}", count);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateCartItemApi(@RequestBody @Valid CartRequest request) {
        try {
            cartService.updateCartItem(request.getProductId(), request.getQuantity());

            return createCartSummaryResponse(true, "Količina ažurirana");
        } catch (Exception e) {
            logger.error("API: Greška prilikom ažuriranja košarice: {}", e.getMessage());
            return createErrorResponse(e.getMessage());
        }
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeCartItemApi(@RequestBody RemoveCartRequest request) {
        try {
            Cart updatedCart = cartService.removeCartItem(request.getProductId());

            return createCartSummaryResponse(true, "Proizvod uklonjen iz košarice");
        } catch (Exception e) {
            logger.error("API: Greška prilikom uklanjanja proizvoda iz košarice: {}", e.getMessage());
            return createErrorResponse(e.getMessage());
        }
    }

    @PostMapping("/clear")
    public ResponseEntity<?> apiClearCart() {
        logger.info("API: Praznim košaricu");
        cartService.clearCart();

        return createCartSummaryResponse(true, "Košarica uspješno ispražnjena");
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getCartSummary() {
        Cart cart = cartService.getCart();
        boolean freeShipping = isFreeShipping(cart.getTotalAmount());
        BigDecimal finalAmount = calculateFinalAmount(cart.getTotalAmount(), freeShipping);

        Map<String, Object> response = new HashMap<>();
        response.put("itemCount", cart.getTotalQuantity());
        response.put("subtotal", cart.getTotalAmount());
        response.put("freeShipping", freeShipping);
        response.put("shippingCost", freeShipping ? BigDecimal.ZERO : SHIPPING_COST);
        response.put("total", finalAmount);

        logger.debug("API: Dohvaćam sažetak košarice, ukupno: {}", finalAmount);
        return ResponseEntity.ok(response);
    }

    private boolean isFreeShipping(BigDecimal subtotal) {
        return subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0;
    }

    private BigDecimal calculateFinalAmount(BigDecimal subtotal, boolean freeShipping) {
        return subtotal.add(freeShipping ? BigDecimal.ZERO : SHIPPING_COST);
    }

    private ResponseEntity<?> createCartSummaryResponse(boolean success, String message) {
        Cart cart = cartService.getCart();
        boolean freeShipping = isFreeShipping(cart.getTotalAmount());

        CartDto response = new CartDto();
        response.setSuccess(success);
        response.setMessage(message);
        response.setItemCount(cart.getTotalQuantity());
        response.setTotal(cart.getTotalAmount());
        response.setFreeShipping(freeShipping);
        response.setShippingCost(freeShipping ? BigDecimal.ZERO : SHIPPING_COST);
        response.setFinalAmount(calculateFinalAmount(cart.getTotalAmount(), freeShipping));

        return ResponseEntity.ok(response);
    }

    private ResponseEntity<?> createErrorResponse(String message) {
        CartResponse response = new CartResponse();
        response.setSuccess(false);
        response.setMessage(message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}