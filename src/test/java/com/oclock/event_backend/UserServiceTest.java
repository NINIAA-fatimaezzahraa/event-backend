package com.oclock.event_backend;

import com.oclock.event_backend.domain.User;
import com.oclock.event_backend.dto.ProfileRequest;
import com.oclock.event_backend.dto.ProfileResponse;
import com.oclock.event_backend.dto.UpdatePasswordRequest;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @Mock
    private PasswordEncoder passwordEncoder;

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

    @Test
    void testUpdateUserProfile_Success() {
        String email = "test@example.com";
        ProfileRequest profileRequest = ProfileRequest
                .builder()
                .firstName("Updated FirstName")
                .lastName("Updated LastName")
                .build();

        User mockUser = User
                .builder()
                .firstName("Old FirstName")
                .lastName("Old LastName")
                .email(email)
                .build();

        ProfileResponse profileResponse = ProfileResponse
                .builder()
                .firstName("Updated FirstName")
                .lastName("Updated LastName")
                .email(email)
                .isActive(true)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(userMapper.toDto(mockUser)).thenReturn(profileResponse);

        ProfileResponse response = userService.updateUserProfile(email, profileRequest);

        assertEquals("Updated FirstName", response.firstName());
        assertEquals("Updated LastName", response.lastName());
        verify(userRepository, times(1)).save(mockUser);
        verify(userMapper, times(1)).toDto(mockUser);
    }

    @Test
    void testUpdateUserProfile_UserNotFound() {
        String email = "notfound@example.com";
        ProfileRequest profileRequest = ProfileRequest
                .builder()
                .firstName("Updated FirstName")
                .lastName("Updated LastName")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUserProfile(email, profileRequest);
        });

        assertEquals("User not found with email: " + email, thrown.getMessage());

        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void testUpdatePassword_Success() {
        String email = "test@example.com";
        UpdatePasswordRequest passwordRequest = UpdatePasswordRequest
                .builder()
                .currentPassword("currentPassword")
                .newPassword("newPassword")
                .build();

        User mockUser = User
                .builder()
                .email(email)
                .password("hashedOldPassword").build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        when(
            passwordEncoder.matches(passwordRequest.currentPassword(), mockUser.getPassword())
        ).thenReturn(true);

        when(passwordEncoder.encode(passwordRequest.newPassword())).thenReturn("hashedNewPassword");

        userService.updatePassword(email, passwordRequest);

        verify(
            passwordEncoder, times(1)
        ).matches(passwordRequest.currentPassword(), "hashedOldPassword");
        verify(passwordEncoder, times(1)).encode(passwordRequest.newPassword());
        verify(userRepository, times(1)).save(mockUser);

        assertEquals("hashedNewPassword", mockUser.getPassword());
    }


    @Test
    void testUpdatePassword_UserNotFound() {
        String email = "notfound@example.com";
        UpdatePasswordRequest passwordRequest = UpdatePasswordRequest
                .builder()
                .currentPassword("oldPassword")
                .newPassword("newPassword")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updatePassword(email, passwordRequest);
        });

        assertEquals("User not found with email: " + email, thrown.getMessage());

        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testUpdatePassword_BadCredentials() {
        String email = "test@example.com";
        UpdatePasswordRequest passwordRequest = UpdatePasswordRequest
                .builder()
                .currentPassword("oldPassword")
                .newPassword("newPassword")
                .build();

        User mockUser = User
                .builder()
                .email(email)
                .password("hashedOldPassword")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(passwordRequest.currentPassword(), mockUser.getPassword())).thenReturn(false);

        BadCredentialsException thrown = assertThrows(BadCredentialsException.class, () -> {
            userService.updatePassword(email, passwordRequest);
        });

        assertEquals("Current password is incorrect.", thrown.getMessage());

        verify(passwordEncoder, times(1)).matches(passwordRequest.currentPassword(), mockUser.getPassword());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateEmail_Success() {
        String currentEmail = "test@example.com";
        String newEmail = "newemail@example.com";

        User mockUser = User
                .builder()
                .email(currentEmail)
                .build();

        when(userRepository.findByEmail(currentEmail)).thenReturn(Optional.of(mockUser));
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);

        userService.updateEmail(currentEmail, newEmail);

        verify(userRepository, times(1)).findByEmail(currentEmail);
        verify(userRepository, times(1)).existsByEmail(newEmail);
        verify(userRepository, never()).save(any());  // Assuming the email change happens after email verification
    }

    @Test
    void testUpdateEmail_EmailAlreadyExists() {
        String currentEmail = "test@example.com";
        String newEmail = "newemail@example.com";

        User mockUser = User
                .builder()
                .email(currentEmail)
                .build();

        when(userRepository.findByEmail(currentEmail)).thenReturn(Optional.of(mockUser));
        when(userRepository.existsByEmail(newEmail)).thenReturn(true);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateEmail(currentEmail, newEmail);
        });

        assertEquals("Email is already in use.", thrown.getMessage());

        verify(userRepository, times(1)).findByEmail(currentEmail);
        verify(userRepository, times(1)).existsByEmail(newEmail);
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateEmail_UserNotFound() {
        String currentEmail = "notfound@example.com";
        String newEmail = "newemail@example.com";

        when(userRepository.findByEmail(currentEmail)).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateEmail(currentEmail, newEmail);
        });

        assertEquals("User not found with email: " + currentEmail, thrown.getMessage());

        verify(userRepository, times(1)).findByEmail(currentEmail);
        verify(userRepository, never()).existsByEmail(newEmail);
        verify(userRepository, never()).save(any());
    }
}
