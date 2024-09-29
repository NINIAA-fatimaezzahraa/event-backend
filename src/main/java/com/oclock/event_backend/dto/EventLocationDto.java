package com.oclock.event_backend.dto;

import lombok.Builder;

@Builder
public record EventLocationDto(
        Long id,
        String name,
        String address,
        String city,
        String country,
        String postalCode
) { }
