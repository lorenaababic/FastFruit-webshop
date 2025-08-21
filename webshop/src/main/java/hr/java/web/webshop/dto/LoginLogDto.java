package hr.java.web.webshop.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginLogDto {
    private String username;
    private LocalDateTime loginTime;
    private String ipAddress;
}
