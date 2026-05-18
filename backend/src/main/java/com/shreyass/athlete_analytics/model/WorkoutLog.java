package com.shreyass.athlete_analytics.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Entity
@Table(name = "workout_logs", indexes = {
    @Index(name = "idx_workout_logs_user_date", columnList = "user_id, date DESC")
})
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