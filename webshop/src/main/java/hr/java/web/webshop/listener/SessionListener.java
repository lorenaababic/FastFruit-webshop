package hr.java.web.webshop.listener;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class SessionListener implements HttpSessionListener {

    private static final String IP_ADDRESS_MESSAGE = "IP Address: ";
    private static final AtomicInteger activeSessions = new AtomicInteger(0);


    @Override
    public void sessionCreated(HttpSessionEvent event) {
        String ipAddr = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest().getRemoteAddr();

        log.info(IP_ADDRESS_MESSAGE + ipAddr);
        log.info("New session created: {}", event.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        log.info("Session destroyed: {}", event.getSession().getId());
    }

    public static int getActiveSessionsCount() {
        return activeSessions.get();
    }

    public void updateSessionWithUser(String sessionId, String username, String ipAddress, String userAgent) {
        log.info("Session {} linked to user: {} from IP: {}", sessionId, username, ipAddress);
    }
}