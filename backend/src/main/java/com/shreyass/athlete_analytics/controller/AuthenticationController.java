package com.shreyass.athlete_analytics.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shreyass.athlete_analytics.dto.AuthenticationRequest;
import com.shreyass.athlete_analytics.dto.AuthenticationResponse;
import com.shreyass.athlete_analytics.dto.RegisterRequest;
import com.shreyass.athlete_analytics.service.AuthenticationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "5. Authentication", description = "Endpoints for user registration and login, handling JWT token issuance securely.")
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    @Operation(
        summary = "Register a new user", 
        description = "Creates a new user account and returns an authentication token. Validates uniqueness of username and email, returning appropriate error messages without exposing sensitive information."
    )
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(service.register(request));
        } catch (IllegalArgumentException e) {
            // Catches our uniqueness errors and returns a 400 Bad Request safely
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    @Operation(
        summary = "Login", 
        description = "Authenticates a user and returns a JWT token."
    )
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }
}