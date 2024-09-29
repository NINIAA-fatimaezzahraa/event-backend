package com.oclock.event_backend.mapper;

import com.oclock.event_backend.domain.Event;
import com.oclock.event_backend.domain.EventCategory;
import com.oclock.event_backend.domain.User;
import com.oclock.event_backend.dto.EventDto;
import com.oclock.event_backend.dto.EventLocationDto;
import com.oclock.event_backend.dto.SponsorDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventMapper {

    private final SponsorMapper sponsorMapper;
    private final EventLocationMapper eventLocationMapper;

    public Event toEntity(EventDto eventRequest, User manager) {
        return Event.builder()
                .title(eventRequest.title())
                .description(eventRequest.description())
                .createdAt(LocalDateTime.now())
                .startDate(eventRequest.startDate())
                .endDate(eventRequest.endDate())
                .price(eventRequest.price())
                .category(EventCategory.fromDisplayName(eventRequest.category()))
                .manager(manager)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public EventDto toDto(Event event) {
        Set<SponsorDto> sponsorDtos = event.getSponsors().stream()
                .map(sponsorMapper::toDto)
                .collect(Collectors.toSet());

        EventLocationDto eventLocationDto = eventLocationMapper.toDto(event.getLocation());

        return EventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .createdAt(event.getCreatedAt())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .manager(event.getManager().getFirstName() + " " + event.getManager().getLastName())
                .price(event.getPrice())
                .category(event.getCategory().getDisplayName())
                .location(eventLocationDto)
                .sponsors(sponsorDtos)
                .build();
    }
}
