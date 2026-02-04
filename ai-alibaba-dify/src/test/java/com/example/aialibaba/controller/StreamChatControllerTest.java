package com.example.aialibaba.controller;

import com.example.aialibaba.model.dto.ChatRequestDTO;
import com.example.aialibaba.service.impl.DifyChatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StreamChatControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DifyChatServiceImpl difyChatService;

    private StreamChatController streamChatController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        streamChatController = new StreamChatController(difyChatService);
        mockMvc = MockMvcBuilders.standaloneSetup(streamChatController).build();
    }

    @Test
    void testStreamMessage_Success() throws Exception {
        // Given
        SseEmitter mockEmitter = new SseEmitter();
        when(difyChatService.streamMessage(any(ChatRequestDTO.class))).thenReturn(mockEmitter);

        String requestBody = """
            {
                "message": "Hello streaming world",
                "userId": "test-user-123"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/stream/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/event-stream;charset=UTF-8"));

        verify(difyChatService, times(1)).streamMessage(any(ChatRequestDTO.class));
    }

    @Test
    void testStreamMessageWithConversation_Success() throws Exception {
        // Given
        SseEmitter mockEmitter = new SseEmitter();
        when(difyChatService.streamMessageWithConversation(any(ChatRequestDTO.class))).thenReturn(mockEmitter);

        String requestBody = """
            {
                "message": "Continue our conversation",
                "userId": "test-user-123",
                "conversationId": "conv-456"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/stream/chat-with-conversation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/event-stream;charset=UTF-8"));

        verify(difyChatService, times(1)).streamMessageWithConversation(any(ChatRequestDTO.class));
    }

    @Test
    void testStreamMessage_ValidationError_EmptyMessage() throws Exception {
        String requestBody = """
            {
                "message": "",
                "userId": "test-user-123"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/stream/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(difyChatService, never()).streamMessage(any(ChatRequestDTO.class));
    }

    @Test
    void testStreamMessage_ValidationError_NullUserId() throws Exception {
        String requestBody = """
            {
                "message": "Hello",
                "userId": null
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/stream/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(difyChatService, never()).streamMessage(any(ChatRequestDTO.class));
    }

    @Test
    void testHealthCheck() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/stream/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Streaming chat service is operational"));
    }

    @Test
    void testStreamMessage_ServiceException() throws Exception {
        // Given
        when(difyChatService.streamMessage(any(ChatRequestDTO.class)))
                .thenThrow(new RuntimeException("Service unavailable"));

        String requestBody = """
            {
                "message": "Hello",
                "userId": "test-user"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/stream/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk()); // SSE emitter completes with error

        verify(difyChatService, times(1)).streamMessage(any(ChatRequestDTO.class));
    }
}