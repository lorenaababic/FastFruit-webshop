package hr.java.web.webshop.service.impl;

import hr.java.web.webshop.model.Cart;
import hr.java.web.webshop.model.CartItem;
import hr.java.web.webshop.model.Product;
import hr.java.web.webshop.repository.ProductRepository;
import hr.java.web.webshop.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

@Service
@SessionScope
public class CartServiceImpl implements CartService {
    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);
    private Cart cart = new Cart();

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Cart getCart() {
        logger.debug("Getting cart with {} items", cart.getItems().size());
        return cart;
    }

    @Override
    public Cart addToCart(Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock available");
        }

        CartItem cartItem = cart.getItems().get(productId);
        if (cartItem == null) {
            cartItem = new CartItem(product, quantity);
            cart.getItems().put(productId, cartItem);
            logger.debug("Added new product to cart: {} (qty: {})", product.getName(), quantity);
        } else {
            int newQuantity = cartItem.getQuantity() + quantity;
            if (product.getQuantity() < newQuantity) {
                throw new RuntimeException("Insufficient stock available");
            }
            cartItem.setQuantity(newQuantity);
            logger.debug("Updated product quantity in cart: {} (qty: {})", product.getName(), newQuantity);
        }

        return cart;
    }


    @Override
    public void updateCartItem(Long productId, Integer quantity) {
        if (!cart.getItems().containsKey(productId)) {
            throw new RuntimeException("Product not in cart");
        }

        if (quantity <= 0) {
            cart.getItems().remove(productId);
            logger.debug("Removed product from cart (id: {})", productId);
            return;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock available");
        }

        CartItem item = cart.getItems().get(productId);
        item.setQuantity(quantity);
        logger.debug("Updated product quantity: {} (qty: {})", product.getName(), quantity);
    }

    @Override
    public Cart removeCartItem(Long productId) {
        cart.getItems().remove(productId);
        logger.debug("Removed product from cart (id: {})", productId);
        return cart;
    }

    @Override
    public void clearCart() {
        cart.getItems().clear();
        logger.debug("Cart cleared");
    }
}