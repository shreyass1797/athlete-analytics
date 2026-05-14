package com.shreyass.athlete_analytics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shreyass.athlete_analytics.model.AthleteInsights;

public interface AthleteInsightsRepository extends JpaRepository<AthleteInsights, Long> {
    List<AthleteInsights> findByUserIdOrderByDateDesc(Long userId);
    
}
