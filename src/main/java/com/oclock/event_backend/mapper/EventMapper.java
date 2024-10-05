package com.oclock.event_backend.mapper;

import com.oclock.event_backend.domain.Event;
import com.oclock.event_backend.domain.EventCategory;
import com.oclock.event_backend.domain.User;
import com.oclock.event_backend.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventMapper {

    private final SponsorMapper sponsorMapper;
    private final EventLocationMapper eventLocationMapper;

    public Event toEntity(CreateEventDto eventRequest, User manager) {
        return Event.builder()
                .title(eventRequest.title())
                .description(eventRequest.description())
                .createdAt(LocalDateTime.now())
                .startDate(eventRequest.startDate())
                .endDate(eventRequest.endDate())
                .price(eventRequest.price())
                .category(EventCategory.fromDisplayName(eventRequest.category()))
                .manager(manager)
                .build();
    }

    public EventResponseDto toDto(Event event) {
        Set<SponsorDto> sponsorDtos = event.getSponsors().stream()
                .map(sponsorMapper::toDto)
                .collect(Collectors.toSet());

        EventLocationDto eventLocationDto = eventLocationMapper.toDto(event.getLocation());

        String managerName = event.getManager().getFirstName() + " " + event.getManager().getLastName();

        return EventResponseDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .createdAt(event.getCreatedAt())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .manager(managerName)
                .price(event.getPrice())
                .category(event.getCategory().getDisplayName())
                .location(eventLocationDto)
                .sponsors(sponsorDtos)
                .build();
    }

    public Event updateEntity(Event eventDb, UpdateEventDto eventRequest) {
        return Event.builder()
                .id(eventRequest.id())
                .title(eventRequest.title() != null ? eventRequest.title() : eventDb.getTitle())
                .description(eventRequest.description() != null ? eventRequest.description() : eventDb.getDescription())
                .createdAt(eventDb.getCreatedAt())
                .startDate(eventRequest.startDate() != null ? eventRequest.startDate() : eventDb.getStartDate())
                .endDate(eventRequest.endDate() != null ? eventRequest.endDate() : eventDb.getEndDate())
                .manager(eventDb.getManager())
                .price(eventRequest.price() != null ? eventRequest.price() : eventDb.getPrice())
                .category(eventRequest.category() != null ? EventCategory.fromDisplayName(eventRequest.category()) : eventDb.getCategory())
                .location(eventDb.getLocation())
                .sponsors(new HashSet<>(eventDb.getSponsors()))
                .build();
    }
}
