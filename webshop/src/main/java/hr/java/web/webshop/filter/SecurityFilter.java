package hr.java.web.webshop.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@Order(2)
public class SecurityFilter implements Filter {

    private static final List<String> DANGEROUS_PATTERNS = Arrays.asList(
            "<script", "</script>", "javascript:", "vbscript:",
            "onload=", "onerror=", "onclick=", "onmouseover=",
            "document.cookie", "document.write", "eval(", "setTimeout(", "setInterval("
    );

    private static final List<String> BLOCKED_USER_AGENTS = Arrays.asList(
            "bot", "crawler", "spider", "scraper"
    );

    private static final List<String> SKIP_DANGEROUS_PARAM_CHECK = Arrays.asList(
            "/admin/", "/api/", "/actuator/", "/checkout/paypal/"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("SecurityFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        log.debug("Processing security filter for URI: {}", httpRequest.getRequestURI());

        addSecurityHeaders(httpResponse, httpRequest);

        if (isBlockedUserAgent(httpRequest)) {
            log.warn("Blocked request from suspicious User-Agent: {}",
                    httpRequest.getHeader("User-Agent"));
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String requestURI = httpRequest.getRequestURI();
        String contentType = httpRequest.getContentType();

        boolean skipDangerousCheck = shouldSkipDangerousCheck(requestURI, contentType);

        if (!skipDangerousCheck && containsDangerousParams(httpRequest)) {
            log.warn("Blocked request with dangerous parameters from IP: {} - URI: {}",
                    httpRequest.getRemoteAddr(), requestURI);
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean shouldSkipDangerousCheck(String requestURI, String contentType) {
        // Check for skip routes
        for (String skipRoute : SKIP_DANGEROUS_PARAM_CHECK) {
            if (requestURI.startsWith(skipRoute)) {
                log.debug("Skipping dangerous parameter check for route: {}", requestURI);
                return true;
            }
        }

        // Check for multipart content
        if (contentType != null && contentType.toLowerCase().startsWith("multipart/form-data")) {
            log.debug("Skipping dangerous parameter check for multipart request");
            return true;
        }

        return false;
    }

    private void addSecurityHeaders(HttpServletResponse response, HttpServletRequest request) {
        String requestURI = request.getRequestURI();

        if (requestURI.startsWith("/checkout/paypal/")) {
            log.debug("Skipping security headers for PayPal endpoint: {}", requestURI);
            return;
        }

        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");

        response.setHeader("Content-Security-Policy",
                "default-src 'self' 'unsafe-inline' 'unsafe-eval' data: https:; " +
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval' " +
                        "https://www.paypal.com https://*.paypal.com https://www.sandbox.paypal.com https://*.sandbox.paypal.com " +
                        "https://cdnjs.cloudflare.com https://cdn.jsdelivr.net; " +
                        "frame-src 'self' " +
                        "https://www.paypal.com https://*.paypal.com https://www.sandbox.paypal.com https://*.sandbox.paypal.com; " +
                        "connect-src 'self' " +
                        "https://api-m.sandbox.paypal.com https://api-m.paypal.com " +
                        "https://*.paypal.com https://*.sandbox.paypal.com wss://*.paypal.com; " +
                        "style-src 'self' 'unsafe-inline' " +
                        "https://cdnjs.cloudflare.com https://cdn.jsdelivr.net https://*.paypal.com; " +
                        "font-src 'self' https://cdnjs.cloudflare.com https://cdn.jsdelivr.net https://*.paypal.com; " +
                        "img-src 'self' data: https:; " +
                        "object-src 'none';");

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
    }

    private boolean isBlockedUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return false;
        }

        String userAgentLower = userAgent.toLowerCase();
        return BLOCKED_USER_AGENTS.stream()
                .anyMatch(blocked -> userAgentLower.contains(blocked.toLowerCase()));
    }

    private boolean containsDangerousParams(HttpServletRequest request) {
        // Check query string
        if (request.getQueryString() != null) {
            String queryLower = request.getQueryString().toLowerCase();
            for (String dangerous : DANGEROUS_PATTERNS) {
                if (queryLower.contains(dangerous.toLowerCase())) {
                    log.warn("Dangerous pattern '{}' found in query string: {}", dangerous, request.getQueryString());
                    return true;
                }
            }
        }

        // Check parameters
        return request.getParameterMap().entrySet().stream()
                .anyMatch(entry -> {
                    String paramName = entry.getKey().toLowerCase();
                    String[] paramValues = entry.getValue();

                    for (String value : paramValues) {
                        if (value != null && !value.isEmpty()) {
                            String valueLower = value.toLowerCase();
                            for (String dangerous : DANGEROUS_PATTERNS) {
                                if (valueLower.contains(dangerous.toLowerCase())) {
                                    log.warn("Dangerous pattern '{}' found in parameter '{}': {}",
                                            dangerous, paramName, value);
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                });
    }

    @Override
    public void destroy() {
        log.info("SecurityFilter destroyed");
    }
}