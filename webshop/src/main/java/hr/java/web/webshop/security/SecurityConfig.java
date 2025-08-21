package hr.java.web.webshop.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final LoginSuccessHandler loginSuccessHandler;

    public SecurityConfig(LoginSuccessHandler loginSuccessHandler) {
        this.loginSuccessHandler = loginSuccessHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers("/", "/home", "/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()
                                .requestMatchers("/categories", "/products", "/products/**").permitAll()
                                .requestMatchers("/cart", "/cart/**").permitAll()
                                .requestMatchers("/checkout/paypal/**").permitAll()
                                .requestMatchers("/checkout/paypal/create-order").permitAll()
                                .requestMatchers("/checkout/paypal/capture-order").permitAll()
                                .requestMatchers("/checkout/paypal/test").permitAll()

                                .requestMatchers("/checkout", "/checkout/process", "/checkout/confirmation").authenticated()
                                .requestMatchers("/categories/create", "/categories/save", "/categories/edit/**",
                                        "/categories/update/**", "/categories/delete/**").hasRole("ADMIN")
                                .requestMatchers("/admin/**").hasRole("ADMIN")

                                .requestMatchers("/user/**").hasRole("USER")
                                .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/checkout/paypal/**")
                        .ignoringRequestMatchers("/checkout/paypal/create-order")
                        .ignoringRequestMatchers("/checkout/paypal/capture-order")
                        .ignoringRequestMatchers("/cart/add/**")
                )
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                        .contentTypeOptions(contentTypeOptions -> contentTypeOptions.disable())
                        .httpStrictTransportSecurity(hstsConfig -> hstsConfig.disable())
                        .cacheControl(cacheControl -> cacheControl.disable())
                )

                .formLogin(form ->
                        form
                                .loginPage("/login")
                                .loginProcessingUrl("/login")
                                .successHandler(loginSuccessHandler)
                                .permitAll()
                )
                .logout(logout ->
                        logout
                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                .permitAll()
                );

        return http.build();
    }
}