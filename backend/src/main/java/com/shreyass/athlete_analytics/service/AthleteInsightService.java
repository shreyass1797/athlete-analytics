package com.shreyass.athlete_analytics.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.shreyass.athlete_analytics.model.AthleteInsights;
import com.shreyass.athlete_analytics.model.User;
import com.shreyass.athlete_analytics.repository.AthleteInsightsRepository;
import com.shreyass.athlete_analytics.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AthleteInsightService {

    private final AthleteInsightsRepository athleteInsightsRepository;
    private final UserRepository userRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public AthleteInsights saveInsight(AthleteInsights insight, long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        insight.setUser(user);
        return athleteInsightsRepository.save(insight);
    }
        
    public List<AthleteInsights> getInsightsByUser(Long userId) {
        return athleteInsightsRepository.findByUserIdOrderByDateDesc(userId);
    }
}