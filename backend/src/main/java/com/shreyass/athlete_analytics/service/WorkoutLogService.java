package com.shreyass.athlete_analytics.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.shreyass.athlete_analytics.model.User;
import com.shreyass.athlete_analytics.model.WorkoutLog;
import com.shreyass.athlete_analytics.repository.UserRepository;
import com.shreyass.athlete_analytics.repository.WorkoutLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkoutLogService {
    
    private final WorkoutLogRepository workoutLogRepository;
    private final UserRepository userRepository;
    
    public WorkoutLog saveWorkout(Long userId, WorkoutLog workoutLog) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        workoutLog.setUser(user);
        return workoutLogRepository.save(workoutLog);
    }

    public Page<WorkoutLog> getWorkoutsByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        return workoutLogRepository.findAllByUserId(userId, pageable);
    }
}
