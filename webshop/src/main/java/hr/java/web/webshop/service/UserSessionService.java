package hr.java.web.webshop.service;

import hr.java.web.webshop.model.User;
import hr.java.web.webshop.model.UserSession;

import java.util.List;

public interface UserSessionService {
    UserSession saveUserSession(UserSession userSession);
    void deactivateSession(String sessionId);
    void deactivateAllSessions();
    void cleanupOldSessions();
    List<UserSession> getActiveSessions();
    List<UserSession> getSessionsByUser(User user);
    UserSession findBySessionId(String sessionId);
    void updateLastActivity(String sessionId);
    long getActiveSessionsCount();
}