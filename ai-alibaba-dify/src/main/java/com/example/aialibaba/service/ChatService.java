package com.example.aialibaba.service;

import com.example.aialibaba.model.dto.ChatRequestDTO;
import com.example.aialibaba.model.dto.ChatResponseDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Chat service interface for handling chat interactions
 * Supports both blocking and streaming response modes
 */
public interface ChatService {
    
    /**
     * Send message and get response (blocking mode)
     * 
     * @param request Chat request containing message and user information
     * @return Chat response with answer
     * @throws com.example.aialibaba.exception.ServiceException if service error occurs
     */
    ChatResponseDTO sendMessage(ChatRequestDTO request);
    
    /**
     * Send message with conversation context (blocking mode)
     * 
     * @param request Chat request containing message, user info, and conversation ID
     * @return Chat response with answer and updated conversation context
     * @throws com.example.aialibaba.exception.ServiceException if service error occurs
     */
    ChatResponseDTO sendMessageWithConversation(ChatRequestDTO request);
    
    /**
     * Stream message response using Server-Sent Events
     * 
     * @param request Chat request containing message and user information
     * @return SseEmitter for streaming response
     * @throws com.example.aialibaba.exception.ServiceException if service error occurs
     */
    SseEmitter streamMessage(ChatRequestDTO request);
    
    /**
     * Stream message with conversation context using Server-Sent Events
     * 
     * @param request Chat request containing message, user info, and conversation ID
     * @return SseEmitter for streaming response
     * @throws com.example.aialibaba.exception.ServiceException if service error occurs
     */
    SseEmitter streamMessageWithConversation(ChatRequestDTO request);
    
    /**
     * Validate chat request parameters
     * 
     * @param request Chat request to validate
     * @throws com.example.aialibaba.exception.ServiceException if validation fails
     */
    void validateRequest(ChatRequestDTO request);
}
