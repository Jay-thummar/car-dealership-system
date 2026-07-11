package com.incubyte.backend.config;

import com.incubyte.backend.model.User;
import com.incubyte.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    void shouldCreateAdminUserIfNotExists() throws Exception {
        // Arrange
        when(userRepository.findByEmail("admin@dealership.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("AdminPassword@123")).thenReturn("encryptedAdminPassword");

        // Act
        dataInitializer.run();

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User admin = userCaptor.getValue();
        assertEquals("admin@dealership.com", admin.getEmail());
        assertEquals("encryptedAdminPassword", admin.getPassword());
        assertTrue(admin.getRoles().contains("ROLE_ADMIN"));
        assertTrue(admin.getRoles().contains("ROLE_USER"));
    }

    @Test
    void shouldNotCreateAdminUserIfExists() throws Exception {
        // Arrange
        when(userRepository.findByEmail("admin@dealership.com")).thenReturn(Optional.of(new User()));

        // Act
        dataInitializer.run();

        // Assert
        verify(userRepository, never()).save(any(User.class));
    }
}
