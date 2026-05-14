package com.shreyass.athlete_analytics.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shreyass.athlete_analytics.model.FieldSession;
import com.shreyass.athlete_analytics.model.User;
import com.shreyass.athlete_analytics.service.FieldSessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
@Tag(name = "2. Field Session Tracker", description = "Endpoints for logging and retrieving field sessions like sprints, jumps, and throws.")
public class FieldSessionController {

    private final FieldSessionService sessionService;

    @PostMapping("/log")
    @Operation(
        summary = "Log a new field session", 
        description = "Saves a new field session to PostgreSQL. Requires a valid JWT token."
    )
    public ResponseEntity<FieldSession> logSession(
            @RequestBody FieldSession session,
            @AuthenticationPrincipal User currentUser
    ) {
        FieldSession savedSession = sessionService.saveSession(currentUser.getId(), session);
        return ResponseEntity.ok(savedSession);
    }

    @GetMapping("/me")
    @Operation(
        summary = "Get user field session history", 
        description = "Retrieves a paginated list of all past field sessions for the authenticated athlete."
    )
    public ResponseEntity<Page<FieldSession>> getMySessions(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(sessionService.getSessionsByUser(currentUser.getId(), page, size));
    }
}