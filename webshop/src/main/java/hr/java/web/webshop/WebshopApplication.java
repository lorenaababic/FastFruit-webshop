package hr.java.web.webshop;

import hr.java.web.webshop.model.Role;
import hr.java.web.webshop.model.User;
import hr.java.web.webshop.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;

@SpringBootApplication
@ServletComponentScan
public class WebshopApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebshopApplication.class, args);
    }

    @Bean
    public CommandLineRunner createAdmin(UserRepository userRepository) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@webshop.hr");
                admin.setPassword(new BCryptPasswordEncoder().encode("admin123"));
                admin.setRole(Role.ROLE_ADMIN);
                admin.setCreatedAt(LocalDateTime.now());
                userRepository.save(admin);
                System.out.println("✔️ Admin korisnik je uspješno kreiran.");
            }
        };
    }
}