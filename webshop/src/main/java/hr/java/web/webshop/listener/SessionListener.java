package hr.java.web.webshop.listener;

import hr.java.web.webshop.model.User;
import hr.java.web.webshop.model.UserSession;
import hr.java.web.webshop.service.UserService;
import hr.java.web.webshop.service.UserSessionService;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@WebListener
public class SessionListener implements HttpSessionListener {

    private static final AtomicInteger activeSessions = new AtomicInteger(0);
    private static final String IP_ADDRESS_MESSAGE = "IP Address: ";

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        int currentSessions = activeSessions.incrementAndGet();
        String sessionId = se.getSession().getId();

        // Dobivanje IP adrese
        String ipAddress = null;
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            ipAddress = attr.getRequest().getRemoteAddr();
            log.info(IP_ADDRESS_MESSAGE + ipAddress);
        } catch (Exception e) {
            log.warn("Could not retrieve IP address: {}", e.getMessage());
        }

        log.info("New session created: {} - Total active sessions: {}", sessionId, currentSessions);

        se.getSession().setMaxInactiveInterval(30 * 60);

        // Dobivanje Spring servisa kroz WebApplicationContext
        try {
            WebApplicationContext context = WebApplicationContextUtils
                    .getWebApplicationContext(se.getSession().getServletContext());

            if (context != null) {
                UserSessionService userSessionService = context.getBean(UserSessionService.class);

                // NOVO: Provjeri postoji li već sesija s ovim ID-om
                UserSession existingSession = userSessionService.findBySessionId(sessionId);

                if (existingSession != null) {
                    // Ako sesija postoji, samo je reaktiviraj
                    log.info("Session {} already exists, reactivating...", sessionId);
                    existingSession.setActive(true);
                    existingSession.setLastActivity(LocalDateTime.now());
                    if (ipAddress != null) {
                        existingSession.setIpAddress(ipAddress);
                    }
                    userSessionService.saveUserSession(existingSession);
                } else {
                    // Kreiraj novu sesiju
                    UserSession userSession = new UserSession();
                    userSession.setSessionId(sessionId);
                    userSession.setCreatedAt(LocalDateTime.now());
                    userSession.setActive(true);
                    userSession.setIpAddress(ipAddress);

                    userSessionService.saveUserSession(userSession);
                    log.debug("New session {} saved to database", sessionId);
                }
            }
        } catch (Exception e) {
            log.error("Error saving session to database: {}", e.getMessage(), e);
        }

        logSessionStatistics(currentSessions);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        int currentSessions = activeSessions.decrementAndGet();
        String sessionId = se.getSession().getId();

        log.info("Session destroyed: {} - Total active sessions: {}", sessionId, currentSessions);

        try {
            WebApplicationContext context = WebApplicationContextUtils
                    .getWebApplicationContext(se.getSession().getServletContext());

            if (context != null) {
                UserSessionService userSessionService = context.getBean(UserSessionService.class);
                userSessionService.deactivateSession(sessionId);
                log.debug("Session {} marked as inactive in database", sessionId);
            }
        } catch (Exception e) {
            log.error("Error updating session in database: {}", e.getMessage(), e);
        }

        logSessionStatistics(currentSessions);
    }

    public static int getActiveSessionsCount() {
        return activeSessions.get();
    }

    private void logSessionStatistics(int currentSessions) {
        if (currentSessions % 10 == 0 && currentSessions > 0) {
            log.info("Session milestone reached: {} active sessions", currentSessions);
        }

        if (currentSessions > 100) {
            log.warn("High number of active sessions detected: {}", currentSessions);
        }
    }

    public void updateSessionWithUser(String sessionId, String username, String ipAddress, String userAgent) {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attr != null) {
                WebApplicationContext context = WebApplicationContextUtils
                        .getWebApplicationContext(attr.getRequest().getServletContext());

                if (context != null) {
                    UserSessionService userSessionService = context.getBean(UserSessionService.class);
                    UserService userService = context.getBean(UserService.class);

                    Optional<User> userOpt = userService.findByUsername(username);
                    if (userOpt.isPresent()) {
                        UserSession session = userSessionService.findBySessionId(sessionId);
                        if (session != null) {
                            session.setUser(userOpt.get());
                            session.setIpAddress(ipAddress);
                            session.setUserAgent(userAgent);
                            session.setLastActivity(LocalDateTime.now());
                            userSessionService.saveUserSession(session);

                            log.info("Session {} linked to user: {}", sessionId, username);
                        } else {
                            log.warn("Session {} not found when trying to link to user: {}", sessionId, username);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error updating session with user info: {}", e.getMessage(), e);
        }
    }
}