package hr.java.web.webshop.service.impl;

import hr.java.web.webshop.model.User;
import hr.java.web.webshop.model.UserSession;
import hr.java.web.webshop.repository.UserSessionRepository;
import hr.java.web.webshop.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserSessionServiceImpl implements UserSessionService {

    private static final Logger logger = LoggerFactory.getLogger(UserSessionServiceImpl.class);

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Override
    public UserSession saveUserSession(UserSession userSession) {
        try {
            return userSessionRepository.save(userSession);
        } catch (Exception e) {
            logger.error("Error saving user session: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save user session", e);
        }
    }

    @Override
    public void deactivateSession(String sessionId) {
        try {
            Optional<UserSession> sessionOpt = userSessionRepository.findBySessionId(sessionId);
            if (sessionOpt.isPresent()) {
                UserSession session = sessionOpt.get();
                session.setActive(false);
                session.setEndedAt(LocalDateTime.now());
                userSessionRepository.save(session);
                logger.debug("Session {} deactivated", sessionId);
            }
        } catch (Exception e) {
            logger.error("Error deactivating session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public void deactivateAllSessions() {
        try {
            List<UserSession> activeSessions = userSessionRepository.findByActiveTrue();
            LocalDateTime now = LocalDateTime.now();

            for (UserSession session : activeSessions) {
                session.setActive(false);
                session.setEndedAt(now);
            }

            userSessionRepository.saveAll(activeSessions);
            logger.info("Deactivated {} active sessions", activeSessions.size());
        } catch (Exception e) {
            logger.error("Error deactivating all sessions: {}", e.getMessage(), e);
        }
    }

    @Override
    public void cleanupOldSessions() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
            List<UserSession> oldSessions = userSessionRepository.findByCreatedAtBefore(cutoffDate);

            if (!oldSessions.isEmpty()) {
                userSessionRepository.deleteAll(oldSessions);
                logger.info("Cleaned up {} old sessions", oldSessions.size());
            }
        } catch (Exception e) {
            logger.error("Error cleaning up old sessions: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<UserSession> getActiveSessions() {
        try {
            return userSessionRepository.findByActiveTrue();
        } catch (Exception e) {
            logger.error("Error getting active sessions: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public List<UserSession> getSessionsByUser(User user) {
        try {
            return userSessionRepository.findByUser(user);
        } catch (Exception e) {
            logger.error("Error getting sessions for user {}: {}", user.getUsername(), e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public UserSession findBySessionId(String sessionId) {
        try {
            Optional<UserSession> sessionOpt = userSessionRepository.findBySessionId(sessionId);
            return sessionOpt.orElse(null);
        } catch (Exception e) {
            logger.error("Error finding session by ID {}: {}", sessionId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void updateLastActivity(String sessionId) {
        try {
            Optional<UserSession> sessionOpt = userSessionRepository.findBySessionId(sessionId);
            if (sessionOpt.isPresent()) {
                UserSession session = sessionOpt.get();
                session.setLastActivity(LocalDateTime.now());
                userSessionRepository.save(session);
            }
        } catch (Exception e) {
            logger.error("Error updating last activity for session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public long getActiveSessionsCount() {
        try {
            return userSessionRepository.countByActiveTrue();
        } catch (Exception e) {
            logger.error("Error counting active sessions: {}", e.getMessage(), e);
            return 0;
        }
    }
}