package com.example.aialibaba.service.impl;

import com.example.aialibaba.exception.ServiceException;
import com.example.aialibaba.model.dto.ChatRequestDTO;
import com.example.aialibaba.model.dto.ChatResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SpringAiUnifiedServiceImplTest {

    @Mock
    private ChatModel chatModel;

    private SpringAiUnifiedServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new SpringAiUnifiedServiceImpl(chatModel);
    }

    @Test
    void testSendMessage_Success() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Hello")
                .serviceType("spring-ai")
                .modelProvider("dashscope")
                .modelCode("qwen-plus")
                .userId("test-user")
                .build();

        ChatResponse mockResponse = mock(ChatResponse.class);
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);
        
        var result = mock(org.springframework.ai.chat.model.Generation.class);
        when(mockResponse.getResult()).thenReturn(result);
        
        var output = mock(org.springframework.ai.chat.messages.AssistantMessage.class);
        when(result.getOutput()).thenReturn(output);
        when(output.getText()).thenReturn("Hello! How can I help you today?");

        // When
        ChatResponseDTO response = service.sendMessage(request);

        // Then
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals("Hello! How can I help you today?", response.getAnswer());
        assertNotNull(response.getMessageId());
        assertTrue(response.getCreatedAt() > 0);
        
        verify(chatModel).call(any(Prompt.class));
    }

    @Test
    void testSendMessage_NullChatModel_ThrowsException() {
        // Given
        service = new SpringAiUnifiedServiceImpl(null);
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Hello")
                .userId("test-user")
                .build();

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            service.sendMessage(request);
        });
        
        assertEquals("AI_SERVICE_NOT_CONFIGURED", exception.getErrorCode());
    }

    @Test
    void testSendMessage_WithDeepSeekModel() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Hello")
                .serviceType("spring-ai")
                .modelProvider("deepseek")
                .modelCode("deepseek-chat")
                .userId("test-user")
                .build();

        ChatResponse mockResponse = mock(ChatResponse.class);
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);
        
        var result = mock(org.springframework.ai.chat.model.Generation.class);
        when(mockResponse.getResult()).thenReturn(result);
        
        var output = mock(org.springframework.ai.chat.messages.AssistantMessage.class);
        when(result.getOutput()).thenReturn(output);
        when(output.getText()).thenReturn("Hello from DeepSeek!");

        // When
        ChatResponseDTO response = service.sendMessage(request);

        // Then
        assertNotNull(response);
        assertEquals("Hello from DeepSeek!", response.getAnswer());
        verify(chatModel).call(any(Prompt.class));
    }

    @Test
    void testSendMessage_DefaultModelFallback() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Hello")
                .serviceType("spring-ai")
                .userId("test-user")
                .build();

        ChatResponse mockResponse = mock(ChatResponse.class);
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);
        
        var result = mock(org.springframework.ai.chat.model.Generation.class);
        when(mockResponse.getResult()).thenReturn(result);
        
        var output = mock(org.springframework.ai.chat.messages.AssistantMessage.class);
        when(result.getOutput()).thenReturn(output);
        when(output.getText()).thenReturn("Hello from default model!");

        // When
        ChatResponseDTO response = service.sendMessage(request);

        // Then
        assertNotNull(response);
        assertEquals("Hello from default model!", response.getAnswer());
        verify(chatModel).call(any(Prompt.class));
    }

    @Test
    void testSendMessageWithConversation_DelegatesToSendMessage() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Hello")
                .userId("test-user")
                .conversationId("conv-123")
                .build();

        ChatResponse mockResponse = mock(ChatResponse.class);
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);
        
        var result = mock(org.springframework.ai.chat.model.Generation.class);
        when(mockResponse.getResult()).thenReturn(result);
        
        var output = mock(org.springframework.ai.chat.messages.AssistantMessage.class);
        when(result.getOutput()).thenReturn(output);
        when(output.getText()).thenReturn("Response");

        // When
        ChatResponseDTO response = service.sendMessageWithConversation(request);

        // Then
        assertNotNull(response);
        verify(chatModel).call(any(Prompt.class));
    }

    @Test
    void testStreamMessage_ThrowsNotSupportedException() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Hello")
                .userId("test-user")
                .build();

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            service.streamMessage(request);
        });
        
        assertEquals("STREAMING_NOT_SUPPORTED", exception.getErrorCode());
    }

    @Test
    void testStreamMessageWithConversation_ThrowsNotSupportedException() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Hello")
                .userId("test-user")
                .conversationId("conv-123")
                .build();

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            service.streamMessageWithConversation(request);
        });
        
        assertEquals("STREAMING_NOT_SUPPORTED", exception.getErrorCode());
    }

    @Test
    void testValidateRequest_NullRequest_ThrowsException() {
        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            service.validateRequest(null);
        });
        
        assertEquals("INVALID_REQUEST", exception.getErrorCode());
    }

    @Test
    void testValidateRequest_EmptyMessage_ThrowsException() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("")
                .userId("test-user")
                .build();

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            service.validateRequest(request);
        });
        
        assertEquals("EMPTY_MESSAGE", exception.getErrorCode());
    }

    @Test
    void testValidateRequest_NullUserId_ThrowsException() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Hello")
                .userId(null)
                .build();

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            service.validateRequest(request);
        });
        
        assertEquals("MISSING_USER_ID", exception.getErrorCode());
    }

    @Test
    void testValidateRequest_ValidRequest_Passes() {
        // Given
        ChatRequestDTO request = ChatRequestDTO.builder()
                .message("Hello")
                .userId("test-user")
                .build();

        // When & Then
        assertDoesNotThrow(() -> {
            service.validateRequest(request);
        });
    }
}
