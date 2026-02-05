package com.example.aialibaba.service.impl;

import com.example.aialibaba.exception.ServiceException;
import com.example.aialibaba.model.dto.ChatRequestDTO;
import com.example.aialibaba.model.dto.ChatResponseDTO;
import com.example.aialibaba.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Unified chat service that orchestrates calls between Dify and Spring AI
 * Routes requests based on serviceType parameter
 */
@Service
@Primary
public class UnifiedChatServiceImpl implements ChatService {

    private final ChatService difyChatService;
    private final ChatService springAiChatService;

    public UnifiedChatServiceImpl(
            @Qualifier("difyChatServiceImpl") ChatService difyChatService,
            @Autowired(required = false) @Qualifier("springAiChatService") ChatService springAiChatService) {
        this.difyChatService = difyChatService;
        this.springAiChatService = springAiChatService;
    }

    @Override
    public ChatResponseDTO sendMessage(ChatRequestDTO request) {
        validateRequest(request);
        return getTargetService(request).sendMessage(request);
    }

    @Override
    public ChatResponseDTO sendMessageWithConversation(ChatRequestDTO request) {
        return sendMessage(request);
    }

    @Override
    public SseEmitter streamMessage(ChatRequestDTO request) {
        validateRequest(request);
        return getTargetService(request).streamMessage(request);
    }

    @Override
    public SseEmitter streamMessageWithConversation(ChatRequestDTO request) {
        return streamMessage(request);
    }

    @Override
    public void validateRequest(ChatRequestDTO request) {
        difyChatService.validateRequest(request);
    }

    /**
     * Route to target service based on serviceType
     */
    private ChatService getTargetService(ChatRequestDTO request) {
        if ("model".equalsIgnoreCase(request.getServiceType())) {
            if (springAiChatService == null) {
                throw new ServiceException("AI_SERVICE_NOT_AVAILABLE", 
                    "Spring AI service is not available");
            }
            return springAiChatService;
        }
        return difyChatService;
    }
}
