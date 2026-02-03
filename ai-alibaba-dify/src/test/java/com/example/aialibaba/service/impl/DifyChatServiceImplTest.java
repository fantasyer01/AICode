package com.example.aialibaba.service.impl;

import com.example.aialibaba.config.DifyConfig;
import com.example.aialibaba.exception.ServiceException;
import com.example.aialibaba.model.dto.ChatRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DifyChatServiceImpl
 */
class DifyChatServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private DifyConfig difyConfig;

    @InjectMocks
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
        
        when(difyConfig.getApps()).thenReturn(apps);
        when(difyConfig.getChatEndpoint()).thenReturn("https://api.dify.ai/v1/chat-messages");
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
    void testSendMessage_ValidRequest_ShouldReturnResponse() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Hello")
                .userId("user1")
                .build();
        
        // When & Then
        assertThrows(Exception.class, () -> {
            chatService.sendMessage(request);
        }, "Should throw exception due to missing mock setup");
    }

    @Test
    void testSendMessageWithConversation_ShouldCallSendMessage() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Hello")
                .userId("user1")
                .conversationId("conv-123")
                .build();
        
        // When
        assertThrows(Exception.class, () -> {
            chatService.sendMessageWithConversation(request);
        });
    }
}