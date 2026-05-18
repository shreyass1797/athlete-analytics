package com.shreyass.athlete_analytics.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "daily_metrics", indexes = {
    @Index(name = "idx_daily_metrics_user_date", columnList = "user_id, date DESC", unique = true)
})
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