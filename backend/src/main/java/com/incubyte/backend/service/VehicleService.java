package com.incubyte.backend.service;

import com.incubyte.backend.model.Vehicle;
import com.incubyte.backend.repository.VehicleRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final MongoTemplate mongoTemplate;

    public VehicleService(VehicleRepository vehicleRepository, MongoTemplate mongoTemplate) {
        this.vehicleRepository = vehicleRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public Vehicle addVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public List<Vehicle> searchVehicles(String make, String model, String category, Double minPrice, Double maxPrice) {
        Query query = new Query();

        if (make != null && !make.isBlank()) {
            query.addCriteria(Criteria.where("make").regex(make, "i"));
        }
        if (model != null && !model.isBlank()) {
            query.addCriteria(Criteria.where("model").regex(model, "i"));
        }
        if (category != null && !category.isBlank()) {
            query.addCriteria(Criteria.where("category").regex(category, "i"));
        }

        if (minPrice != null && maxPrice != null) {
            query.addCriteria(Criteria.where("price").gte(minPrice).lte(maxPrice));
        } else if (minPrice != null) {
            query.addCriteria(Criteria.where("price").gte(minPrice));
        } else if (maxPrice != null) {
            query.addCriteria(Criteria.where("price").lte(maxPrice));
        }

        return mongoTemplate.find(query, Vehicle.class);
    }

    public Vehicle updateVehicle(String id, Vehicle vehicle) {
        Vehicle existing = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        existing.setMake(vehicle.getMake());
        existing.setModel(vehicle.getModel());
        existing.setCategory(vehicle.getCategory());
        existing.setPrice(vehicle.getPrice());
        existing.setQuantity(vehicle.getQuantity());

        return vehicleRepository.save(existing);
    }

    public Vehicle getVehicleById(String id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
    }

    public void deleteVehicle(String id) {
        if (!vehicleRepository.existsById(id)) {
            throw new IllegalArgumentException("Vehicle not found");
        }
        vehicleRepository.deleteById(id);
    }
}
