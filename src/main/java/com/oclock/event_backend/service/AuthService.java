package com.oclock.event_backend.service;

import com.oclock.event_backend.dto.AuthRequest;
import com.oclock.event_backend.dto.AuthResponse;
import com.oclock.event_backend.dto.RegisterRequest;
import com.oclock.event_backend.dto.RegisterResponse;

public interface AuthService {
    RegisterResponse registerUser(RegisterRequest RregisterRequest);
    AuthResponse loginUser(AuthRequest authRequest);
}
