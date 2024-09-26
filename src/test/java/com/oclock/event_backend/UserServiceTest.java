package com.oclock.event_backend;

import com.oclock.event_backend.domain.User;
import com.oclock.event_backend.dto.ProfileResponse;
import com.oclock.event_backend.exception.ResourceNotFoundException;
import com.oclock.event_backend.mapper.UserMapper;
import com.oclock.event_backend.repository.UserRepository;
import com.oclock.event_backend.service.UserServiceImpl;
import com.oclock.event_backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private AutoCloseable openMocks;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }
    @Test
    void testGetAuthenticatedUserEmail() {
        String token = "Bearer mockToken";
        String expectedEmail = "test@example.com";

        when(httpServletRequest.getHeader("Authorization")).thenReturn(token);
        when(jwtUtil.extractUsername(anyString())).thenReturn(expectedEmail);

        String email = userService.getAuthenticatedUserEmail(httpServletRequest);

        assertEquals(expectedEmail, email);
        verify(jwtUtil, times(1)).extractUsername(anyString());
    }

    @Test
    void testGetUserProfileByEmail_Success() {
        String email = "test@example.com";

        User mockUser = User
                .builder()
                .email(email)
                .build();

        ProfileResponse mockProfileResponse = ProfileResponse
                .builder()
                .email(email)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(userMapper.toDto(mockUser)).thenReturn(mockProfileResponse);

        ProfileResponse profileResponse = userService.getUserProfileByEmail(email);

        assertNotNull(profileResponse);
        assertEquals(email, profileResponse.email());
        verify(userRepository, times(1)).findByEmail(email);
        verify(userMapper, times(1)).toDto(mockUser);
    }

    @Test
    void testGetUserProfileByEmail_UserNotFound() {
        String email = "notfound@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserProfileByEmail(email);
        });

        assertEquals("User not found with email: " + email, thrown.getMessage());

        verify(userRepository, times(1)).findByEmail(email);
        verify(userMapper, times(0)).toDto(any());
    }

    @Test
    void testGetAllUsers_Success() {
        User user1 = User
                .builder()
                .email("user1@gmail.com")
                .build();

        User user2 = User
                .builder()
                .email("user2@gmail.com")
                .build();

        Set<User> mockUsers = new HashSet<>();
        mockUsers.add(user1);
        mockUsers.add(user2);

        ProfileResponse profile1 = ProfileResponse
                .builder()
                .email("user1@gmail.com")
                .build();

        ProfileResponse profile2 = ProfileResponse
                .builder()
                .email("user2@gmail.com")
                .build();

        when(userRepository.findAll()).thenReturn(new ArrayList<>(mockUsers));
        when(userMapper.toDto(user1)).thenReturn(profile1);
        when(userMapper.toDto(user2)).thenReturn(profile2);

        Set<ProfileResponse> profiles = userService.getAllUsers();

        assertEquals(2, profiles.size());
        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(2)).toDto(any(User.class));
    }

    @Test
    void testGetAllUsers_Empty() {
        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        Set<ProfileResponse> profiles = userService.getAllUsers();

        assertTrue(profiles.isEmpty());
        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(0)).toDto(any());
    }



}
