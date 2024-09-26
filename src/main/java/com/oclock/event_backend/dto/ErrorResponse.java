package com.oclock.event_backend.dto;

import lombok.Builder;

@Builder
public record ErrorResponse(
        String errorCode,
        String message
) { }
