package com.example.aialibaba.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class DifyConfigTest {

    @Autowired
    private DifyConfig difyConfig;

    @Test
    public void testPropertyMapping() {
        System.out.println("=== DifyConfig Property Values ===");
        System.out.println("api.baseUrl: " + difyConfig.getApi().getBaseUrl());
        System.out.println("api.temperature: " + difyConfig.getApi().getTemperature());
        System.out.println("api.maxTokens: " + difyConfig.getApi().getMaxTokens());
        
        // 验证配置文件中的值
        assertEquals("https://api.dify.ai/v1", difyConfig.getApi().getBaseUrl());
        assertEquals(0.7, difyConfig.getApi().getTemperature());
        assertEquals(1000, difyConfig.getApi().getMaxTokens());
    }
}