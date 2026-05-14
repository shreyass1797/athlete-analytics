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

import com.shreyass.athlete_analytics.model.DailyMetrics;
import com.shreyass.athlete_analytics.model.User;
import com.shreyass.athlete_analytics.service.DailyMetricsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/metrics")
@Tag(name = "3. Daily Metrics Logger", description = "Endpoints for logging and retrieving daily metrics such as sleep, nutrition, and mood.")
public class DailyMetricsController {

    private final DailyMetricsService metricsService;

    @PostMapping("/log")
    @Operation(
        summary = "Log daily metrics", 
        description = "Saves daily metrics to PostgreSQL. Requires a valid JWT token."
    )
    public ResponseEntity<DailyMetrics> logMetrics(
            @RequestBody DailyMetrics metrics,
            @AuthenticationPrincipal User currentUser
    ) {
        DailyMetrics savedMetrics = metricsService.saveMetrics(currentUser.getId(),metrics);
        return ResponseEntity.ok(savedMetrics);
    }

    @GetMapping("/me")
    @Operation(
        summary = "Get user daily metrics history", 
        description = "Retrieves a paginated list of all past daily metrics entries for the authenticated athlete."
    )
    public ResponseEntity<Page<DailyMetrics>> getMyMetrics(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(metricsService.getMetricsByUser(currentUser.getId(), page, size));
    }
}