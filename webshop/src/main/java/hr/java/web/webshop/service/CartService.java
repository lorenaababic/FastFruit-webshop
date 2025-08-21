package hr.java.web.webshop.service;

import hr.java.web.webshop.model.Cart;
import jakarta.servlet.http.HttpSession;

public interface CartService {
    Cart getCart();
    Cart addToCart(Long productId, Integer quantity);
    void updateCartItem(Long productId, Integer quantity);
    Cart removeCartItem(Long productId);
    void clearCart();
}