package com.example.aialibaba.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class HttpConfigTest {

    @Autowired
    private HttpConfig httpConfig;

    @Test
    public void testHttpPropertyMapping() {
        System.out.println("=== HttpConfig Property Values ===");
        System.out.println("connectTimeout: " + httpConfig.getConnectTimeout());
        System.out.println("readTimeout: " + httpConfig.getReadTimeout());
        System.out.println("writeTimeout: " + httpConfig.getWriteTimeout());
        System.out.println("maxRetryAttempts: " + httpConfig.getMaxRetryAttempts());
        System.out.println("connectionPoolSize: " + httpConfig.getConnectionPoolSize());
        System.out.println("connectionPoolKeepAlive: " + httpConfig.getConnectionPoolKeepAlive());
        System.out.println("retryOnConnectionFailure: " + httpConfig.isRetryOnConnectionFailure());
        
        // 验证测试环境配置文件中的值（使用默认值）
        assertEquals(5000, httpConfig.getConnectTimeout());
        assertEquals(60000, httpConfig.getReadTimeout());
        assertEquals(30000, httpConfig.getWriteTimeout());
        assertEquals(3, httpConfig.getMaxRetryAttempts());
        assertEquals(5, httpConfig.getConnectionPoolSize());
        assertEquals(5, httpConfig.getConnectionPoolKeepAlive());
        assertEquals(true, httpConfig.isRetryOnConnectionFailure());
    }
    
    @Test
    public void testHttpTimeoutMethods() {
        System.out.println("=== HttpConfig Timeout Methods ===");
        System.out.println("connectTimeoutMs: " + httpConfig.getConnectTimeout());
        System.out.println("readTimeoutMs: " + httpConfig.getReadTimeout());
        
        assertEquals(5000, httpConfig.getConnectTimeout());
        assertEquals(60000, httpConfig.getReadTimeout());
    }
}