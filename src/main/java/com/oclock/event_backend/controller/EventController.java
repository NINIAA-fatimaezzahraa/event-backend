package com.oclock.event_backend.controller;

import com.oclock.event_backend.dto.CreateEventDto;
import com.oclock.event_backend.dto.EventResponseDto;
import com.oclock.event_backend.dto.UpdateEventDto;
import com.oclock.event_backend.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Set;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping("/")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ResponseEntity<EventResponseDto> createEvent(@RequestBody CreateEventDto eventDto, @AuthenticationPrincipal UserDetails userDetails) {
        EventResponseDto event = eventService.createEvent(eventDto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponseDto> getEventById(@PathVariable Long eventId) {
        EventResponseDto event = eventService.getEventById(eventId);
        return ResponseEntity.status(HttpStatus.OK).body(event);
    }

    @GetMapping("/")
    public ResponseEntity<Set<EventResponseDto>> getAllEvents() {
        Set<EventResponseDto> events = eventService.getAllEvents();
        return ResponseEntity.status(HttpStatus.OK).body(events);
    }

    @GetMapping("/category/{eventCategory}")
    public ResponseEntity<Set<EventResponseDto>> getEventsByCategory(@PathVariable String eventCategory) {
        Set<EventResponseDto> events = eventService.getEventsByCategory(eventCategory);
        return ResponseEntity.status(HttpStatus.OK).body(events);
    }

    @GetMapping("/location/{eventLocationId}")
    public ResponseEntity<Set<EventResponseDto>> getEventsByLocation(@PathVariable Long eventLocationId) {
        Set<EventResponseDto> events = eventService.getEventsByLocation(eventLocationId);
        return ResponseEntity.status(HttpStatus.OK).body(events);
    }

    @GetMapping("/manager/{eventManagerId}")
    public ResponseEntity<Set<EventResponseDto>> getEventsByManager(@PathVariable Long eventManagerId) {
        Set<EventResponseDto> events = eventService.getEventsByManager(eventManagerId);
        return ResponseEntity.status(HttpStatus.OK).body(events);
    }

    @GetMapping("/date-range")
    public ResponseEntity<Set<EventResponseDto>> getEventsByDateRange(
            @RequestParam(value = "startDate", required = false) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) LocalDateTime endDate
    ) {
        Set<EventResponseDto> events = eventService.getEventsByDateRange(startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(events);
    }

    @PatchMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ResponseEntity<EventResponseDto> updateEventById(
            @PathVariable Long eventId,
            @RequestBody UpdateEventDto eventDto
    ) {
        EventResponseDto event = eventService.updateEventById(eventId, eventDto);
        return ResponseEntity.status(HttpStatus.OK).body(event);
    }

    @DeleteMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ResponseEntity<Void> deleteManagerEventById(
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        eventService.deleteManagerEventById(eventId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/event-admin/{eventId}")
    public ResponseEntity<Void> deleteAdminEventById(@PathVariable Long eventId) {
        eventService.deleteEventById(eventId);
        return ResponseEntity.noContent().build();
    }

}
