package com.oclock.event_backend.service;

import com.oclock.event_backend.domain.RefreshToken;
import com.oclock.event_backend.domain.User;
import com.oclock.event_backend.dto.AuthResponse;
import com.oclock.event_backend.exception.EmptyInputException;
import com.oclock.event_backend.exception.FunctionalException;
import com.oclock.event_backend.repository.RefreshTokenRepository;
import com.oclock.event_backend.util.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private RefreshTokenRepository refreshTokenRepository;
    private JwtUtil jwtUtil;

    @Override
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(Instant.now().plusMillis(7 * 24 * 60 * 60 * 1000)) // Set expiry to 7 days
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public AuthResponse refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new EmptyInputException("Refresh token is required.");
        }

        Optional<RefreshToken> optionalToken = findByToken(refreshToken);

        if (optionalToken.get().getExpiryDate().isBefore(Instant.now())) {
            throw new FunctionalException("Expired refresh token.");
        }

        String newAccessToken = jwtUtil.generateAccessToken(optionalToken.get().getUser());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    public void deleteByToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new EmptyInputException("Refresh token is required.");
        }

        Optional<RefreshToken> optionalToken = refreshTokenRepository.findByToken(refreshToken);

        if (optionalToken.isEmpty()) {
            throw new FunctionalException("Invalid refresh token.");
        }

        refreshTokenRepository.deleteByToken(refreshToken);
    }
}
