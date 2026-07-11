package com.incubyte.backend.service;

import com.incubyte.backend.dto.RegisterRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    @Test
    void shouldRegisterNewUser() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "Jay",
                "jay@gmail.com",
                "Password@123"
        );

        UserService userService = new UserService();

        // Act
        boolean result = userService.register(request);

        // Assert
        assertTrue(result);
    }

    @Test
    void shouldRejectEmptyEmail() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "Jay",
                "",
                "Password@123"
        );

        UserService userService = new UserService();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.register(request));
    }

    @Test
    void shouldRejectInvalidEmail() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "Jay",
                "invalid-email",
                "Password@123"
        );

        UserService userService = new UserService();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.register(request));
    }
}
