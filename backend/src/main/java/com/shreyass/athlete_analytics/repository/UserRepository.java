package com.shreyass.athlete_analytics.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shreyass.athlete_analytics.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByDisplayName(String displayName);
}