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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

    @Test
    @WithMockUser(roles = "USER")
    void shouldSearchVehiclesSuccessfully() throws Exception {
        // Arrange
        Vehicle vehicle = new Vehicle("1", "Toyota", "Camry", "Sedan", 30000.00, 5);
        when(vehicleService.searchVehicles("Toyota", null, null, null, null)).thenReturn(List.of(vehicle));

        // Act & Assert
        mockMvc.perform(get("/api/vehicles/search")
                        .param("make", "Toyota"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].make").value("Toyota"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldUpdateVehicleSuccessfully() throws Exception {
        // Arrange
        Vehicle updatedVehicle = new Vehicle("1", "Toyota", "Camry", "Sedan", 32000.00, 4);
        when(vehicleService.updateVehicle(eq("1"), any(Vehicle.class))).thenReturn(updatedVehicle);

        // Act & Assert
        mockMvc.perform(put("/api/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedVehicle)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(32000.00))
                .andExpect(jsonPath("$.quantity").value(4));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetVehicleByIdSuccessfully() throws Exception {
        // Arrange
        Vehicle vehicle = new Vehicle("1", "Toyota", "Camry", "Sedan", 30000.00, 5);
        when(vehicleService.getVehicleById("1")).thenReturn(vehicle);

        // Act & Assert
        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.make").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Camry"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldPurchaseVehicleSuccessfully() throws Exception {
        // Arrange
        Vehicle updatedVehicle = new Vehicle("1", "Toyota", "Camry", "Sedan", 30000.00, 3);
        when(vehicleService.purchaseVehicle("1", 2)).thenReturn(updatedVehicle);

        // Act & Assert
        mockMvc.perform(post("/api/vehicles/1/purchase")
                        .param("quantity", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(3));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldRejectPurchaseIfStockInsufficient() throws Exception {
        // Arrange
        when(vehicleService.purchaseVehicle("1", 6)).thenThrow(new IllegalArgumentException("Insufficient stock"));

        // Act & Assert
        mockMvc.perform(post("/api/vehicles/1/purchase")
                        .param("quantity", "6"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient stock"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldRestockVehicleSuccessfully() throws Exception {
        // Arrange
        Vehicle updatedVehicle = new Vehicle("1", "Toyota", "Camry", "Sedan", 30000.00, 10);
        when(vehicleService.restockVehicle("1", 5)).thenReturn(updatedVehicle);

        // Act & Assert
        mockMvc.perform(post("/api/vehicles/1/restock")
                        .param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(10));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteVehicleSuccessfully() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/vehicles/1"))
                .andExpect(status().isNoContent());

        verify(vehicleService).deleteVehicle("1");
    }
}
