package com.oclock.event_backend.service;

import com.oclock.event_backend.dto.ProfileResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Set;

public interface UserService {
    String getAuthenticatedUserEmail(HttpServletRequest request);
    ProfileResponse getUserProfileByEmail(String email);
    Set<ProfileResponse> getAllUsers();
    Set<ProfileResponse> getUsersByEventIdForManager(Long eventId, String managerEmail);
}
