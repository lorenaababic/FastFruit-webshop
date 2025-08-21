package hr.java.web.webshop.controller;

import hr.java.web.webshop.model.Cart;
import hr.java.web.webshop.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;


@Controller
@RequestMapping("/cart")
@Validated
public class CartController {
    private static final Logger logger = LoggerFactory.getLogger(CartController.class);
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("50");
    private static final BigDecimal SHIPPING_COST = new BigDecimal("5.00");

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @ModelAttribute("cart")
    public Cart initCart() {
        return cartService.getCart();
    }

    @ModelAttribute("itemCount")
    public Integer initItemCount() {
        Cart cart = cartService.getCart();
        return cart != null ? cart.getTotalQuantity() : 0;
    }

    @GetMapping()
    public String viewCart(Model model) {
        Cart cart = cartService.getCart();
        if (cart == null) {
            return "redirect:/cart?error=Cart is empty";
        }

        boolean freeShipping = isFreeShipping(cart.getTotalAmount());
        BigDecimal finalAmount = calculateFinalAmount(cart.getTotalAmount(), freeShipping);

        model.addAttribute("cart", cart);
        model.addAttribute("freeShipping", freeShipping);
        model.addAttribute("shippingCost", freeShipping ? BigDecimal.ZERO : SHIPPING_COST);
        model.addAttribute("finalAmount", finalAmount);

        logger.debug("Viewing cart with {} items, total: {}", cart.getTotalQuantity(), finalAmount);
        return "cart/view";
    }

    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable Long productId,
                            @RequestParam(defaultValue = "1") Integer quantity,
                            RedirectAttributes redirectAttributes) {
        try {
            logger.info("Dodajem proizvod ID: {} u količini: {} u košaricu", productId, quantity);
            Cart cart = cartService.addToCart(productId, quantity);

            redirectAttributes.addFlashAttribute("success", "Proizvod dodan u košaricu.");
            return "redirect:/cart";
        } catch (Exception e) {
            logger.error("Greška prilikom dodavanja proizvoda u košaricu: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/products/" + productId;
        }
    }

    @PostMapping("/update/{productId}")
    public String updateQuantity(@PathVariable Long productId,
                                 @RequestParam Integer quantity,
                                 RedirectAttributes redirectAttributes) {
        try {
            logger.info("Ažuriram količinu proizvoda ID: {} na: {}", productId, quantity);
            cartService.updateCartItem(productId, quantity);

            redirectAttributes.addFlashAttribute("success", "Količina proizvoda ažurirana.");
            return "redirect:/cart";
        } catch (Exception e) {
            logger.error("Greška prilikom ažuriranja količine proizvoda: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }

    @PostMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId,
                                 RedirectAttributes redirectAttributes) {
        try {
            logger.info("Uklanjam proizvod ID: {} iz košarice", productId);
            Cart updatedCart = cartService.removeCartItem(productId);

            redirectAttributes.addFlashAttribute("success", "Proizvod uklonjen iz košarice.");
        } catch (Exception e) {
            logger.error("Greška prilikom uklanjanja proizvoda iz košarice: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(RedirectAttributes redirectAttributes) {
        logger.info("Praznim košaricu");
        cartService.clearCart();

        redirectAttributes.addFlashAttribute("success", "Košarica je ispražnjena.");
        return "redirect:/cart";
    }

    private boolean isFreeShipping(BigDecimal subtotal) {
        return subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0;
    }

    private BigDecimal calculateFinalAmount(BigDecimal subtotal, boolean freeShipping) {
        return subtotal.add(freeShipping ? BigDecimal.ZERO : SHIPPING_COST);
    }
}
