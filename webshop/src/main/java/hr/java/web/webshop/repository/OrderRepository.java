package hr.java.web.webshop.repository;

import hr.java.web.webshop.model.Order;
import hr.java.web.webshop.model.PaymentMethod;
import hr.java.web.webshop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Order> findByUserAndCreatedAtBetween(User user, LocalDateTime startDate, LocalDateTime endDate);
    List<Order> findByPaymentMethod(PaymentMethod paymentMethod);
    List<Order> findByCreatedAtBetweenAndPaymentMethod(LocalDateTime startDate, LocalDateTime endDate, PaymentMethod paymentMethod);
    List<Order> findByUserAndPaymentMethod(User user, PaymentMethod paymentMethod);
    List<Order> findByUserAndCreatedAtBetweenAndPaymentMethod(User user, LocalDateTime startDate, LocalDateTime endDate, PaymentMethod paymentMethod);
}