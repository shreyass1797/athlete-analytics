package com.shreyass.athlete_analytics.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
             HttpServletRequest request,
             HttpServletResponse response,
             FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Check if the VIP pass is missing or malformed
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Pass it to the next filter (which will likely reject it)
            return;
        }

        // 2. Extract the token and the email
        jwt = authHeader.substring(7); // Removes the "Bearer " prefix
        userEmail = jwtService.extractUsername(jwt); // returns the email embedded in the token

        // 3. If we have an email and the user isn't already authenticated in this session
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Fetch the user from the database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 4. Validate the token mathematically
            if (jwtService.isTokenValid(jwt, userDetails)) {
                
                // 5. Create the official Spring Security authentication token
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                // 6. Open the gate and update the Security Context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        // Let the request continue to the Controller
        filterChain.doFilter(request, response);
    }
}