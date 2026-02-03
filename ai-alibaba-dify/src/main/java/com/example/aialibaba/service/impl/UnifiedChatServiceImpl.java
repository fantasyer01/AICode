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
    private final AiModelService dashScopeAiService;
    private final AiModelService deepSeekAiService;

    public UnifiedChatServiceImpl(@Qualifier("difyChatServiceImpl") ChatService difyChatService, 
                                  @Autowired(required = false) @Qualifier("springAiUnifiedService") AiModelService springAiUnifiedService,
                                  @Autowired(required = false) @Qualifier("dashScopeAiServiceImpl") AiModelService dashScopeAiService,
                                  @Autowired(required = false) @Qualifier("deepSeekAiServiceImpl") AiModelService deepSeekAiService) {
        this.difyChatService = difyChatService;
        this.springAiUnifiedService = springAiUnifiedService;
        this.dashScopeAiService = dashScopeAiService;
        this.deepSeekAiService = deepSeekAiService;
    }

    @Override
    public ChatResponseDTO sendMessage(ChatRequestDTO request) {
        validateRequest(request);
        
        // Route based on service type and model provider
        if ("spring-ai".equalsIgnoreCase(request.getServiceType())) {
            // Use the new unified Spring AI service if available
            if (springAiUnifiedService != null) {
                return springAiUnifiedService.sendMessage(request);
            }
            
            // Fallback to individual services for backward compatibility
            String provider = request.getModelProvider();
            
            if ("deepseek".equalsIgnoreCase(provider)) {
                if (deepSeekAiService == null) {
                    throw new ServiceException("AI_SERVICE_NOT_AVAILABLE", "DeepSeek AI service is not available. Please configure the API key.");
                }
                return deepSeekAiService.sendMessage(request);
            } else {
                // Default to DashScope for other providers or when not specified
                if (dashScopeAiService == null) {
                    throw new ServiceException("AI_SERVICE_NOT_AVAILABLE", "DashScope AI service is not available. Please configure the API key.");
                }
                return dashScopeAiService.sendMessage(request);
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
