package com.oclock.event_backend;

import com.oclock.event_backend.domain.RefreshToken;
import com.oclock.event_backend.domain.User;
import com.oclock.event_backend.dto.AuthResponse;
import com.oclock.event_backend.exception.EmptyInputException;
import com.oclock.event_backend.exception.FunctionalException;
import com.oclock.event_backend.repository.RefreshTokenRepository;
import com.oclock.event_backend.service.RefreshTokenServiceImpl;
import com.oclock.event_backend.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RefreshTokenServiceTest {

    private AutoCloseable openMocks;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    void testCreateRefreshToken_Success() {
        User user = new User();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(Instant.now().plusMillis(7 * 24 * 60 * 60 * 1000))
                .build();

        when(refreshTokenRepository.save(any())).thenReturn(refreshToken);

        RefreshToken createdToken = refreshTokenService.createRefreshToken(user);

        assertNotNull(createdToken);
        assertEquals(refreshToken.getToken(), createdToken.getToken());
        assertEquals(refreshToken.getExpiryDate(), createdToken.getExpiryDate());

        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void testRefreshAccessToken_Success() {
        String generatedRefreshToken = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(generatedRefreshToken)
                .expiryDate(Instant.now().plusMillis(7 * 24 * 60 * 60 * 1000))
                .user(new User())
                .build();

        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(refreshToken));
        when(jwtUtil.generateAccessToken(any())).thenReturn("newAccessToken");

        AuthResponse response = refreshTokenService.refreshAccessToken(refreshToken.getToken());

        assertNotNull(response);
        assertEquals("newAccessToken", response.accessToken());
        assertEquals(generatedRefreshToken, response.refreshToken());
    }

    @Test
    void testRefreshAccessToken_ExpiredToken() {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().minusMillis(7 * 24 * 60 * 60 * 1000))
                .user(new User())
                .build();

        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(refreshToken));

        FunctionalException thrown = assertThrows(
                FunctionalException.class,
                () -> refreshTokenService.refreshAccessToken(refreshToken.getToken())
        );

        assertEquals("Expired refresh token.", thrown.getMessage());
    }

    @Test
    void testRefreshAccessToken_InvalidToken() {
        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.empty());

        FunctionalException thrown = assertThrows(
                FunctionalException.class,
                () -> refreshTokenService.refreshAccessToken("invalidToken")
        );

        assertEquals("Invalid refresh token.", thrown.getMessage());
    }


    @Test
    void testDeleteByToken_Success() {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(7 * 24 * 60 * 60 * 1000))
                .build();

        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(refreshToken));

        refreshTokenService.deleteByToken(refreshToken.getToken());

        verify(refreshTokenRepository, times(1)).deleteByToken(refreshToken.getToken());
    }

    @Test
    void testDeleteByToken_InvalidToken() {
        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.empty());

        FunctionalException thrown = assertThrows(
                FunctionalException.class,
                () -> refreshTokenService.deleteByToken("invalidToken")
        );

        assertEquals("Invalid refresh token.", thrown.getMessage());
    }

    @Test
    void testRefreshAccessToken_MissingToken() {
        EmptyInputException thrown = assertThrows(
                EmptyInputException.class,
                () -> refreshTokenService.refreshAccessToken(null)
        );

        assertEquals("Refresh token is required.", thrown.getMessage());
    }
}
