package com.example.aialibaba.config;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Web configuration for HTTP clients
 */
@Configuration
public class RestWebConfig {
    
    /**
     * Configure OkHttpClient with timeout settings from HttpConfig
     */
    @Bean
    public OkHttpClient okHttpClient(HttpConfig httpConfig) {
        return new OkHttpClient.Builder()
            .connectTimeout(httpConfig.getConnectTimeout(), TimeUnit.MILLISECONDS)
            .readTimeout(httpConfig.getReadTimeout(), TimeUnit.MILLISECONDS)
            .writeTimeout(httpConfig.getWriteTimeout(), TimeUnit.MILLISECONDS)
            .connectionPool(new ConnectionPool(
                httpConfig.getConnectionPoolSize(), 
                httpConfig.getConnectionPoolKeepAlive(), 
                TimeUnit.MINUTES))
            .retryOnConnectionFailure(httpConfig.isRetryOnConnectionFailure())
            .build();
    }
}
