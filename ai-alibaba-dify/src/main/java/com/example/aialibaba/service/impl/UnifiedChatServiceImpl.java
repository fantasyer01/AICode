package com.example.aialibaba.service.impl;

import com.example.aialibaba.exception.ServiceException;
import com.example.aialibaba.model.dto.ChatRequestDTO;
import com.example.aialibaba.model.dto.ChatResponseDTO;
import com.example.aialibaba.service.AiModelService;
import com.example.aialibaba.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Unified chat service that orchestrates calls between Dify and Spring AI
 */
@Service
@Primary
public class UnifiedChatServiceImpl implements ChatService {

    private final ChatService difyChatService; 
    private final AiModelService springAiUnifiedService;

    public UnifiedChatServiceImpl(@Qualifier("difyChatServiceImpl") ChatService difyChatService, 
                                  @Autowired(required = false) @Qualifier("springAiUnifiedService") AiModelService springAiUnifiedService) {
        this.difyChatService = difyChatService;
        this.springAiUnifiedService = springAiUnifiedService;
    }

    @Override
    public ChatResponseDTO sendMessage(ChatRequestDTO request) {
        validateRequest(request);
        
        // Route based on service type and model provider
        if ("spring-ai".equalsIgnoreCase(request.getServiceType())) {
            // Use the new unified Spring AI service if available
            if (springAiUnifiedService != null) {
                return springAiUnifiedService.sendMessage(request);
            } else {
                throw new ServiceException("AI_SERVICE_NOT_AVAILABLE", "Spring AI service is not available.");
            }
        } else {
            // Default to Dify service
            return difyChatService.sendMessage(request);
        }
    }

    @Override
    public ChatResponseDTO sendMessageWithConversation(ChatRequestDTO request) {
        return sendMessage(request);
    }

    @Override
    public void validateRequest(ChatRequestDTO request) {
        difyChatService.validateRequest(request);
    }
}
