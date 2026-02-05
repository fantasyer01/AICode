package com.example.aialibaba.service.impl;

import com.example.aialibaba.config.DifyConfig;
import com.example.aialibaba.exception.ServiceException;
import com.example.aialibaba.model.dto.ChatRequestDTO;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DifyChatServiceImpl
 */
class DifyChatServiceImplTest {

    @Mock
    private OkHttpClient okHttpClient;

    @Mock
    private DifyConfig difyConfig;

    private DifyChatServiceImpl chatService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Mock DifyConfig structure
        Map<String, DifyConfig.AppConfig> apps = new HashMap<>();
        DifyConfig.AppConfig appConfig = new DifyConfig.AppConfig();
        appConfig.setApiKey("test-api-key");
        appConfig.setAppId("test-app-id");
        apps.put("default", appConfig);
        
        DifyConfig.ApiConfig apiConfig = new DifyConfig.ApiConfig();
        apiConfig.setResponseMode("blocking");
        apiConfig.setTemperature(0.7);
        apiConfig.setMaxTokens(1000);
        
        when(difyConfig.getApps()).thenReturn(apps);
        when(difyConfig.getApi()).thenReturn(apiConfig);
        when(difyConfig.getChatEndpoint()).thenReturn("https://api.dify.ai/v1/chat-messages");
        
        chatService = new DifyChatServiceImpl(okHttpClient, difyConfig);
    }

    @Test
    void testValidateRequest_ValidRequest_ShouldPass() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Hello")
                .userId("user1")
                .build();
        
        // When & Then
        assertDoesNotThrow(() -> chatService.validateRequest(request));
    }

    @Test
    void testValidateRequest_NullRequest_ShouldThrowException() {
        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            chatService.validateRequest(null);
        });
        
        assertEquals("INVALID_REQUEST", exception.getErrorCode());
    }

    @Test
    void testValidateRequest_EmptyMessage_ShouldThrowException() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("")
                .userId("user1")
                .build();
        
        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            chatService.validateRequest(request);
        });
        
        assertEquals("EMPTY_MESSAGE", exception.getErrorCode());
    }

    @Test
    void testValidateRequest_NullUser_ShouldThrowException() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Hello")
                .userId(null)
                .build();
        
        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            chatService.validateRequest(request);
        });
        
        assertEquals("MISSING_USER_ID", exception.getErrorCode());
    }

    @Test
    void testSendMessage_MissingAppConfig_ShouldThrowException() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Hello")
                .userId("user1")
                .appCode("non-existent")
                .build();
        
        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            chatService.sendMessage(request);
        });
        
        assertEquals("APP_CONFIG_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void testSendMessageWithConversation_ShouldDelegateToSendMessage() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Hello")
                .userId("user1")
                .conversationId("conv-123")
                .appCode("non-existent")
                .build();
        
        // When & Then - Should throw same exception as sendMessage
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            chatService.sendMessageWithConversation(request);
        });
        
        assertEquals("APP_CONFIG_NOT_FOUND", exception.getErrorCode());
    }
}
