package com.oclock.event_backend.mapper;

import com.oclock.event_backend.domain.EventLocation;
import com.oclock.event_backend.dto.EventLocationDto;
import org.springframework.stereotype.Component;

@Component
public class EventLocationMapper {
    public EventLocation toEntity(EventLocationDto request) {
        return EventLocation.builder()
                .id(request.id())
                .name(request.name())
                .address(request.address())
                .city(request.city())
                .country(request.country())
                .postalCode(request.postalCode())
                .build();
    }

    public EventLocationDto toDto(EventLocation location) {
        return EventLocationDto.builder()
                .id(location.getId())
                .name(location.getName())
                .address(location.getAddress())
                .city(location.getCity())
                .country(location.getCountry())
                .postalCode(location.getPostalCode())
                .build();
    }

    public EventLocation updateEntity(EventLocation locationDb, EventLocationDto request) {
        return EventLocation.builder()
                .id(request.id())
                .name(request.name() != null ? request.name() : locationDb.getName())
                .address(request.address() != null ? request.address() : locationDb.getAddress())
                .city(request.city() != null ? request.city() : locationDb.getCity())
                .country(request.country() != null ? request.country() : locationDb.getCountry())
                .postalCode(request.postalCode() != null ? request.postalCode() : locationDb.getPostalCode())
                .build();
    }
}
