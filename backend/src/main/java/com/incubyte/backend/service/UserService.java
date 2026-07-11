package com.incubyte.backend.service;

import com.incubyte.backend.dto.RegisterRequest;

public class UserService {

    public boolean register(RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is mandatory");
        }
        return true;
    }
}
