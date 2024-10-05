package com.oclock.event_backend;

import com.oclock.event_backend.domain.*;
import com.oclock.event_backend.dto.CreateEventDto;
import com.oclock.event_backend.dto.EventResponseDto;
import com.oclock.event_backend.dto.EventLocationDto;
import com.oclock.event_backend.dto.SponsorDto;
import com.oclock.event_backend.exception.CustomDatabaseException;
import com.oclock.event_backend.exception.ResourceNotFoundException;
import com.oclock.event_backend.mapper.EventLocationMapper;
import com.oclock.event_backend.mapper.EventMapper;
import com.oclock.event_backend.mapper.SponsorMapper;
import com.oclock.event_backend.repository.EventLocationRepository;
import com.oclock.event_backend.repository.EventRepository;
import com.oclock.event_backend.repository.SponsorRepository;
import com.oclock.event_backend.repository.UserRepository;
import com.oclock.event_backend.service.EventServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

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
}
