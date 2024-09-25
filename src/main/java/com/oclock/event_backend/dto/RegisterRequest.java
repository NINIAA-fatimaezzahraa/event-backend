package com.oclock.event_backend.dto;

import lombok.Builder;

@Builder
public record RegisterRequest(
        String firstName,
        String lastName,
        String email,
        String password,
        String role
) { }
