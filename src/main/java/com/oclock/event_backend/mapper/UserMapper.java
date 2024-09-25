package com.oclock.event_backend.mapper;

import com.oclock.event_backend.domain.Role;
import com.oclock.event_backend.domain.User;
import com.oclock.event_backend.dto.RegisterRequest;
import com.oclock.event_backend.dto.RegisterResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {
    public User toEntity(RegisterRequest registerRequest) {
        Role role = registerRequest.role() != null ? Role.valueOf(registerRequest.role().toUpperCase()) : Role.PARTICIPANT;

        return User.builder()
                .email(registerRequest.email())
                .password(registerRequest.password())
                .firstName(registerRequest.firstName())
                .lastName(registerRequest.lastName())
                .createdDate(LocalDateTime.now())
                .isActive(true)
                .role(role)
                .build();
    }

    public RegisterResponse toDto(User user) {
        return RegisterResponse.builder()
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isActive(user.isActive())
                .createdDate(user.getCreatedDate())
                .build();
    }
}
