package hr.java.web.webshop.repository;

import hr.java.web.webshop.model.User;
import hr.java.web.webshop.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findBySessionId(String sessionId);
    List<UserSession> findByActiveTrue();
    List<UserSession> findByUser(User user);
    List<UserSession> findByCreatedAtBefore(LocalDateTime date);
    long countByActiveTrue();
}