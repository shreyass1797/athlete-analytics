package com.shreyass.athlete_analytics.repository;

import org.springframework.data.domain.Page; // Import this!
import org.springframework.data.domain.Pageable; // Import this!
import org.springframework.data.jpa.repository.JpaRepository;

import com.shreyass.athlete_analytics.model.WorkoutLog;

public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {
    
    // Spring Data JPA automatically writes the LIMIT/OFFSET SQL for this
    Page<WorkoutLog> findAllByUserId(Long userId, Pageable pageable);
}