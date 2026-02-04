package com.example.aialibaba.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Web configuration for REST template and HTTP clients
 */
@Configuration
public class RestWebConfig {
    
    /**
     * Configure RestTemplate with timeout settings from HttpConfig
     */
    @Bean
    public RestTemplate restTemplate(HttpConfig httpConfig) {
        RestTemplate restTemplate = new RestTemplate();
        
        // Configure timeouts
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(httpConfig.getConnectTimeout()));
        factory.setReadTimeout(Duration.ofMillis(httpConfig.getReadTimeout()));
        
        restTemplate.setRequestFactory(factory);
        return restTemplate;
    }
}