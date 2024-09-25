package com.oclock.event_backend.controller;

import com.oclock.event_backend.dto.AuthRequest;
import com.oclock.event_backend.dto.AuthResponse;
import com.oclock.event_backend.dto.RegisterRequest;
import com.oclock.event_backend.dto.RegisterResponse;
import com.oclock.event_backend.service.RefreshTokenService;
import com.oclock.event_backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("api/auth")
public class AuthController {
    private AuthService authService;
    private RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest registerRequest) {
        RegisterResponse response = authService.registerUser(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        AuthResponse response = authService.loginUser(authRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam String refreshToken) {
        refreshTokenService.deleteByToken(refreshToken);
        return ResponseEntity.ok().body("Logged out successfully");
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestParam String refreshToken) {
        AuthResponse response = refreshTokenService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(response);
    }
}
