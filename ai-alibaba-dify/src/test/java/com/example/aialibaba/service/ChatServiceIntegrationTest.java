package com.example.aialibaba.service;

import com.example.aialibaba.exception.ServiceException;
import com.example.aialibaba.model.dto.ChatRequestDTO;
import com.example.aialibaba.model.dto.ChatResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ChatService
 */
@SpringBootTest
@ActiveProfiles("test")
class ChatServiceIntegrationTest {

    @Autowired
    private ChatService chatService;

    @Test
    void testSendMessage_Success() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Hello, how are you?")
                .userId("test-user-1")
                .build();
        
        // When
        ChatResponseDTO response = chatService.sendMessage(request);
        
        // Then
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertNotNull(response.getAnswer());
        assertNotNull(response.getConversationId());
    }

    @Test
    void testSendMessageWithEmptyMessage_ShouldThrowException() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("")
                .userId("test-user-1")
                .build();
        
        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            chatService.sendMessage(request);
        });
        
        assertEquals("EMPTY_MESSAGE", exception.getErrorCode());
    }

    @Test
    void testSendMessageWithNullUser_ShouldThrowException() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Hello")
                .userId(null)
                .build();
        
        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            chatService.sendMessage(request);
        });
        
        assertEquals("MISSING_USER_ID", exception.getErrorCode());
    }

    @Test
    void testValidateRequest_NullRequest_ShouldThrowException() {
        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            chatService.validateRequest(null);
        });
        
        assertEquals("INVALID_REQUEST", exception.getErrorCode());
    }
}