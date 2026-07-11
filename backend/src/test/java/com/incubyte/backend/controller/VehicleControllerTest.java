package com.incubyte.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incubyte.backend.model.Vehicle;
import com.incubyte.backend.service.VehicleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VehicleService vehicleService;

    @Test
    @WithMockUser(roles = "USER")
    void shouldAddVehicleSuccessfully() throws Exception {
        // Arrange
        Vehicle vehicle = new Vehicle("1", "Toyota", "Camry", "Sedan", 30000.00, 5);
        when(vehicleService.addVehicle(any(Vehicle.class))).thenReturn(vehicle);

        // Act & Assert
        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicle)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.make").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Camry"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetAllVehiclesSuccessfully() throws Exception {
        // Arrange
        Vehicle vehicle1 = new Vehicle("1", "Toyota", "Camry", "Sedan", 30000.00, 5);
        Vehicle vehicle2 = new Vehicle("2", "Honda", "Civic", "Sedan", 25000.00, 3);
        when(vehicleService.getAllVehicles()).thenReturn(List.of(vehicle1, vehicle2));

        // Act & Assert
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[1].id").value("2"));
    }
}
