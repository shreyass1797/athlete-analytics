package com.shreyass.athlete_analytics.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private  LocalDateTime timestamp;
    private  int status;
    private  String error;
    private  String message;
}
