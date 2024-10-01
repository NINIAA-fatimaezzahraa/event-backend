package com.oclock.event_backend.service;

import com.oclock.event_backend.dto.EventDto;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Set;

public interface EventService {
    EventDto createEvent(EventDto eventDto, String managerUsername);
    EventDto getEventById(Long eventId);
    Set<EventDto> getAllEvents();
    Set<EventDto> getEventsByCategory(String eventCategory);
    Set<EventDto> getEventsByLocation(Long locationId);
    Set<EventDto> getEventsByManager(Long managerId);
    Set<EventDto> getEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    void deleteManagerEventById(Long eventId, UserDetails currentUser);
}
