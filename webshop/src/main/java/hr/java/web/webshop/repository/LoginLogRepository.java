package hr.java.web.webshop.repository;

import hr.java.web.webshop.model.LoginLog;
import hr.java.web.webshop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {
    List<LoginLog> findByUserOrderByLoginTimeDesc(User user);
    List<LoginLog> findAllByOrderByLoginTimeDesc();
    List<LoginLog> findByLoginTimeBetweenOrderByLoginTimeDesc(LocalDateTime start, LocalDateTime end);
    List<LoginLog> findByUserAndLoginTimeBetweenOrderByLoginTimeDesc(User user, LocalDateTime start, LocalDateTime end);
}
