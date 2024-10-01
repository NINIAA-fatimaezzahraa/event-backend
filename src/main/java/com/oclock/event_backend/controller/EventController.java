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

import java.time.LocalDateTime;
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

    @GetMapping("/location/{eventLocationId}")
    public ResponseEntity<Set<EventDto>> getEventsByLocation(@PathVariable Long eventLocationId) {
        Set<EventDto> events = eventService.getEventsByLocation(eventLocationId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/manager/{eventManagerId}")
    public ResponseEntity<Set<EventDto>> getEventsByManager(@PathVariable Long eventManagerId) {
        Set<EventDto> events = eventService.getEventsByManager(eventManagerId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/date-range")
    public ResponseEntity<Set<EventDto>> getEventsByDateRange(
            @RequestParam(value = "startDate", required = false) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) LocalDateTime endDate
    ) {
        Set<EventDto> events = eventService.getEventsByDateRange(startDate, endDate);
        return ResponseEntity.ok(events);
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
