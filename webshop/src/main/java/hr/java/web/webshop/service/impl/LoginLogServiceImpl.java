package hr.java.web.webshop.service.impl;

import hr.java.web.webshop.model.LoginLog;
import hr.java.web.webshop.model.User;
import hr.java.web.webshop.repository.LoginLogRepository;
import hr.java.web.webshop.service.LoginLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoginLogServiceImpl implements LoginLogService {

    @Autowired
    private LoginLogRepository loginLogRepository;

    @Override
    public LoginLog logLogin(User user, String ipAddress) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUser(user);
        loginLog.setLoginTime(LocalDateTime.now());
        loginLog.setIpAddress(ipAddress);
        return loginLogRepository.save(loginLog);
    }

    @Override
    public List<LoginLog> getAllLogs() {
        return loginLogRepository.findAllByOrderByLoginTimeDesc();
    }

    @Override
    public List<LoginLog> getLogsByUser(User user) {
        return loginLogRepository.findByUserOrderByLoginTimeDesc(user);
    }

    @Override
    public List<LoginLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return loginLogRepository.findByLoginTimeBetweenOrderByLoginTimeDesc(start, end);
    }

    @Override
    public List<LoginLog> getLogsByUserAndDateRange(User user, LocalDateTime start, LocalDateTime end) {
        return loginLogRepository.findByUserAndLoginTimeBetweenOrderByLoginTimeDesc(user, start, end);
    }
}
