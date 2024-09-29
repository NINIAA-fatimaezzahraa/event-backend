package com.oclock.event_backend.service;

import com.oclock.event_backend.domain.User;
import com.oclock.event_backend.dto.AuthRequest;
import com.oclock.event_backend.dto.AuthResponse;
import com.oclock.event_backend.dto.ProfileRequest;
import com.oclock.event_backend.dto.ProfileResponse;
import com.oclock.event_backend.exception.EmptyInputException;
import com.oclock.event_backend.exception.FunctionalException;
import com.oclock.event_backend.exception.ResourceNotFoundException;
import com.oclock.event_backend.mapper.UserMapper;
import com.oclock.event_backend.repository.UserRepository;
import com.oclock.event_backend.util.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class AuthServiceImpl implements UserDetailsService, AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ProfileResponse registerUser(ProfileRequest registerRequest) {
        if (registerRequest.email() == null || registerRequest.email().isEmpty()) {
            throw new EmptyInputException("Email is required.");
        }
        if (registerRequest.password() == null || registerRequest.password().isEmpty()) {
            throw new EmptyInputException("Password is required.");
        }

        if (userRepository.findByEmail(registerRequest.email()).isPresent()) {
            throw new FunctionalException("User with email= " +registerRequest.email()+  " already exists.");
        }

        User user = userMapper.toEntity(registerRequest);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return userMapper.toDto(user);
    }

    public AuthResponse loginUser(AuthRequest authRequest) {
        if (authRequest.email() == null || authRequest.email().isEmpty()) {
            throw new EmptyInputException("Email is required.");
        }
        if (authRequest.password() == null || authRequest.password().isEmpty()) {
            throw new EmptyInputException("Password is required.");
        }

        Optional<User> optionalUser = userRepository.findByEmail(authRequest.email());
        if (optionalUser.isEmpty()) {
            throw new FunctionalException("User not found.");
        }

        User user = optionalUser.get();
        if (!user.isActive()) {
            throw new FunctionalException("User is inactive.");
        }

        if (!jwtUtil.checkPassword(authRequest.password(), user.getPassword())) {
            throw new FunctionalException("Invalid credentials.");
        }

        user.setLastLoginDate(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}
