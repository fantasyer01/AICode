package com.example.aialibaba.service.impl;

import com.example.aialibaba.exception.ServiceException;
import com.example.aialibaba.model.dto.ChatRequestDTO;
import com.example.aialibaba.model.dto.ChatResponseDTO;
import com.example.aialibaba.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(UnifiedChatServiceImpl.class);

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
        ChatService targetService = getTargetService(request);
        logger.info("🔀 Routing BLOCKING request to: {} (serviceType: {}, modelCode: {}, appCode: {})",
                targetService.getClass().getSimpleName(),
                request.getServiceType(),
                request.getModelCode(),
                request.getAppCode());
        return targetService.sendMessage(request);
    }

    @Override
    public ChatResponseDTO sendMessageWithConversation(ChatRequestDTO request) {
        return sendMessage(request);
    }

    @Override
    public SseEmitter streamMessage(ChatRequestDTO request) {
        validateRequest(request);
        ChatService targetService = getTargetService(request);
        logger.info("🔀 Routing STREAMING request to: {} (serviceType: {}, modelCode: {}, appCode: {})",
                targetService.getClass().getSimpleName(),
                request.getServiceType(),
                request.getModelCode(),
                request.getAppCode());
        return targetService.streamMessage(request);
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
        String serviceType = request.getServiceType();
        logger.debug("Determining target service for serviceType: '{}'", serviceType);
        
        if ("model".equalsIgnoreCase(serviceType)) {
            if (springAiChatService == null) {
                logger.error("Spring AI service requested but not available");
                throw new ServiceException("AI_SERVICE_NOT_AVAILABLE", 
                    "Spring AI service is not available. Please check configuration.");
            }
            logger.info("✅ Routing to Spring AI service for model: {}", request.getModelCode());
            return springAiChatService;
        }
        
        logger.info("✅ Routing to Dify service for app: {}", request.getAppCode());
        return difyChatService;
    }
}
