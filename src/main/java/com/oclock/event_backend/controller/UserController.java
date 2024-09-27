package com.oclock.event_backend.controller;

import com.oclock.event_backend.dto.ProfileRequest;
import com.oclock.event_backend.dto.ProfileResponse;
import com.oclock.event_backend.dto.UpdatePasswordRequest;
import com.oclock.event_backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@AllArgsConstructor
@RequestMapping("api/users")
public class UserController {
    private UserService userService;

    @GetMapping("/")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Set<ProfileResponse>> getAllUsers() {
        Set<ProfileResponse> responses = userService.getAllUsers();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('ROLE_PARTICIPANT')")
    public ResponseEntity<ProfileResponse> getOwnProfile(HttpServletRequest request) {
        String email = userService.getAuthenticatedUserEmail(request);
        ProfileResponse response = userService.getUserProfileByEmail(email);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('ROLE_PARTICIPANT') or hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public ResponseEntity<ProfileResponse> updateProfile(
            @RequestBody ProfileRequest request, HttpServletRequest httpServletRequest) {
        String email = userService.getAuthenticatedUserEmail(httpServletRequest);
        ProfileResponse updatedProfile = userService.updateUserProfile(email, request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PutMapping("/profile/password")
    @PreAuthorize("hasRole('ROLE_PARTICIPANT') or hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public ResponseEntity<String> updatePassword(
            @RequestBody UpdatePasswordRequest request, HttpServletRequest httpServletRequest
    ) {
        String email = userService.getAuthenticatedUserEmail(httpServletRequest);
        userService.updatePassword(email, request);
        return ResponseEntity.ok("Password updated successfully.");
    }

    @PutMapping("/profile/email")
    @PreAuthorize("hasRole('ROLE_PARTICIPANT') or hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public ResponseEntity<String> updateEmail(
            @RequestParam String newEmail, HttpServletRequest httpServletRequest) {
        String email = userService.getAuthenticatedUserEmail(httpServletRequest);
        userService.updateEmail(email, newEmail);
        return ResponseEntity.ok("Verification link sent to the new email.");
    }

    @GetMapping("/manager/events/{eventId}/participants")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ResponseEntity<Set<ProfileResponse>> getUsersForManagerEvent(
            @PathVariable Long eventId, HttpServletRequest request
    ) {
        String email = userService.getAuthenticatedUserEmail(request);
        Set<ProfileResponse> responses = userService.getUsersByEventIdForManager(eventId, email);
        return ResponseEntity.ok(responses);
    }
}
