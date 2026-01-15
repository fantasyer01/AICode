package com.portfolio.showcase.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS Configuration for allowing frontend applications to access the API.
 */
@Configuration
public class CorsConfiguration implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:3001}")
    private String[] allowedOrigins;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("Cache-Control", "Content-Type")
                .allowCredentials(false)
                .maxAge(maxAge);
    }
}
