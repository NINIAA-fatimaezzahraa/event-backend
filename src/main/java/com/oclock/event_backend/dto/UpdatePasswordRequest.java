package com.oclock.event_backend.dto;

import lombok.Builder;

@Builder
public record UpdatePasswordRequest(
        String currentPassword,
        String newPassword
) { }
