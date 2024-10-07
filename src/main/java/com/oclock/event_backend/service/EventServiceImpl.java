package com.oclock.event_backend.service;

import com.oclock.event_backend.domain.*;
import com.oclock.event_backend.dto.*;
import com.oclock.event_backend.exception.CustomDatabaseException;
import com.oclock.event_backend.exception.FunctionalException;
import com.oclock.event_backend.exception.ResourceNotFoundException;
import com.oclock.event_backend.mapper.EventLocationMapper;
import com.oclock.event_backend.mapper.EventMapper;
import com.oclock.event_backend.mapper.SponsorMapper;
import com.oclock.event_backend.repository.EventLocationRepository;
import com.oclock.event_backend.repository.EventRepository;
import com.oclock.event_backend.repository.SponsorRepository;
import com.oclock.event_backend.repository.UserRepository;
import com.oclock.event_backend.util.APIsErrorCodesConstants;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class EventServiceImpl implements EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventLocationRepository eventLocationRepository;
    private final SponsorRepository sponsorRepository;

    private final EventMapper eventMapper;
    private final EventLocationMapper eventLocationMapper;
    private final SponsorMapper sponsorMapper;

    // TODO: Handling EventLocation and Sponsor exists and duplicate items
    @Override
    public EventResponseDto createEvent(CreateEventDto request, String managerUsername) {
        User manager = this.getUserByUsername(managerUsername);

        Event event = eventMapper.toEntity(request, manager);

        EventLocation eventLocation = Optional.ofNullable(request.location().id())
                .flatMap(eventLocationRepository::findById)
                .orElseGet(() -> eventLocationRepository.save(eventLocationMapper.toEntity(request.location())));

        Set<Sponsor> sponsors = request.sponsors()
                .stream()
                .map(sponsorDto -> {
                    if (sponsorDto.id() != null) {
                        return sponsorRepository.findById(sponsorDto.id())
                                .orElseGet(() -> sponsorRepository.save(sponsorMapper.toEntity(sponsorDto)));
                    } else {
                        return sponsorRepository.save(sponsorMapper.toEntity(sponsorDto));
                    }
                })
                .collect(Collectors.toSet());

        event.setLocation(eventLocation);
        event.setSponsors(sponsors);

        try {
            Event savedEvent = eventRepository.save(event);
            return eventMapper.toDto(savedEvent);
        } catch (DataIntegrityViolationException e) {
            throw new CustomDatabaseException(APIsErrorCodesConstants.DATABASE_CONSTRAINT_VIOLATION);
        }
    }

    @Override
    public EventResponseDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(APIsErrorCodesConstants.EVENT_NOT_FOUND, eventId))
                );

        return eventMapper.toDto(event);
    }

    @Override
    public Set<EventResponseDto> getAllEvents() {
        List<Event> event = eventRepository.findAll();

        return event.stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<EventResponseDto> getEventsByCategory(String eventCategory) {
        EventCategory category = EventCategory.fromDisplayName(eventCategory);

        if(category == null) {
            throw new FunctionalException(String.format(APIsErrorCodesConstants.CATEGORY_NOT_FOUND, eventCategory));
        }

        Set<Event> event = eventRepository.findByCategory(category);

        return event.stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<EventResponseDto> getEventsByLocation(Long locationId) {
        Optional<EventLocation> location = eventLocationRepository.findById(locationId);
        Set<Event> event = new HashSet<>();

        if(location.isPresent()) {
            event = eventRepository.findByLocation(location.get());
        }

        return event.stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<EventResponseDto> getEventsByManager(Long managerId) {
        Optional<User> manager = userRepository.findById(managerId);
        Set<Event> event = new HashSet<>();

        if(manager.isPresent()) {
            event = eventRepository.findByManager(manager.get());
        }

        return event.stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<EventResponseDto> getEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        validateDateRange(startDate, endDate);
        Set<Event> events = eventRepository.findByStartDateBetween(startDate, endDate);
        return events.stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toSet());
    }

    @Override
    public EventResponseDto updateEventById(Long eventId, UpdateEventDto eventDto) {
        Event eventDb = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(
                                String.format(APIsErrorCodesConstants.EVENT_NOT_FOUND, eventId)
                ));

        Event event = eventMapper.updateEntity(eventDb, eventDto);
        eventRepository.save(event);

        return eventMapper.toDto(event);
    }

    @Override
    public EventResponseDto updateSponsors(Long eventId, Set<SponsorDto> sponsors) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(
                                String.format(APIsErrorCodesConstants.EVENT_NOT_FOUND, eventId)
                ));

        for (SponsorDto sponsorDto : sponsors) {
            Sponsor sponsor = sponsorRepository.findById(sponsorDto.id())
                    .orElseThrow(() -> new FunctionalException(
                                    String.format(APIsErrorCodesConstants.SPONSOR_NOT_FOUND, sponsorDto.id())
                    ));

            if (!event.getSponsors().contains(sponsor)) {
                throw new FunctionalException(
                        String.format(APIsErrorCodesConstants.SPONSOR_NOT_ASSOCIATED, sponsorDto.id())
                );
            }
        }

        Set<Sponsor> updatedSponsors = sponsors.stream()
                .map(sponsorMapper::toEntity)
                .collect(Collectors.toSet());

        event.setSponsors(updatedSponsors);
        Set<Sponsor> savedSponsors = new HashSet<>(sponsorRepository.saveAll(updatedSponsors));
        event.setSponsors(savedSponsors);

        return eventMapper.toDto(event);
    }

    @Override
    public EventResponseDto updateEventLocation(Long eventId, EventLocationDto locationDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(APIsErrorCodesConstants.EVENT_NOT_FOUND, eventId)
                ));

        EventLocation updatedLocation = eventLocationRepository.findById(locationDto.id())
                .map(existingLocation -> eventLocationMapper.updateEntity(existingLocation, locationDto))
                .orElseGet(() -> eventLocationMapper.toEntity(locationDto));

        EventLocation savedLocation = eventLocationRepository.save(updatedLocation);
        event.setLocation(savedLocation);

        return eventMapper.toDto(event);
    }

    @Override
    public EventResponseDto addSponsorsToEvent(Long eventId, Set<SponsorDto> sponsors) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(APIsErrorCodesConstants.EVENT_NOT_FOUND, eventId)
                ));

        Set<Sponsor> existingSponsors = event.getSponsors();
        Set<Sponsor> newSponsors = sponsors.stream()
                .map(sponsorDto -> {
                    if (sponsorDto.id() != null) {
                        return sponsorRepository.findById(sponsorDto.id())
                                .orElseGet(() -> sponsorRepository.save(sponsorMapper.toEntity(sponsorDto)));
                    } else {
                        return sponsorRepository.save(sponsorMapper.toEntity(sponsorDto));
                    }
                })
                .collect(Collectors.toSet());


        existingSponsors.addAll(newSponsors);
        event.setSponsors(existingSponsors);

        Event updatedEvent = eventRepository.save(event);

        return eventMapper.toDto(updatedEvent);
    }

    @Override
    public EventResponseDto removeSponsorsFromEvent(Long eventId, Set<Long> sponsorIds) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(APIsErrorCodesConstants.EVENT_NOT_FOUND, eventId)
                ));

        Set<Sponsor> sponsorsToRemove = sponsorIds.stream()
                .map(sponsorId -> sponsorRepository.findById(sponsorId)
                        .orElseThrow(() -> new FunctionalException(
                                String.format(APIsErrorCodesConstants.SPONSOR_NOT_FOUND, sponsorId)
                        ))
                ).collect(Collectors.toSet());

        event.getSponsors().removeAll(sponsorsToRemove);

        Event updatedEvent = eventRepository.save(event);

        return eventMapper.toDto(updatedEvent);
    }

    @Override
    public void deleteManagerEventById(Long eventId, UserDetails currentUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(APIsErrorCodesConstants.EVENT_NOT_FOUND, eventId)
                ));

        if (!event.getManager().getEmail().equals(currentUser.getUsername())) {
            throw new BadCredentialsException(APIsErrorCodesConstants.NOT_AUTHORIZED_TO_DELETE_EVENT);
        }

        eventRepository.deleteById(eventId);
    }

    @Override
    public void deleteEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(APIsErrorCodesConstants.EVENT_NOT_FOUND, eventId)
                ));

        eventRepository.delete(event);
    }

    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new FunctionalException(APIsErrorCodesConstants.MANDATORY_DATE_PROVIDE);
        }

        if (startDate.isAfter(endDate)) {
            throw new FunctionalException(APIsErrorCodesConstants.START_DATE_AFTER_END_DATE);
        }
    }

    public User getUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(APIsErrorCodesConstants.USER_NOT_FOUND, email)
                ));
    }
}
