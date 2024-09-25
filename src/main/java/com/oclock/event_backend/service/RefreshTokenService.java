package com.oclock.event_backend.service;

import com.oclock.event_backend.domain.RefreshToken;
import com.oclock.event_backend.domain.User;
import com.oclock.event_backend.dto.AuthResponse;

import java.util.Optional;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);
    AuthResponse refreshAccessToken(String refreshToken);
    Optional<RefreshToken> findByToken(String token);
    void deleteByToken(String token);
}
