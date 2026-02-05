package com.example.aialibaba.service.impl;

import com.example.aialibaba.config.DifyConfig;
import com.example.aialibaba.exception.ServiceException;
import com.example.aialibaba.model.dto.ChatRequestDTO;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DifyChatServiceImplStreamingTest {

    @Mock
    private OkHttpClient okHttpClient;

    private DifyConfig difyConfig;
    private DifyChatServiceImpl difyChatService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup DifyConfig
        difyConfig = new DifyConfig();
        DifyConfig.ApiConfig apiConfig = new DifyConfig.ApiConfig();
        apiConfig.setResponseMode("streaming");
        apiConfig.setTemperature(0.7);
        apiConfig.setMaxTokens(1000);
        difyConfig.setApi(apiConfig);
        
        Map<String, DifyConfig.AppConfig> apps = new HashMap<>();
        DifyConfig.AppConfig appConfig = new DifyConfig.AppConfig();
        appConfig.setApiKey("test-api-key");
        appConfig.setAppId("test-app-id");
        apps.put("default", appConfig);
        difyConfig.setApps(apps);
        
        difyChatService = new DifyChatServiceImpl(okHttpClient, difyConfig);
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
    }

    @Test
    void testStreamMessage_MissingAppConfig_ShouldThrowException() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Hello")
                .userId("test-user")
                .appCode("non-existent")
                .build();

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            difyChatService.streamMessage(request);
        });
        
        assertEquals("APP_CONFIG_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void testValidateRequest_WithNullRequest() {
        // When & Then
        assertThrows(ServiceException.class, () -> {
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
        assertThrows(ServiceException.class, () -> {
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
        assertThrows(ServiceException.class, () -> {
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

    @Test
    void testBlockingMode_ShouldUseBlockingHandler() {
        // Given - Change config to blocking mode
        difyConfig.getApi().setResponseMode("blocking");
        
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Hello")
                .userId("test-user")
                .build();

        // When - sendMessage uses blocking mode internally
        // Since we don't have a real OkHttpClient response, we expect an exception
        assertThrows(Exception.class, () -> {
            difyChatService.sendMessage(request);
        });
    }
}
