package com.example.aialibaba.service.impl;

import com.example.aialibaba.exception.ServiceException;
import com.example.aialibaba.model.dto.ChatRequestDTO;
import com.example.aialibaba.model.dto.ChatResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.messages.UserMessage;
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
                .build(); // No model provider or code specified

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
}