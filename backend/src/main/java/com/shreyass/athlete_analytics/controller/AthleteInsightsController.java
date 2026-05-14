package com.shreyass.athlete_analytics.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shreyass.athlete_analytics.model.AthleteInsights;
import com.shreyass.athlete_analytics.model.User;
import com.shreyass.athlete_analytics.service.AthleteInsightService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/insights")
@Tag(name = "4. Athlete Insights", description = "Endpoints for logging and retrieving personalized insights derived from workouts, field sessions, and daily metrics.")
public class AthleteInsightsController {

    private final AthleteInsightService insightService;

    // 1. URL no longer accepts a path variable
    @PostMapping("/log") 
    @Operation(
        summary = "Log a new athlete insight", 
        description = "Saves a new insight to PostgreSQL. Requires a valid JWT token. The user ID is securely extracted from the token, not passed in the request body."
    )
    public ResponseEntity<AthleteInsights> logInsight(
            @RequestBody AthleteInsights insight, 
            @AuthenticationPrincipal User currentUser // 2. Extract identity securely
    ) {
        // 3. Pass the guaranteed secure ID to the service layer
        AthleteInsights savedInsight = insightService.saveInsight(insight, currentUser.getId());
        return ResponseEntity.ok(savedInsight);
    }

    @GetMapping("/me")
    @Operation(
        summary = "Get user athlete insights", 
        description = "Retrieves a list of all personalized insights for the authenticated athlete."
    )
    public ResponseEntity<?> getMyInsights(@AuthenticationPrincipal User currentUser) {
        Long secureUserId = currentUser.getId(); 
        return ResponseEntity.ok(insightService.getInsightsByUser(secureUserId)); 
    }
}