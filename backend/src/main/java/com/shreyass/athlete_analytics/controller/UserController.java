package com.shreyass.athlete_analytics.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shreyass.athlete_analytics.dto.UserProfileResponse;
import com.shreyass.athlete_analytics.model.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@Tag(name = "6. User Profile", description = "Endpoint for retrieving the authenticated user's profile information. The user ID is securely extracted from the JWT token, ensuring that users can only access their own data.")
public class UserController {

    @GetMapping("/me")
    @Operation(
        summary = "Get current user profile", 
        description = "Retrieves the profile information of the authenticated user. The user ID is securely extracted from the JWT token, ensuring that users can only access their own data."
    )
    public ResponseEntity<UserProfileResponse> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        
        // Map the secure entity to the safe DTO
        UserProfileResponse profile = UserProfileResponse.builder()
                .id(currentUser.getId())
                .email(currentUser.getEmail())
                .displayName(currentUser.getDisplayName())
                .build();
                
        return ResponseEntity.ok(profile);
    }
}