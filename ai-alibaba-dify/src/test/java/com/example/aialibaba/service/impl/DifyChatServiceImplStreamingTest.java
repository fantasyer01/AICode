package com.example.aialibaba.service.impl;

import com.example.aialibaba.config.DifyConfig;
import com.example.aialibaba.model.dto.ChatRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DifyChatServiceImplStreamingTest {

    @Mock
    private RestTemplate restTemplate;

    private DifyConfig difyConfig;
    private DifyChatServiceImpl difyChatService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup DifyConfig
        difyConfig = new DifyConfig();
        DifyConfig.ApiConfig apiConfig = new DifyConfig.ApiConfig();
        apiConfig.setResponseMode("streaming");  // Set to streaming for this test
        difyConfig.setApi(apiConfig);
        
        Map<String, DifyConfig.AppConfig> apps = new HashMap<>();
        DifyConfig.AppConfig appConfig = new DifyConfig.AppConfig();
        appConfig.setApiKey("test-api-key");
        appConfig.setAppId("test-app-id");
        apps.put("default", appConfig);
        difyConfig.setApps(apps);
        
        difyChatService = new DifyChatServiceImpl(restTemplate, difyConfig);
    }

    @Test
    void testStreamMessage_Success() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Hello")
                .userId("test-user")
                .build();

        // When
        SseEmitter emitter = difyChatService.streamMessage(request);

        // Then
        assertNotNull(emitter);
        // Timeout should be around 60000L (default) or derived from system property
    }

    @Test
    void testStreamMessageWithConversation_Success() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Hello")
                .userId("test-user")
                .conversationId("test-conv")
                .build();

        // When
        SseEmitter emitter = difyChatService.streamMessageWithConversation(request);

        // Then
        assertNotNull(emitter);
        // Timeout should be around 60000L (default) or derived from system property
    }

    @Test
    void testBuildDifyRequestBody_WithStreamingMode() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Test message")
                .userId("test-user")
                .build();

        DifyConfig.AppConfig appConfig = new DifyConfig.AppConfig();
        appConfig.setApiKey("test-key");
        appConfig.setAppId("test-app");

        // When - Use reflection to test private method
        try {
            java.lang.reflect.Method method = DifyChatServiceImpl.class.getDeclaredMethod(
                    "buildDifyRequestBody", ChatRequestDTO.class, DifyConfig.AppConfig.class);
            method.setAccessible(true);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) method.invoke(difyChatService, request, appConfig);

            // Then
            assertEquals("Test message", result.get("query"));
            assertEquals("streaming", result.get("response_mode"));
            assertEquals("test-user", result.get("user"));
            assertNotNull(result.get("inputs"));
            assertEquals(0.7, result.get("temperature"));
            assertEquals(1000, result.get("max_tokens"));

        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    void testBuildDifyRequestBody_WithBlockingMode() {
        // Given - Change config to blocking mode for this test
        difyConfig.getApi().setResponseMode("blocking");
        
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Test message")
                .userId("test-user")
                .build();

        DifyConfig.AppConfig appConfig = new DifyConfig.AppConfig();
        appConfig.setApiKey("test-key");
        appConfig.setAppId("test-app");

        // When - Use reflection to test private method
        try {
            java.lang.reflect.Method method = DifyChatServiceImpl.class.getDeclaredMethod(
                    "buildDifyRequestBody", ChatRequestDTO.class, DifyConfig.AppConfig.class);
            method.setAccessible(true);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) method.invoke(difyChatService, request, appConfig);

            // Then
            assertEquals("Test message", result.get("query"));
            assertEquals("blocking", result.get("response_mode"));
            assertEquals("test-user", result.get("user"));

        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    void testValidateRequest_WithNullRequest() {
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            difyChatService.validateRequest(null);
        });
    }

    @Test
    void testValidateRequest_WithEmptyMessage() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("")
                .userId("test-user")
                .build();

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            difyChatService.validateRequest(request);
        });
    }

    @Test
    void testValidateRequest_WithNullUserId() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Hello")
                .userId(null)
                .build();

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            difyChatService.validateRequest(request);
        });
    }

    @Test
    void testValidateRequest_ValidRequest() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Hello")
                .userId("test-user")
                .build();

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            difyChatService.validateRequest(request);
        });
    }
}