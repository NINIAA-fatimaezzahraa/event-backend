package com.oclock.event_backend.service;

import com.oclock.event_backend.domain.User;
import com.oclock.event_backend.dto.ProfileRequest;
import com.oclock.event_backend.dto.ProfileResponse;
import com.oclock.event_backend.dto.UpdatePasswordRequest;
import com.oclock.event_backend.exception.ResourceNotFoundException;
import com.oclock.event_backend.mapper.UserMapper;
import com.oclock.event_backend.repository.UserRepository;
import com.oclock.event_backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String getAuthenticatedUserEmail(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractUsername(token);
    }

    @Override
    public ProfileResponse getUserProfileByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User not found with email: " + email);
        }
        return userMapper.toDto(user.get());
    }

    @Override
    public Set<ProfileResponse> getAllUsers() {
        return userRepository
                .findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toSet());
    }

    @Override
    public ProfileResponse updateUserProfile(String email, ProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }

        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }

        userRepository.save(user);

        return userMapper.toDto(user);
    }

    @Override
    public void updatePassword(String email, UpdatePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    public void updateEmail(String currentEmail, String newEmail) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentEmail));

        if (userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("Email is already in use.");
        }

        logger.info("user information" + user);

        // TODO: Implement email verification sending after emailService implementation
        // emailService.sendVerificationEmail(user, newEmail);
    }



    // TODO: implementation will be completed after create event schema
    @Override
    public Set<ProfileResponse> getUsersByEventIdForManager(Long eventId, String managerEmail) {
        return new HashSet<>();
    }
}
