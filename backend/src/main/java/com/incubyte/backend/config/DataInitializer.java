package com.incubyte.backend.config;

import com.incubyte.backend.model.User;
import com.incubyte.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("admin@dealership.com").isEmpty()) {
            String encryptedPassword = passwordEncoder.encode("AdminPassword@123");
            User admin = new User(
                    null,
                    "System Admin",
                    "admin@dealership.com",
                    encryptedPassword,
                    List.of("ROLE_ADMIN", "ROLE_USER")
            );
            userRepository.save(admin);
        }
    }
}
