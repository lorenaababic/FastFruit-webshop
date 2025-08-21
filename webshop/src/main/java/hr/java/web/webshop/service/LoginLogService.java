package hr.java.web.webshop.service;

import hr.java.web.webshop.model.LoginLog;
import hr.java.web.webshop.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface LoginLogService {
    LoginLog logLogin(User user, String ipAddress);
    List<LoginLog> getAllLogs();
    List<LoginLog> getLogsByUser(User user);
    List<LoginLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end);
    List<LoginLog> getLogsByUserAndDateRange(User user, LocalDateTime start, LocalDateTime end);
}
