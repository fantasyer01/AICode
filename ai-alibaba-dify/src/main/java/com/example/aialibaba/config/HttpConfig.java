package com.example.aialibaba.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for HTTP client settings
 * Shared by Dify and AI Model services
 */
@Configuration
@ConfigurationProperties(prefix = "spring.http")
@Data
public class HttpConfig {
    
    private int connectTimeout = 5000;
    private int readTimeout = 60000;
    private int maxRetryAttempts = 3;
    
    /**
     * Get connect timeout in milliseconds
     */
    public int getConnectTimeoutMs() {
        return connectTimeout;
    }
    
    /**
     * Get read timeout in milliseconds
     */
    public int getReadTimeoutMs() {
        return readTimeout;
    }
}