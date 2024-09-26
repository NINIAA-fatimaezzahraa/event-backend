package com.oclock.event_backend.service;

import com.oclock.event_backend.domain.User;
import com.oclock.event_backend.dto.ProfileResponse;
import com.oclock.event_backend.exception.ResourceNotFoundException;
import com.oclock.event_backend.mapper.UserMapper;
import com.oclock.event_backend.repository.UserRepository;
import com.oclock.event_backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
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

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

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

    // TODO: implementation will be completed after create event schema
    @Override
    public Set<ProfileResponse> getUsersByEventIdForManager(Long eventId, String managerEmail) {
        return new HashSet<>();
    }
}
