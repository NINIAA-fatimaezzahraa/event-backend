package com.oclock.event_backend.service;

import com.oclock.event_backend.dto.ProfileRequest;
import com.oclock.event_backend.dto.ProfileResponse;
import com.oclock.event_backend.dto.UpdatePasswordRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Set;

public interface UserService {
    String getAuthenticatedUserEmail(HttpServletRequest request);
    ProfileResponse getUserProfileByEmail(String email);
    Set<ProfileResponse> getAllUsers();
    void deleteUserById(Long userId);
    void updateUserStatus(Long userId, boolean isActive);
    Set<ProfileResponse> getUsersByEventIdForManager(Long eventId, String managerEmail);
    ProfileResponse updateUserProfile(String email, ProfileRequest request);
    void updatePassword(String email, UpdatePasswordRequest newPassword);
    void updateEmail(String currentEmail, String newEmail);
}
