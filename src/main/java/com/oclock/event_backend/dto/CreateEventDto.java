package com.oclock.event_backend.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Builder
public record CreateEventDto(
        String title,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        EventLocationDto location,
        BigDecimal price,
        String category,
        Set<SponsorDto> sponsors
) { }
