package com.example.aialibaba.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.example.aialibaba.exception.ServiceException;
import com.example.aialibaba.model.dto.ChatRequestDTO;
import com.example.aialibaba.model.dto.ChatResponseDTO;
import com.example.aialibaba.service.AiModelService;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Unified implementation of AiModelService using Spring AI Alibaba
 * Supports multiple AI models through dynamic model switching
 */
@Service("springAiUnifiedService")
public class SpringAiUnifiedServiceImpl implements AiModelService {

    private final ChatModel chatModel;
    
    // Model mapping configuration
    @Value("${ai-models.dashscope.temperature:0.7}")
    private Double dashscopeTemperature;
    
    @Value("${ai-models.dashscope.max-tokens:1000}")
    private Integer dashscopeMaxTokens;
    
    @Value("${ai-models.deepseek.temperature:0.7}")
    private Double deepseekTemperature;
    
    @Value("${ai-models.deepseek.max-tokens:1000}")
    private Integer deepseekMaxTokens;
    
    // Model code to provider mapping
    private static final Map<String, String> MODEL_PROVIDER_MAP = new HashMap<>();
    
    static {
        // DashScope models
        MODEL_PROVIDER_MAP.put("qwen-turbo", "dashscope");
        MODEL_PROVIDER_MAP.put("qwen-plus", "dashscope");
        MODEL_PROVIDER_MAP.put("qwen-max", "dashscope");
        MODEL_PROVIDER_MAP.put("qwen-long", "dashscope");
        
        // DeepSeek models  
        MODEL_PROVIDER_MAP.put("deepseek-chat", "deepseek");
        MODEL_PROVIDER_MAP.put("deepseek-coder", "deepseek");
    }

    public SpringAiUnifiedServiceImpl(@Autowired(required = false) ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public ChatResponseDTO sendMessage(ChatRequestDTO request) {
        if (chatModel == null) {
            throw new ServiceException("AI_SERVICE_NOT_CONFIGURED", 
                "Spring AI service is not configured. Please check your configuration.");
        }
        
        try {
            // Determine the model to use
            String modelCode = getModelCode(request);
            
            // Build prompt with dynamic options
            UserMessage userMessage = new UserMessage(request.getMessage());
            Prompt prompt = new Prompt(userMessage, buildChatOptions(modelCode));
            
            // Call the model
            ChatResponse aiResponse = chatModel.call(prompt);
            
            // Convert to response DTO
            return convertToChatResponseDTO(aiResponse, request.getUserId());
            
        } catch (Exception e) {
            throw new ServiceException("SPRING_AI_ERROR", "Failed to communicate with AI service", e);
        }
    }
    
    /**
     * Get model code from request or determine based on provider
     */
    private String getModelCode(ChatRequestDTO request) {
        if (request.getModelCode() != null && !request.getModelCode().isEmpty()) {
            return request.getModelCode();
        }
        
        // Fallback logic based on provider
        String provider = request.getModelProvider();
        if ("deepseek".equalsIgnoreCase(provider)) {
            return "deepseek-chat";
        } else {
            return "qwen-plus"; // Default to DashScope
        }
    }
    
    /**
     * Build chat options based on model
     */
    private DashScopeChatOptions buildChatOptions(String modelCode) {
        String provider = MODEL_PROVIDER_MAP.getOrDefault(modelCode, "dashscope");
        
        // For now, we'll use DashScopeChatOptions for all models since we're using DashScope ChatModel
        // In the future, we could extend this to support other model providers
        return DashScopeChatOptions.builder()
            .withModel(modelCode)
            .withTemperature(getTemperatureForProvider(provider))
            .build();
    }
    
    /**
     * Get temperature setting for provider
     */
    private Double getTemperatureForProvider(String provider) {
        if ("deepseek".equals(provider)) {
            return deepseekTemperature;
        } else {
            return dashscopeTemperature;
        }
    }
    
    /**
     * Get max tokens setting for provider
     */
    private Integer getMaxTokensForProvider(String provider) {
        if ("deepseek".equals(provider)) {
            return deepseekMaxTokens;
        } else {
            return dashscopeMaxTokens;
        }
    }
    
    /**
     * Convert Spring AI ChatResponse to ChatResponseDTO
     */
    private ChatResponseDTO convertToChatResponseDTO(ChatResponse aiResponse, String userId) {
        ChatResponseDTO response = new ChatResponseDTO();
        response.setMessageId(UUID.randomUUID().toString());
        response.setCreatedAt(System.currentTimeMillis());
        response.setStatus("success");
        response.setAnswer(aiResponse.getResult().getOutput().getText());
        
        // Extract usage information if available
        if (aiResponse.getMetadata() != null && aiResponse.getMetadata().getUsage() != null) {
            ChatResponseDTO.Usage usage = new ChatResponseDTO.Usage();
            var usageMetadata = aiResponse.getMetadata().getUsage();
            usage.setPromptTokens(usageMetadata.getPromptTokens().intValue());
            usage.setCompletionTokens(usageMetadata.getCompletionTokens().intValue());
            usage.setTotalTokens(usageMetadata.getTotalTokens().intValue());
            response.setUsage(usage);
        }
        
        return response;
    }
}