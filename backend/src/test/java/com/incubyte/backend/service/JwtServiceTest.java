package com.incubyte.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // We will initialize a secret key with 256 bits (32 bytes) for HS256
        String secretKey = "mysecuresecretkeyforjwttokengenerationwhichmustbe256bitslong";
        jwtService = new JwtService(secretKey, 3600000); // 1 hour expiration
        userDetails = new User("jay@gmail.com", "password", Collections.emptyList());
    }

    @Test
    void shouldGenerateJwt() {
        // Act
        String token = jwtService.generateToken(userDetails);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void shouldExtractUsername() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertEquals("jay@gmail.com", username);
    }

    @Test
    void shouldValidateJwt() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        boolean isValid = jwtService.validateToken(token, userDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void shouldRejectExpiredJwt() {
        // Arrange
        // Create a JWT service with 0ms expiration time to guarantee expiration immediately
        String secretKey = "mysecuresecretkeyforjwttokengenerationwhichmustbe256bitslong";
        JwtService shortLivedJwtService = new JwtService(secretKey, -1000); // Negative expiration guarantees it is expired immediately
        String token = shortLivedJwtService.generateToken(userDetails);

        // Act
        boolean isValid = shortLivedJwtService.validateToken(token, userDetails);

        // Assert
        assertFalse(isValid);
    }
}
