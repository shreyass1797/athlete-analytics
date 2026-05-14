package com.shreyass.athlete_analytics.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "workout_logs")
public class WorkoutLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkoutType type;

    private Integer durationMinutes;
    private String primaryFocus; // e.g., "Muscle-up progression", "5K recovery run"

    // Flexible metrics: Use volumeLoad for lifting/calisthenics, distanceKm for cardio
    private Integer volumeLoad; 
    private Double distanceKm;  

    @Min(1) @Max(10)
    private Integer rpe; // Rate of Perceived Exertion (1 = Easy, 10 = Max Effort)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}