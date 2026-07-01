package com.mitocode;

import com.mitocode.iam.persistence.entities.UserEntity;
import com.mitocode.iam.persistence.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import com.mitocode.iam.persistence.entities.Role;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }

    @Bean
    @Order(0)
    CommandLineRunner init(UserRepository userRepo, PasswordEncoder encoder,
                           @Value("${app.bootstrap.admin-email:}") String email,
                           @Value("${app.bootstrap.admin-password:}") String password,
                           @Value("${app.bootstrap.admin-name:Administrador}") String name) {
        return args -> {
            if (!email.isBlank() && !password.isBlank() && userRepo.findByEmail(email.toLowerCase()).isEmpty()) {
                UserEntity u = new UserEntity();
                u.setEmail(email.trim().toLowerCase());
                u.setPassword(encoder.encode(password));
                u.setName(name.trim());
                u.setRole(Role.ADMIN);
                userRepo.save(u);
            }
        };
    }
}
