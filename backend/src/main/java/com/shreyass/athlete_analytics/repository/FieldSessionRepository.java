package com.shreyass.athlete_analytics.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.shreyass.athlete_analytics.model.FieldSession;

public interface FieldSessionRepository extends JpaRepository<FieldSession, Long> {
    List<FieldSession> findByUserId(Long userId);
    Page<FieldSession> findAllByUserId(Long userId, Pageable pageable);
}