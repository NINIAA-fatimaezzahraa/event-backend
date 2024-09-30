package com.oclock.event_backend.controller;

import com.oclock.event_backend.dto.EventDto;
import com.oclock.event_backend.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping("/")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ResponseEntity<EventDto> createEvent(@RequestBody EventDto eventDto,  @AuthenticationPrincipal UserDetails userDetails) {
        EventDto event = eventService.createEvent(eventDto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDto> getEventById(@PathVariable Long eventId) {
        EventDto event = eventService.getEventById(eventId);
        return ResponseEntity.status(HttpStatus.OK).body(event);
    }

    @GetMapping("/")
    public ResponseEntity<Set<EventDto>> getAllEvents() {
        Set<EventDto> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/category/{eventCategory}")
    public ResponseEntity<Set<EventDto>> getEventsByCategory(@PathVariable String eventCategory) {
        Set<EventDto> events = eventService.getEventsByCategory(eventCategory);
        return ResponseEntity.ok(events);
    }

}
