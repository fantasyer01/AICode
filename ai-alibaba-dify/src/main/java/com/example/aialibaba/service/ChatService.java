package com.example.aialibaba.service;

import com.example.aialibaba.model.dto.ChatRequestDTO;
import com.example.aialibaba.model.dto.ChatResponseDTO;

/**
 * Chat service interface for handling chat interactions
 */
public interface ChatService {
    
    /**
     * Send message to Dify and get response
     * 
     * @param request Chat request containing message and user information
     * @return Chat response with answer from Dify
     * @throws com.example.aialibaba.exception.ServiceException if service error occurs
     */
    ChatResponseDTO sendMessage(ChatRequestDTO request);
    
    /**
     * Send message to Dify with conversation context
     * 
     * @param request Chat request containing message, user info, and conversation ID
     * @return Chat response with answer and updated conversation context
     * @throws com.example.aialibaba.exception.ServiceException if service error occurs
     */
    ChatResponseDTO sendMessageWithConversation(ChatRequestDTO request);
    
    /**
     * Validate chat request parameters
     * 
     * @param request Chat request to validate
     * @throws com.example.aialibaba.exception.ServiceException if validation fails
     */
    void validateRequest(ChatRequestDTO request);
}