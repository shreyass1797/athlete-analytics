package com.shreyass.athlete_analytics.exception;

public class DuplicateMetricException extends RuntimeException {
    public DuplicateMetricException(String message) {
        super(message);
    }
}