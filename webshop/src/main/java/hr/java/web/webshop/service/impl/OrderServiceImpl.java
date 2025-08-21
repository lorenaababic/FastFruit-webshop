package hr.java.web.webshop.service.impl;

import hr.java.web.webshop.model.*;
import hr.java.web.webshop.repository.OrderItemRepository;
import hr.java.web.webshop.repository.OrderRepository;
import hr.java.web.webshop.repository.PaymentRepository;
import hr.java.web.webshop.repository.ProductRepository;
import hr.java.web.webshop.response.PayPalCaptureResponse;
import hr.java.web.webshop.service.CartService;
import hr.java.web.webshop.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private PaymentRepository paymentRepository;


    @Override
    @Transactional
    public Order createOrder(User user, PaymentMethod paymentMethod, String shippingAddress) {
        Cart cart = cartService.getCart();

        if (cart.isEmpty()) {
            throw new RuntimeException("Cannot create order with empty cart");
        }

        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(cart.getTotalAmount());
        order.setPaymentMethod(paymentMethod);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(shippingAddress);
        order.setCreatedAt(LocalDateTime.now());

        order = orderRepository.save(order);

        Map<Long, CartItem> cartItems = cartService.getCart().getItems();
        for (CartItem cartItem : cartItems.values()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getSubtotal());

            order.getItems().add(orderItem);

            Product product = cartItem.getProduct();
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        orderRepository.save(order);

        cartService.clearCart();

        return order;
    }

    @Override
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }

    @Override
    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByCreatedAtBetween(startDate, endDate);
    }

    @Override
    public List<Order> getOrdersByUserAndDateRange(User user, LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByUserAndCreatedAtBetween(user, startDate, endDate);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            orderRepository.save(order);
        } else {
            throw new RuntimeException("Order not found");
        }
    }

    @Override
    public List<Order> getOrdersByPaymentMethod(String paymentMethod) {
        try {
            PaymentMethod method = PaymentMethod.valueOf(paymentMethod);
            return orderRepository.findByPaymentMethod(method);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    @Override
    public List<Order> getOrdersByDateRangeAndPaymentMethod(LocalDateTime startDate, LocalDateTime endDate, String paymentMethod) {
        try {
            PaymentMethod method = PaymentMethod.valueOf(paymentMethod);
            return orderRepository.findByCreatedAtBetweenAndPaymentMethod(startDate, endDate, method);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    @Override
    public List<Order> getOrdersByUserAndPaymentMethod(User user, String paymentMethod) {
        try {
            PaymentMethod method = PaymentMethod.valueOf(paymentMethod);
            return orderRepository.findByUserAndPaymentMethod(user, method);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    @Override
    public List<Order> getOrdersByUserDateRangeAndPaymentMethod(User user, LocalDateTime startDate, LocalDateTime endDate, String paymentMethod) {
        try {
            PaymentMethod method = PaymentMethod.valueOf(paymentMethod);
            return orderRepository.findByUserAndCreatedAtBetweenAndPaymentMethod(user, startDate, endDate, method);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    @Override
    public List<String> getAllPaymentMethods() {
        return Arrays.stream(PaymentMethod.values())
                .map(Enum::name)
                .toList();
    }

    @Override
    public void cancelOrderById(Long id) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        orderOpt.ifPresent(order -> {
            order.setStatus(OrderStatus.CANCELED);
            orderRepository.save(order);
        });
    }

    @Override
    public Order createOrderWithPayPalId(User user, PaymentMethod paymentMethod, String shippingAddress, String name, String email, String paypalOrderId) {
        try {
            Cart cart = cartService.getCart();
            if (cart == null || cart.getItems().isEmpty()) {
                throw new RuntimeException("Košarica je prazna");
            }

            BigDecimal totalAmount = cart.getTotalAmount();

            if (totalAmount.compareTo(new BigDecimal("50")) < 0) {
                totalAmount = totalAmount.add(new BigDecimal("5.00"));
            }

            Order order = new Order();
            order.setUser(user);
            order.setTotalAmount(totalAmount);
            order.setPaymentMethod(paymentMethod);
            order.setStatus(OrderStatus.PENDING);
            order.setShippingAddress(shippingAddress);
            order.setCreatedAt(LocalDateTime.now());

            Order savedOrder = orderRepository.save(order);

            for (CartItem cartItem : cart.getItems().values()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(savedOrder);
                orderItem.setProduct(cartItem.getProduct());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setPrice(cartItem.getProduct().getPrice());

                savedOrder.getItems().add(orderItem);
            }

            Payment payment = new Payment();
            payment.setOrder(savedOrder);
            payment.setPaymentMethod(paymentMethod);
            payment.setOrderStatus(OrderStatus.PENDING);
            payment.setAmount(totalAmount);
            payment.setCurrency("EUR");
            payment.setPaypalPaymentId(paypalOrderId);
            payment.setCreatedAt(LocalDateTime.now());

            savedOrder.setPayment(payment);

            Order finalOrder = orderRepository.save(savedOrder);

            logger.info("Order created with PayPal ID: {} for user: {}", paypalOrderId, user != null ? user.getUsername() : "guest");
            return finalOrder;

        } catch (Exception e) {
            logger.error("Error creating order with PayPal ID: {}", paypalOrderId, e);
            throw new RuntimeException("Greška prilikom stvaranja narudžbe: " + e.getMessage());
        }
    }

    @Override
    public Optional<Order> findByPayPalOrderId(String paypalOrderId) {
        try {
            logger.debug("Finding order by PayPal order ID: {}", paypalOrderId);

            Optional<Payment> paymentOpt = paymentRepository.findByPaypalPaymentId(paypalOrderId);

            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                return Optional.of(payment.getOrder());
            }

            return Optional.empty();

        } catch (Exception e) {
            logger.error("Error finding order by PayPal order ID: {}", paypalOrderId, e);
            return Optional.empty();
        }
    }

    @Override
    public void completePayment(Long orderId, PayPalCaptureResponse captureResponse) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (!orderOpt.isPresent()) {
                throw new RuntimeException("Narudžba s ID-om " + orderId + " nije pronađena");
            }

            Order order = orderOpt.get();

            order.setStatus(OrderStatus.PAID);

            if (order.getPayment() != null) {
                Payment payment = order.getPayment();
                payment.setOrderStatus(OrderStatus.PAID);
                payment.setPaypalPayerId(captureResponse.getPayerId());
                payment.setTransactionId(captureResponse.getId()); // PayPal capture ID
                payment.setUpdatedAt(LocalDateTime.now());

                BigDecimal paidAmount = new BigDecimal(captureResponse.getAmount());
                if (order.getTotalAmount().compareTo(paidAmount) != 0) {
                    logger.warn("Payment amount mismatch for order {}. Expected: {}, Paid: {}",
                            orderId, order.getTotalAmount(), paidAmount);
                }
            }

            Order savedOrder = orderRepository.save(order);

            for (OrderItem orderItem : savedOrder.getItems()) {
                Product product = orderItem.getProduct();
                int newQuantity = product.getQuantity() - orderItem.getQuantity();

                if (newQuantity < 0) {
                    logger.warn("Product {} quantity would become negative: {}",
                            product.getName(), newQuantity);
                    newQuantity = 0;
                }

                product.setQuantity(newQuantity);
                productRepository.save(product);
            }

            logger.info("Payment completed for order {}. PayPal Capture ID: {}",
                    orderId, captureResponse.getId());

        } catch (Exception e) {
            logger.error("Error completing payment for order: {}", orderId, e);
            throw new RuntimeException("Greška prilikom finalizacije plaćanja: " + e.getMessage());
        }
    }
}