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

import com.shreyass.athlete_analytics.model.User;
import com.shreyass.athlete_analytics.model.WorkoutLog;
import com.shreyass.athlete_analytics.service.WorkoutLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workouts")
@Tag(name = "1. Workout Tracker", description = "Core endpoints for logging and retrieving calisthenics and gym sessions.")
public class WorkoutLogController {

    private final WorkoutLogService workoutLogService;

    @PostMapping("/log")
    @Operation(
        summary = "Log a new workout", 
        description = "Saves a new workout session to PostgreSQL. Requires a valid JWT token."
    )
    public ResponseEntity<WorkoutLog> logWorkout(
            @RequestBody WorkoutLog workoutLog,
            @AuthenticationPrincipal User currentUser
    ) {
        // Securely pass the ID derived from the JWT
        WorkoutLog savedLog = workoutLogService.saveWorkout(currentUser.getId(), workoutLog);
        return ResponseEntity.ok(savedLog);
    }

    @GetMapping("/me")
    @Operation(
        summary = "Get user workout history", 
        description = "Retrieves a paginated list of all past workouts for the authenticated athlete."
    )
    public ResponseEntity<Page<WorkoutLog>> getMyWorkouts(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page, // Spring uses 0-indexed pages
            @RequestParam(defaultValue = "10") int size // 10 items per page
    ) {
        return ResponseEntity.ok(workoutLogService.getWorkoutsByUser(currentUser.getId(), page, size));
    }
}