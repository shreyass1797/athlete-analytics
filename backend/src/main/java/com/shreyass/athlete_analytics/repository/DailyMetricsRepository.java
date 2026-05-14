package com.shreyass.athlete_analytics.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.shreyass.athlete_analytics.model.DailyMetrics;

public interface DailyMetricsRepository extends JpaRepository<DailyMetrics, Long> {
    Optional<DailyMetrics> findByUser_IdAndDate(Long userId, LocalDate date);
    Optional<DailyMetrics> findByUserId(Long userId);
    Page<DailyMetrics> findAllByUserId(Long userId, Pageable pageable);
}