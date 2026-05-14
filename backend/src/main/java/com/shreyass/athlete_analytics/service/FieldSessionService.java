package com.shreyass.athlete_analytics.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.shreyass.athlete_analytics.model.FieldSession;
import com.shreyass.athlete_analytics.model.User;
import com.shreyass.athlete_analytics.repository.FieldSessionRepository;
import com.shreyass.athlete_analytics.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FieldSessionService {
    
    private final FieldSessionRepository fieldSessionRepository;
    private final UserRepository userRepository; 

    public FieldSession saveSession(Long userId, FieldSession fieldSession){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        fieldSession.setUser(user);
        return fieldSessionRepository.save(fieldSession);
    }

    public Page<FieldSession> getSessionsByUser(Long userId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
    return fieldSessionRepository.findAllByUserId(userId, pageable);
}
}
