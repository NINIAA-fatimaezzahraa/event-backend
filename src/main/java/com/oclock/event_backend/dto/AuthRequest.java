package com.oclock.event_backend.dto;

import lombok.Builder;

@Builder
public record AuthRequest(
        String email,
        String password
) {}
