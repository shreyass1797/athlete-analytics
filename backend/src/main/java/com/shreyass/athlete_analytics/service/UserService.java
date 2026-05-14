package com.shreyass.athlete_analytics.service;

import org.springframework.stereotype.Service;

import com.shreyass.athlete_analytics.exception.DuplicateUserException;
import com.shreyass.athlete_analytics.model.User;
import com.shreyass.athlete_analytics.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;

    public User registerUser(User user) {
        if(userRepository.findByEmail(user.getEmail()).isEmpty()) {
            return userRepository.save(user);
        }
        else {
            throw new DuplicateUserException("Email already exists: " + user.getEmail());
        }
    }
}
