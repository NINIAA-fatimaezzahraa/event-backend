package com.oclock.event_backend;

import com.oclock.event_backend.domain.RefreshToken;
import com.oclock.event_backend.domain.User;
import com.oclock.event_backend.dto.AuthRequest;
import com.oclock.event_backend.dto.AuthResponse;
import com.oclock.event_backend.dto.RegisterRequest;
import com.oclock.event_backend.dto.RegisterResponse;
import com.oclock.event_backend.exception.FunctionalException;
import com.oclock.event_backend.mapper.UserMapper;
import com.oclock.event_backend.repository.UserRepository;
import com.oclock.event_backend.service.AuthServiceImpl;
import com.oclock.event_backend.service.RefreshTokenService;
import com.oclock.event_backend.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    private AutoCloseable openMocks;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    void testRegisterUser_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .email("alpha@gmail.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();

        RegisterResponse response = RegisterResponse.builder()
                .email("alpha@gmail.com")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .createdDate(LocalDateTime.now())
                .build();

        User user = User.builder()
                .email("alpha@gmail.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userMapper.toEntity(any())).thenReturn(user);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.toDto(any())).thenReturn(response);

        RegisterResponse registerResponse = authService.registerUser(request);
        assertNotNull(registerResponse);
        assertEquals("alpha@gmail.com", registerResponse.email());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterUser_ThrowsFunctionalException() {
        RegisterRequest request = RegisterRequest.builder()
                .email("alpha@gmail.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(new User()));

        FunctionalException thrown = assertThrows(FunctionalException.class, () -> authService.registerUser(request));

        assertEquals("User with email= alpha@gmail.com already exists.", thrown.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLoginUser_Success() {
        AuthRequest request = AuthRequest.builder()
                .email("alpha@gmail.com")
                .password("password123")
                .build();

        User user = User.builder()
                .email("alpha@gmail.com")
                .password("encodedPassword")
                .isActive(true)
                .build();

        RefreshToken refreshToken = RefreshToken.builder()
                .token("refreshToken123")
                .user(user)
                .build();

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(jwtUtil.checkPassword(any(), any())).thenReturn(true);
        when(jwtUtil.generateAccessToken(any())).thenReturn("accessToken");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(refreshToken);

        AuthResponse response = authService.loginUser(request);

        assertNotNull(response);
        assertEquals("accessToken", response.accessToken());
        assertEquals("refreshToken123", response.refreshToken());
    }

    @Test
    void testLoginUser_InvalidCredentials() {
        AuthRequest request = AuthRequest.builder()
                .email("alpha@gmail.com")
                .password("wrongPassword")
                .build();

        User user = User.builder()
                .email("alpha@gmail.com")
                .password("encodedPassword")
                .isActive(true)
                .build();

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(jwtUtil.checkPassword(any(), any())).thenReturn(false);

        FunctionalException thrown = assertThrows(FunctionalException.class, () -> authService.loginUser(request));
        assertEquals("Invalid credentials.", thrown.getMessage());
    }

    @Test
    void testLoginUser_InactiveUser() {
        AuthRequest request = AuthRequest.builder()
                .email("alpha@gmail.com")
                .password("password123")
                .build();

        User user = User.builder()
                .email("alpha@gmail.com")
                .password("encodedPassword")
                .isActive(false)
                .build();

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        FunctionalException thrown = assertThrows(FunctionalException.class, () -> authService.loginUser(request));
        assertEquals("User is inactive.", thrown.getMessage());
    }

    @Test
    void testLoginUser_UserNotFound() {
        AuthRequest request = AuthRequest.builder()
                .email("unknown@gmail.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        FunctionalException thrown = assertThrows(FunctionalException.class, () -> authService.loginUser(request));
        assertEquals("User not found.", thrown.getMessage());
    }
}
