package com.oclock.event_backend.dto;

import lombok.Builder;

@Builder
public record ProfileRequest(
        String firstName,
        String lastName,
        String email,
        String password,
        String role
) { }
