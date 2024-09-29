package com.oclock.event_backend.service;

import com.oclock.event_backend.dto.EventDto;

public interface EventService {
    EventDto createEvent(EventDto eventDto, String managerUsername);
    EventDto getEventById(Long eventId);
}
