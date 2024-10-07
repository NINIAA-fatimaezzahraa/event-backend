package com.oclock.event_backend;

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
import com.oclock.event_backend.service.EventServiceImpl;
import com.oclock.event_backend.util.APIsErrorCodesConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EventServiceTest {

    private AutoCloseable openMocks;

    @InjectMocks
    private EventServiceImpl eventService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventLocationRepository eventLocationRepository;

    @Mock
    private SponsorRepository sponsorRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private EventLocationMapper eventLocationMapper;

    @Mock
    private SponsorMapper sponsorMapper;

    private User mockManager;
    private CreateEventDto mockCreateEventDto;
    private EventResponseDto mockEventResponse;
    private Event mockEvent;
    private EventLocationDto mockEventLocationDto;
    private EventLocation mockEventLocation;
    private SponsorDto mockSponsorDto;
    private Sponsor mockSponsor;
    private String managerEmail;
    private EventLocation updatedLocation;
    private EventLocation savedLocation;

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);

        managerEmail = "manager@gmail.com";

        mockManager =User.builder()
                .email(managerEmail)
                .build();

        mockEventLocationDto = EventLocationDto.builder()
                .id(1L)
                .name("Palais des Congrès de Paris")
                .address("2 Place de la Porte Maillot")
                .city("Paris")
                .country("France")
                .postalCode("75017")
                .build();

        mockEventLocation = EventLocation.builder()
                .id(1L)
                .name("Palais des Congrès de Paris")
                .address("2 Place de la Porte Maillot")
                .city("Paris")
                .country("France")
                .postalCode("75017")
                .build();

        mockSponsorDto = SponsorDto.builder()
                .id(1L)
                .name("Tech Corp")
                .description("Leading provider of tech solutions.")
                .logo("https://eventpic.com/logo.png")
                .build();

        mockSponsor = Sponsor.builder()
                .id(1L)
                .name("Tech Corp")
                .description("Leading provider of tech solutions.")
                .logo("https://eventpic.com/logo.png")
                .build();

        mockCreateEventDto = CreateEventDto.builder()
                .title("Tech Expo 2024")
                .description("Annual tech conference.")
                .startDate(LocalDateTime.of(2024, 10, 1, 9, 0))
                .endDate(LocalDateTime.of(2024, 10, 3, 17, 0))
                .location(mockEventLocationDto)
                .price(new BigDecimal("199.99"))
                .category("Technology")
                .sponsors(Set.of(mockSponsorDto))
                .build();

        mockEventResponse = EventResponseDto.builder()
                .title("Tech Expo 2024")
                .description("Annual tech conference.")
                .createdAt(LocalDateTime.now())
                .startDate(LocalDateTime.of(2024, 10, 1, 9, 0))
                .endDate(LocalDateTime.of(2024, 10, 3, 17, 0))
                .location(mockEventLocationDto)
                .price(new BigDecimal("199.99"))
                .category("Technology")
                .manager(mockManager.getFirstName() + " " + mockManager.getLastName())
                .sponsors(Set.of(mockSponsorDto))
                .build();

        mockEvent = Event.builder()
                .id(1L)
                .title("Tech Expo 2024")
                .description("Annual tech conference.")
                .createdAt(LocalDateTime.now())
                .startDate(LocalDateTime.of(2024, 10, 1, 9, 0))
                .endDate(LocalDateTime.of(2024, 10, 3, 17, 0))
                .location(mockEventLocation)
                .price(new BigDecimal("199.99"))
                .category(EventCategory.TECHNOLOGY)
                .manager(mockManager)
                .sponsors(Set.of(mockSponsor))
                .build();

        updatedLocation = new EventLocation();
        savedLocation = new EventLocation();
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    void testCreateEvent_SuccessfulCreation() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockManager));
        when(eventMapper.toEntity(any(CreateEventDto.class), any(User.class))).thenReturn(mockEvent);
        when(eventLocationRepository.findById(mockEventLocationDto.id())).thenReturn(Optional.of(mockEventLocation));
        when(sponsorRepository.findById(mockSponsorDto.id())).thenReturn(Optional.of(mockSponsor));
        when(eventLocationMapper.toEntity(any(EventLocationDto.class))).thenReturn(mockEventLocation);
        when(sponsorMapper.toEntity(any(SponsorDto.class))).thenReturn(mockSponsor);
        when(eventRepository.save(any(Event.class))).thenReturn(mockEvent);
        when(eventMapper.toDto(any(Event.class))).thenReturn(mockEventResponse);

        EventResponseDto result = eventService.createEvent(mockCreateEventDto, managerEmail);

        assertNotNull(result);
        assertEquals("Tech Expo 2024", result.title());
        assertEquals("Technology", result.category());
        assertEquals(mockEventLocationDto, result.location());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testCreateEvent_LocationNotFound_CreatesNewLocation() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockManager));
        when(eventMapper.toEntity(any(CreateEventDto.class), any(User.class))).thenReturn(mockEvent);
        when(eventLocationRepository.findById(mockEventLocationDto.id())).thenReturn(Optional.empty());
        when(eventLocationRepository.save(any(EventLocation.class))).thenReturn(mockEventLocation);
        when(sponsorRepository.findById(mockSponsorDto.id())).thenReturn(Optional.of(mockSponsor));
        when(eventLocationMapper.toEntity(any(EventLocationDto.class))).thenReturn(mockEventLocation);
        when(sponsorMapper.toEntity(any(SponsorDto.class))).thenReturn(mockSponsor);
        when(eventRepository.save(any(Event.class))).thenReturn(mockEvent);
        when(eventMapper.toDto(any(Event.class))).thenReturn(mockEventResponse);

        EventResponseDto result = eventService.createEvent(mockCreateEventDto, managerEmail);

        assertNotNull(result);
        assertEquals("Tech Expo 2024", result.title());
        assertEquals("Technology", result.category());
        verify(eventLocationRepository, times(1)).save(any(EventLocation.class));
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testCreateEvent_SponsorNotFound_CreatesNewSponsor() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockManager));
        when(eventMapper.toEntity(any(CreateEventDto.class), any(User.class))).thenReturn(mockEvent);
        when(eventLocationRepository.findById(mockEventLocationDto.id())).thenReturn(Optional.of(mockEventLocation));
        when(sponsorRepository.findById(mockSponsorDto.id())).thenReturn(Optional.empty());
        when(sponsorRepository.save(any(Sponsor.class))).thenReturn(mockSponsor);
        when(eventLocationMapper.toEntity(any(EventLocationDto.class))).thenReturn(mockEventLocation);
        when(sponsorMapper.toEntity(any(SponsorDto.class))).thenReturn(mockSponsor);
        when(eventRepository.save(any(Event.class))).thenReturn(mockEvent);
        when(eventMapper.toDto(any(Event.class))).thenReturn(mockEventResponse);

        EventResponseDto result = eventService.createEvent(mockCreateEventDto, managerEmail);

        assertNotNull(result);
        assertEquals("Tech Expo 2024", result.title());
        assertEquals("Technology", result.category());
        verify(sponsorRepository, times(1)).save(any(Sponsor.class));
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testCreateEvent_DatabaseConstraintViolation() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockManager));
        when(eventMapper.toEntity(any(CreateEventDto.class), any(User.class))).thenReturn(mockEvent);
        when(eventRepository.save(any(Event.class))).thenThrow(DataIntegrityViolationException.class);

        CustomDatabaseException thrown = assertThrows(CustomDatabaseException.class, () -> {
            eventService.createEvent(mockCreateEventDto, managerEmail);
        });

        assertEquals("Failed to save event due to database constraints", thrown.getMessage());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testCreateEvent_UserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            eventService.createEvent(mockCreateEventDto, managerEmail);
        });

        assertEquals("User not found with email: " + managerEmail, thrown.getMessage());

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void testGetEventById_Success() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(mockEvent));
        when(eventMapper.toDto(any(Event.class))).thenReturn(mockEventResponse);

        EventResponseDto result = eventService.getEventById(1L);

        assertNotNull(result);
        assertEquals("Tech Expo 2024", result.title());
        verify(eventRepository, times(1)).findById(1L);
    }

    @Test
    void testGetEventById_NotFound() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            eventService.getEventById(1L);
        });

        assertEquals("Event not found with id: 1", thrown.getMessage());
        verify(eventRepository, times(1)).findById(1L);
    }

    @Test
    void testGetAllEvents_Success() {
        when(eventRepository.findAll()).thenReturn(List.of(mockEvent));
        when(eventMapper.toDto(any(Event.class))).thenReturn(mockEventResponse);

        Set<EventResponseDto> result = eventService.getAllEvents();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void testGetEventsByCategory_Success() {
        when(eventRepository.findByCategory(any(EventCategory.class))).thenReturn(Set.of(mockEvent));
        when(eventMapper.toDto(any(Event.class))).thenReturn(mockEventResponse);

        Set<EventResponseDto> result = eventService.getEventsByCategory("Technology");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(eventRepository, times(1)).findByCategory(any(EventCategory.class));
    }

    @Test
    void testGetEventsByCategory_NotFound() {
        when(eventRepository.findByCategory(any(EventCategory.class))).thenReturn(Set.of());

        Set<EventResponseDto> result = eventService.getEventsByCategory("Technology");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(eventRepository, times(1)).findByCategory(any(EventCategory.class));
    }

    @Test
    void testGetEventsByLocation_Success() {
        when(eventLocationRepository.findById(anyLong())).thenReturn(Optional.of(mockEventLocation));
        when(eventRepository.findByLocation(any(EventLocation.class))).thenReturn(Set.of(mockEvent));
        when(eventMapper.toDto(any(Event.class))).thenReturn(mockEventResponse);

        Set<EventResponseDto> result = eventService.getEventsByLocation(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(eventRepository, times(1)).findByLocation(any(EventLocation.class));
    }

    @Test
    void testGetEventsByManager_Success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockManager));
        when(eventRepository.findByManager(any(User.class))).thenReturn(Set.of(mockEvent));
        when(eventMapper.toDto(any(Event.class))).thenReturn(mockEventResponse);

        Set<EventResponseDto> result = eventService.getEventsByManager(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(eventRepository, times(1)).findByManager(any(User.class));
    }

    @Test
    void testGetEventsByDateRange_Success() {
        when(
                eventRepository.findByStartDateBetween(any(LocalDateTime.class), any(LocalDateTime.class))
        ).thenReturn(Set.of(mockEvent));
        when(eventMapper.toDto(any(Event.class))).thenReturn(mockEventResponse);

        Set<EventResponseDto> result = eventService.getEventsByDateRange(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(eventRepository, times(1))
                .findByStartDateBetween(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testUpdateSponsors_Success() {
        Set<SponsorDto> sponsorDtos = new HashSet<>();
        sponsorDtos.add(mockSponsorDto);

        when(eventRepository.findById(any(Long.class))).thenReturn(Optional.of(mockEvent));
        when(sponsorRepository.findById(any(Long.class))).thenReturn(Optional.of(mockSponsor));
        when(sponsorMapper.toEntity(any(SponsorDto.class))).thenReturn(mockSponsor);
        when(eventMapper.toDto(any(Event.class))).thenReturn(mockEventResponse);

        when(sponsorRepository.saveAll(anyList())).thenReturn(List.of(mockSponsor));

        EventResponseDto result = eventService.updateSponsors(1L, sponsorDtos);

        verify(eventRepository).findById(1L);
        verify(sponsorRepository).findById(1L);
        verify(sponsorRepository).saveAll(anySet());
        assertEquals(sponsorDtos, result.sponsors());
    }

    @Test
    void updateSponsors_shouldThrowResourceNotFoundException_WhenEventNotFound() {
        Set<SponsorDto> sponsorDtos = new HashSet<>();
        sponsorDtos.add(mockSponsorDto);

        when(eventRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.updateSponsors(1L, sponsorDtos));
        verify(eventRepository).findById(1L);
        verifyNoMoreInteractions(sponsorRepository);
    }

    @Test
    void updateSponsors_shouldThrowFunctionalException_WhenSponsorNotFound() {
        Set<SponsorDto> sponsorDtos = new HashSet<>();
        sponsorDtos.add(mockSponsorDto);

        when(eventRepository.findById(any(Long.class))).thenReturn(Optional.of(mockEvent));
        when(sponsorRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        assertThrows(FunctionalException.class, () -> eventService.updateSponsors(1L, sponsorDtos));
        verify(eventRepository).findById(1L);
        verify(sponsorRepository).findById(1L);
    }

    @Test
    void updateSponsors_shouldThrowFunctionalException_WhenSponsorNotAssociatedWithEvent() {
        Set<SponsorDto> sponsorDtos = new HashSet<>();
        sponsorDtos.add(mockSponsorDto);

        Sponsor sponsor = Sponsor
                .builder()
                .id(1L)
                .build();

        when(eventRepository.findById(any(Long.class))).thenReturn(Optional.of(mockEvent));
        when(sponsorRepository.findById(any(Long.class))).thenReturn(Optional.of(sponsor));

        assertThrows(FunctionalException.class, () -> eventService.updateSponsors(1L, sponsorDtos));
        verify(eventRepository).findById(1L);
        verify(sponsorRepository).findById(1L);
    }

    @Test
    void testUpdateEventLocationWhenEventExists() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(mockEvent));
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.of(updatedLocation));
        when(eventLocationMapper.updateEntity(updatedLocation, mockEventLocationDto)).thenReturn(updatedLocation);
        when(eventLocationRepository.save(updatedLocation)).thenReturn(savedLocation);
        when(eventMapper.toDto(mockEvent)).thenReturn(mockEventResponse);

        EventResponseDto result = eventService.updateEventLocation(1L, mockEventLocationDto);

        assertEquals(mockEventResponse, result);
        verify(eventRepository, times(1)).findById(1L);
        verify(eventLocationRepository, times(1)).findById(1L);
        verify(eventLocationMapper, times(1)).updateEntity(updatedLocation, mockEventLocationDto);
        verify(eventLocationRepository, times(1)).save(updatedLocation);
        verify(eventMapper, times(1)).toDto(mockEvent);
    }

    @Test
    void testUpdateEventLocationWhenEventDoesNotExist() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, () ->
                eventService.updateEventLocation(1L, mockEventLocationDto)
        );

        assertEquals(String.format(APIsErrorCodesConstants.EVENT_NOT_FOUND, 1L), exception.getMessage());
        verify(eventRepository, times(1)).findById(1L);
        verify(eventLocationRepository, never()).findById(anyLong());
        verify(eventLocationMapper, never()).updateEntity(any(), any());
        verify(eventLocationRepository, never()).save(any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    void testUpdateEventLocationWhenLocationDoesNotExist() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(mockEvent));
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.empty());
        when(eventLocationMapper.toEntity(mockEventLocationDto)).thenReturn(updatedLocation);
        when(eventLocationRepository.save(updatedLocation)).thenReturn(savedLocation);
        when(eventMapper.toDto(mockEvent)).thenReturn(mockEventResponse);

        EventResponseDto result = eventService.updateEventLocation(1L, mockEventLocationDto);

        assertEquals(mockEventResponse, result);
        verify(eventRepository, times(1)).findById(1L);
        verify(eventLocationRepository, times(1)).findById(1L);
        verify(eventLocationMapper, times(1)).toEntity(mockEventLocationDto);
        verify(eventLocationRepository, times(1)).save(updatedLocation);
        verify(eventMapper, times(1)).toDto(mockEvent);
    }


    @Test
    void addSponsorsToEvent_shouldAddNewSponsorsSuccessfully() {
        Set<SponsorDto> sponsorDtos = Stream.of(
                new SponsorDto(null, "New Sponsor", "ss", "ss"),
                new SponsorDto(1L, "Existing Sponsor", "ss", "ss")
        ).collect(Collectors.toSet());

        Set<Sponsor> existingSponsors = new HashSet<>();
        mockEvent.setSponsors(existingSponsors);

        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(mockEvent));
        when(sponsorRepository.findById(anyLong())).thenReturn(Optional.of(mockSponsor));
        when(sponsorRepository.save(any(Sponsor.class))).thenReturn(mockSponsor);
        when(sponsorRepository.findById(null)).thenReturn(Optional.empty());
        when(sponsorMapper.toEntity(any(SponsorDto.class))).thenReturn(mockSponsor);
        when(eventRepository.save(any(Event.class))).thenReturn(mockEvent);
        when(eventMapper.toDto(any(Event.class))).thenReturn(mockEventResponse);

        EventResponseDto result = eventService.addSponsorsToEvent(1L, sponsorDtos);

        verify(eventRepository).findById(1L);
        verify(sponsorRepository, times(1)).findById(1L);
        verify(sponsorRepository, times(1)).save(any(Sponsor.class));
        verify(eventRepository).save(any(Event.class));
        assertEquals(mockEventResponse, result);
    }

    @Test
    void addSponsorsToEvent_shouldThrowResourceNotFoundException_whenEventNotFound() {
        Set<SponsorDto> sponsorDtos = new HashSet<>();

        when(eventRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> eventService.addSponsorsToEvent(1L, sponsorDtos)
        );

        assertEquals(String.format(APIsErrorCodesConstants.EVENT_NOT_FOUND, 1L), exception.getMessage());

        verify(eventRepository).findById(1L);
        verifyNoMoreInteractions(sponsorRepository);
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, never()).save(any());
        verify(eventMapper, never()).toDto(any());
    }


    @Test
    void testDeleteEventByIdWhenEventExists() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(mockEvent));

        eventService.deleteEventById(1L);

        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).delete(mockEvent);
    }

    @Test
    void testDeleteEventByIdWhenEventDoesNotExist() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, () ->
                eventService.deleteEventById(1L)
        );

        assertEquals(String.format(APIsErrorCodesConstants.EVENT_NOT_FOUND, 1L), exception.getMessage());
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, never()).delete(any());
    }

    // TODO:
    //   - testRemoveSponsorsFromEvent_Success
    //   - testRemoveSponsorsFromEventWhenEventExists

    @Test
    void testRemoveSponsorsFromEventWhenEventDoesNotExist() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, () ->
                eventService.removeSponsorsFromEvent(1L, Set.of(1L, 2L))
        );

        assertEquals(String.format(APIsErrorCodesConstants.EVENT_NOT_FOUND, 1L), exception.getMessage());
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, never()).save(any());
        verify(eventMapper, never()).toDto(any());
    }

    @Test
    void testDeleteManagerEventById_Success() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(mockEvent));

        eventService.deleteManagerEventById(1L, mockManager);

        verify(eventRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteManagerEventById_Unauthorized() {
        User anotherManager = User.builder().email("another_manager@gmail.com").build();
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(mockEvent));

        BadCredentialsException thrown = assertThrows(BadCredentialsException.class, () -> {
            eventService.deleteManagerEventById(1L, anotherManager);
        });

        assertEquals("You are not authorized to delete this event.", thrown.getMessage());
        verify(eventRepository, never()).deleteById(anyLong());
    }

    @Test
    void testDeleteManagerEventById_EventNotFound() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            eventService.deleteManagerEventById(1L, mockManager);
        });

        assertEquals("Event not found with id: 1", thrown.getMessage());
        verify(eventRepository, never()).deleteById(anyLong());
    }

    @Test
    void testUpdateEventById_Success() {
        UpdateEventDto mockUpdateEventDto = UpdateEventDto.builder()
                .title("Updated Tech Expo 2024")
                .build();

        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(mockEvent));
        when(eventMapper.updateEntity(any(Event.class), any(UpdateEventDto.class))).thenReturn(mockEvent);
        when(eventMapper.toDto(any(Event.class))).thenReturn(mockEventResponse);

        EventResponseDto result = eventService.updateEventById(1L, mockUpdateEventDto);

        assertNotNull(result);
        assertEquals("Tech Expo 2024", result.title());
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testUpdateEventById_EventNotFound() {
        UpdateEventDto mockUpdateEventDto = UpdateEventDto.builder()
                .title("Updated Tech Expo 2024")
                .build();

        when(eventRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            eventService.updateEventById(1L, mockUpdateEventDto);
        });

        assertEquals("Event not found with id: 1", thrown.getMessage());
        verify(eventRepository, never()).save(any(Event.class));
    }
}
