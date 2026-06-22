package com.mitocode;

import com.mitocode.iam.persistence.entities.UserEntity;
import com.mitocode.iam.persistence.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }

    @Bean
    CommandLineRunner init(UserRepository userRepo, PasswordEncoder encoder) {
        return args -> {
            if (userRepo.findByEmail("gabriela@cargo.pe").isEmpty()) {
                UserEntity u = new UserEntity();
                u.setEmail("gabriela@cargo.pe");
                u.setPassword(encoder.encode("123456"));
                u.setName("Gabriela");
                u.setRole("Asesora financiera");
                userRepo.save(u);
            }
        };
    }
}
