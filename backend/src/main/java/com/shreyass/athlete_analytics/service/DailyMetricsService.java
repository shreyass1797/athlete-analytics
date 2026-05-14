package com.shreyass.athlete_analytics.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.shreyass.athlete_analytics.exception.DuplicateMetricException;
import com.shreyass.athlete_analytics.model.DailyMetrics;
import com.shreyass.athlete_analytics.model.User;
import com.shreyass.athlete_analytics.repository.DailyMetricsRepository;
import com.shreyass.athlete_analytics.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DailyMetricsService {
    
    private final DailyMetricsRepository dailyMetricsRepository;
    private final UserRepository userRepository;

    public DailyMetrics saveMetrics(Long userId, DailyMetrics metrics){
        
        User foundUser = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
    
        if(dailyMetricsRepository.findByUser_IdAndDate(userId, metrics.getDate()).isPresent()){
            throw new DuplicateMetricException("Metrics for user with id " + userId + " on date " + metrics.getDate() + " already exists.");
        }

        metrics.setUser(foundUser);
        return dailyMetricsRepository.save(metrics);
    }

    public Page<DailyMetrics> getMetricsByUser(Long userId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
    return dailyMetricsRepository.findAllByUserId(userId, pageable);
}
    
}