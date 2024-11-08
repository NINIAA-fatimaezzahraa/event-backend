package com.oclock.event_backend.dto;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record ProfileResponse(
        Long ProfileId,
        String email,
        String firstName,
        String lastName,
        boolean isActive,
        LocalDateTime createdDate
) { }
