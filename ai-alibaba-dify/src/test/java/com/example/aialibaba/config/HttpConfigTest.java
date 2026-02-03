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
        System.out.println("maxRetryAttempts: " + httpConfig.getMaxRetryAttempts());
        
        // 验证测试环境配置文件中的值
        assertEquals(5000, httpConfig.getConnectTimeout());
        assertEquals(60000, httpConfig.getReadTimeout());
        assertEquals(3, httpConfig.getMaxRetryAttempts());
    }
    
    @Test
    public void testHttpTimeoutMethods() {
        System.out.println("=== HttpConfig Timeout Methods ===");
        System.out.println("connectTimeoutMs: " + httpConfig.getConnectTimeoutMs());
        System.out.println("readTimeoutMs: " + httpConfig.getReadTimeoutMs());
        
        assertEquals(5000, httpConfig.getConnectTimeoutMs());
        assertEquals(60000, httpConfig.getReadTimeoutMs());
    }
}