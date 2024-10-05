package com.oclock.event_backend.service;

import com.oclock.event_backend.dto.CreateEventDto;
import com.oclock.event_backend.dto.EventResponseDto;
import com.oclock.event_backend.dto.UpdateEventDto;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Set;

public interface EventService {
    EventResponseDto createEvent(CreateEventDto eventDto, String managerUsername);
    EventResponseDto getEventById(Long eventId);
    Set<EventResponseDto> getAllEvents();
    Set<EventResponseDto> getEventsByCategory(String eventCategory);
    Set<EventResponseDto> getEventsByLocation(Long locationId);
    Set<EventResponseDto> getEventsByManager(Long managerId);
    Set<EventResponseDto> getEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    EventResponseDto updateEventById(Long eventId, UpdateEventDto eventDto);
    void deleteManagerEventById(Long eventId, UserDetails currentUser);
    void deleteEventById(Long eventId);
}
