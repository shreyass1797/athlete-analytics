package com.shreyass.athlete_analytics.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "daily_metrics")
public class DailyMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    private Double sleepHours;
    private Double morningWeightKg;
    private Integer restingHeartRate;
    
    @Min(1)
    @Max(10)
    private Integer sorenessScore; // 1 = Fresh, 10 = Completely destroyed

    // The relational link back to the User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}