package com.shreyass.athlete_analytics.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shreyass.athlete_analytics.config.JwtService;
import com.shreyass.athlete_analytics.dto.AuthenticationRequest;
import com.shreyass.athlete_analytics.dto.AuthenticationResponse;
import com.shreyass.athlete_analytics.dto.RegisterRequest;
import com.shreyass.athlete_analytics.model.Role;
import com.shreyass.athlete_analytics.model.User;
import com.shreyass.athlete_analytics.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        
        // 1. Enforce Uniqueness Rules gracefully
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already registered.");
        }
        if (userRepository.findByDisplayName(request.getDisplayName()).isPresent()) {
            throw new IllegalArgumentException("Display name is already taken.");
        }

        // 2. Build the User and hash the password
        var user = User.builder()
                .displayName(request.getDisplayName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        // 3. Save to PostgreSQL
        userRepository.save(user);

        // 4. Generate the VIP Pass
        var jwtToken = jwtService.generateToken(user);
        
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .displayName(user.getDisplayName())
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // 1. Spring Security checks the password against the BCrypt hash here
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. If we reach this line, the password was correct
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(); // We know they exist because authentication passed

        // 3. Generate the VIP Pass
        var jwtToken = jwtService.generateToken(user);
        
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .displayName(user.getDisplayName())
                .build();
    }
}