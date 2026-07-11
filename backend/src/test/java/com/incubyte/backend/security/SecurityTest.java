package com.incubyte.backend.security;

import com.incubyte.backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldReturn401WhenTokenMissing() throws Exception {
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAuthenticatedRequest() throws Exception {
        UserDetails userDetails = new User("user@gmail.com", "password", 
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        String token = jwtService.generateToken(userDetails);

        mockMvc.perform(get("/api/vehicles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn403ForNormalUserOnDelete() throws Exception {
        UserDetails userDetails = new User("user@gmail.com", "password", 
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        String token = jwtService.generateToken(userDetails);

        mockMvc.perform(delete("/api/vehicles/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToDeleteVehicle() throws Exception {
        UserDetails userDetails = new User("admin@gmail.com", "password", 
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        String token = jwtService.generateToken(userDetails);

        mockMvc.perform(delete("/api/vehicles/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn403ForNormalUserOnRestock() throws Exception {
        UserDetails userDetails = new User("user@gmail.com", "password", 
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        String token = jwtService.generateToken(userDetails);

        mockMvc.perform(post("/api/vehicles/1/restock")
                        .header("Authorization", "Bearer " + token)
                        .param("quantity", "5"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToRestock() throws Exception {
        UserDetails userDetails = new User("admin@gmail.com", "password", 
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        String token = jwtService.generateToken(userDetails);

        mockMvc.perform(post("/api/vehicles/1/restock")
                        .header("Authorization", "Bearer " + token)
                        .param("quantity", "5"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldAllowCorsPreflightRequest() throws Exception {
        mockMvc.perform(options("/api/vehicles")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }
}
