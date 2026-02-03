package com.example.aialibaba.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Configuration class for Dify integration
 */
@Configuration
@ConfigurationProperties(prefix = "dify")
@Data
public class DifyConfig {
    
    private ApiConfig api = new ApiConfig();
    private Map<String, AppConfig> apps;
    
    @Data
    public static class ApiConfig {
        private String baseUrl = "https://api.dify.ai/v1";
        private Double temperature = 0.7;
        private Integer maxTokens = 1000;
    }

    @Data
    public static class AppConfig {
        private String apiKey;
        private String appId;
    }

    /**
     * Get the complete chat endpoint URL
     */
    public String getChatEndpoint() {
        return api.getBaseUrl() + "/chat-messages";
    }
    
    /**
     * Get the complete completion endpoint URL
     */
    public String getCompletionEndpoint() {
        return api.getBaseUrl() + "/completion-messages";
    }
}