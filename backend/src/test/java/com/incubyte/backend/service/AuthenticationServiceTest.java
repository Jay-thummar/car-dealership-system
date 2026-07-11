package com.incubyte.backend.service;

import com.incubyte.backend.dto.LoginRequest;
import com.incubyte.backend.model.User;
import com.incubyte.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        JwtService jwtService = mock(JwtService.class);
        authenticationService = new AuthenticationService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void shouldLoginSuccessfully() {
        // Arrange
        LoginRequest request = new LoginRequest("jay@gmail.com", "Password@123");
        User user = new User("1", "Jay", "jay@gmail.com", "encodedPassword");

        when(userRepository.findByEmail("jay@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@123", "encodedPassword")).thenReturn(true);

        // Act
        boolean result = authenticationService.login(request);

        // Assert
        assertTrue(result);
    }

    @Test
    void shouldRejectWrongPassword() {
        // Arrange
        LoginRequest request = new LoginRequest("jay@gmail.com", "wrongPassword");
        User user = new User("1", "Jay", "jay@gmail.com", "encodedPassword");

        when(userRepository.findByEmail("jay@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authenticationService.login(request));
    }

    @Test
    void shouldRejectUnknownEmail() {
        // Arrange
        LoginRequest request = new LoginRequest("unknown@gmail.com", "Password@123");

        when(userRepository.findByEmail("unknown@gmail.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authenticationService.login(request));
    }
}
