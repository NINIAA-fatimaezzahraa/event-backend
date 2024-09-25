package com.oclock.event_backend.dto;

import lombok.Builder;

@Builder
public record AuthResponse(
        String accessToken,
        String refreshToken
) {}
