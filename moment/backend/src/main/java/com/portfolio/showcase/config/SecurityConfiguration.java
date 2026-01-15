package com.portfolio.showcase.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

/**
 * Security Configuration for the API.
 * Configures security headers and permits all read-only API endpoints.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (not needed for stateless REST API)
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configure session management (stateless)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configure authorization - permit all API endpoints
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            
            // Configure security headers
            .headers(headers -> headers
                // Prevent MIME type sniffing
                .contentTypeOptions(contentTypeOptions -> {})
                // Prevent clickjacking
                .frameOptions(frameOptions -> frameOptions.deny())
                // XSS protection
                .xssProtection(xss -> 
                    xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                )
                // HTTP Strict Transport Security (for HTTPS)
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                )
            );

        return http.build();
    }
}
