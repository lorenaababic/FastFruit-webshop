package hr.java.web.webshop.service;


import hr.java.web.webshop.dto.UserRegistrationDto;
import hr.java.web.webshop.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> findAll();
    User registerNewUser(User user);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean emailExists(String email);
    boolean usernameExists(String username);
    User save(User user);
}
