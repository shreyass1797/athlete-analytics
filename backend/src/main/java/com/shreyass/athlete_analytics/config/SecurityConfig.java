package com.shreyass.athlete_analytics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF (we use JWTs, not cookies)
                .csrf(AbstractHttpConfigurer::disable)
                // 2. Configure endpoint access rules
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**","/v3/api-docs/**","/swagger-ui/**").permitAll()
                .anyRequest().authenticated() // Lock everything else
                )
                // 3. Make the session stateless (No server-side memory of the user)
                .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 4. Wire in our custom authentication provider (BCrypt + UserDetailsService)
                .authenticationProvider(authenticationProvider)
                // 5. Put our custom JWT Bouncer directly in front of the standard Spring filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
