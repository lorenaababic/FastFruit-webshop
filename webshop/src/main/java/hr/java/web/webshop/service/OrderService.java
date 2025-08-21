package hr.java.web.webshop.service;

import hr.java.web.webshop.model.Order;
import hr.java.web.webshop.model.OrderStatus;
import hr.java.web.webshop.model.PaymentMethod;
import hr.java.web.webshop.model.User;
import hr.java.web.webshop.response.PayPalCaptureResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderService {
    Order createOrder(User user, PaymentMethod paymentMethod, String shippingAddress);
    Optional<Order> getOrderById(Long id);
    List<Order> getAllOrders();
    List<Order> getOrdersByUser(User user);
    List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<Order> getOrdersByUserAndDateRange(User user, LocalDateTime startDate, LocalDateTime endDate);
    void updateOrderStatus(Long orderId, OrderStatus status);
    List<Order> getOrdersByPaymentMethod(String paymentMethod);
    List<Order> getOrdersByDateRangeAndPaymentMethod(LocalDateTime startDate, LocalDateTime endDate, String paymentMethod);
    List<Order> getOrdersByUserAndPaymentMethod(User user, String paymentMethod);
    List<Order> getOrdersByUserDateRangeAndPaymentMethod(User user, LocalDateTime startDate,
                                                         LocalDateTime endDate, String paymentMethod);
    List<String> getAllPaymentMethods();
    void cancelOrderById(Long id);
    Order createOrderWithPayPalId(User user, PaymentMethod paymentMethod, String shippingAddress, String name, String email, String id);
    Optional<Order> findByPayPalOrderId(String orderID);
    void completePayment(Long id, PayPalCaptureResponse captureResponse);
}