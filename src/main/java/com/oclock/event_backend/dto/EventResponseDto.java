package com.oclock.event_backend.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Builder
public record EventResponseDto(
        Long id,
        String title,
        String description,
        LocalDateTime createdAt,
        LocalDateTime startDate,
        LocalDateTime endDate,
        EventLocationDto location,
        BigDecimal price,
        String category,
        String manager,
        Set<SponsorDto> sponsors
) { }
