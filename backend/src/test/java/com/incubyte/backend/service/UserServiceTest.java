package com.incubyte.backend.service;

import com.incubyte.backend.dto.RegisterRequest;
import com.incubyte.backend.model.User;
import com.incubyte.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void shouldRegisterNewUser() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "Jay",
                "jay@gmail.com",
                "Password@123"
        );

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

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.register(request));
    }

    @Test
    void shouldRejectEmptyPassword() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "Jay",
                "jay@gmail.com",
                ""
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.register(request));
    }

    @Test
    void shouldRejectWeakPassword() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "Jay",
                "jay@gmail.com",
                "Pass123" // 7 characters, minimum required is 8
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.register(request));
    }

    @Test
    void shouldRejectDuplicateEmail() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "Jay",
                "jay@gmail.com",
                "Password@123"
        );
        
        when(userRepository.existsByEmail("jay@gmail.com")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.register(request));
    }

    @Test
    void shouldEncryptPasswordBeforeSaving() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "Jay",
                "jay@gmail.com",
                "Password@123"
        );

        when(passwordEncoder.encode("Password@123")).thenReturn("encryptedPassword");

        // Act
        userService.register(request);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("encryptedPassword", userCaptor.getValue().getPassword());
    }

    @Test
    void shouldSaveUser() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "Jay",
                "jay@gmail.com",
                "Password@123"
        );

        when(passwordEncoder.encode("Password@123")).thenReturn("encryptedPassword");

        // Act
        boolean result = userService.register(request);

        // Assert
        assertTrue(result);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(java.util.List.of("ROLE_USER"), userCaptor.getValue().getRoles());
    }
}
