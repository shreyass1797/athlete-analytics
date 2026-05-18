package com.shreyass.athlete_analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AthleteAnalyticsApplication {

	public static void main(String[] args) {
		SpringApplication.run(AthleteAnalyticsApplication.class, args);
	}

}
