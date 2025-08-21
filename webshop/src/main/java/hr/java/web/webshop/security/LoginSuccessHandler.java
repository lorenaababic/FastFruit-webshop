package hr.java.web.webshop.security;

import hr.java.web.webshop.listener.SessionListener;
import hr.java.web.webshop.service.LoginLogService;
import hr.java.web.webshop.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    private LoginLogService loginLogService;

    @Autowired
    private SessionListener sessionListener;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String sessionId = request.getSession().getId();

        log.info("Successful login for user: {} from IP: {}", username, ipAddress);

        userService.findByUsername(username).ifPresent(user -> {
            loginLogService.logLogin(user, ipAddress);

            try {
                sessionListener.updateSessionWithUser(sessionId, username, ipAddress, userAgent);
                log.debug("Session {} linked to user: {}", sessionId, username);
            } catch (Exception e) {
                log.error("Error updating session with user info for {}: {}", username, e.getMessage(), e);
            }
        });

        super.onAuthenticationSuccess(request, response, authentication);
    }
}