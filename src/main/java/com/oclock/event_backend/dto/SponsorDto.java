package com.oclock.event_backend.dto;

import lombok.Builder;

@Builder
public record SponsorDto(
        Long id,
        String name,
        String description,
        String logo
) { }
