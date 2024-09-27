package com.oclock.event_backend.service;

import com.oclock.event_backend.dto.AuthRequest;
import com.oclock.event_backend.dto.AuthResponse;
import com.oclock.event_backend.dto.ProfileRequest;
import com.oclock.event_backend.dto.ProfileResponse;

public interface AuthService {
    ProfileResponse registerUser(ProfileRequest registerRequest);
    AuthResponse loginUser(AuthRequest authRequest);
}
