package com.oclock.event_backend.service;

import com.oclock.event_backend.domain.User;
import com.oclock.event_backend.dto.AuthRequest;
import com.oclock.event_backend.dto.AuthResponse;
import com.oclock.event_backend.dto.RegisterRequest;
import com.oclock.event_backend.dto.RegisterResponse;
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

    private UserRepository userRepository;
    private UserMapper userMapper;
    private JwtUtil jwtUtil;

    private RefreshTokenService refreshTokenService;

    private PasswordEncoder passwordEncoder;

    @Override
    public RegisterResponse registerUser(RegisterRequest registerRequest) {
        if (registerRequest.email() == null || registerRequest.password() == null) {
            throw new IllegalArgumentException("Email and Password are required.");
        }

        User user = userMapper.toEntity(registerRequest);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        return userMapper.toDto(user);
    }

    public AuthResponse loginUser(AuthRequest authRequest) {
        Optional<User> optionalUser = userRepository.findByEmail(authRequest.email());

        if (optionalUser.isEmpty() || !optionalUser.get().isActive()) {
            throw new IllegalArgumentException("User not found or inactive.");
        }

        User user = optionalUser.get();

        if (!jwtUtil.checkPassword(authRequest.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
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
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
