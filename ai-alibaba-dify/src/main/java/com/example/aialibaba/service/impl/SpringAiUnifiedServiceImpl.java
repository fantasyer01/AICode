package com.example.aialibaba.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.example.aialibaba.exception.ServiceException;
import com.example.aialibaba.model.dto.ChatRequestDTO;
import com.example.aialibaba.model.dto.ChatResponseDTO;
import com.example.aialibaba.service.ChatService;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Unified implementation of ChatService using Spring AI Alibaba
 * Supports multiple AI models through dynamic model switching
 * Note: Streaming is not supported for direct AI model access
 */
@Service("springAiUnifiedService")
public class SpringAiUnifiedServiceImpl implements ChatService {

    private final ChatModel chatModel;
    
    @Value("${ai-models.api.temperature:0.7}")
    private Double temperature;
    
    @Value("${ai-models.api.max-tokens:1000}")
    private Integer maxTokens;
    
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
        validateRequest(request);
        
        if (chatModel == null) {
            throw new ServiceException("AI_SERVICE_NOT_CONFIGURED", 
                "Spring AI service is not configured. Please check your configuration.");
        }
        
        try {
            String modelCode = getModelCode(request);
            
            UserMessage userMessage = new UserMessage(request.getMessage());
            Prompt prompt = new Prompt(userMessage, buildChatOptions(modelCode));
            
            ChatResponse aiResponse = chatModel.call(prompt);
            
            return convertToChatResponseDTO(aiResponse, request.getUserId());
            
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("SPRING_AI_ERROR", "Failed to communicate with AI service", e);
        }
    }
    
    @Override
    public ChatResponseDTO sendMessageWithConversation(ChatRequestDTO request) {
        return sendMessage(request);
    }
    
    @Override
    public SseEmitter streamMessage(ChatRequestDTO request) {
        throw new ServiceException("STREAMING_NOT_SUPPORTED", 
            "Streaming is not supported for Spring AI direct access");
    }
    
    @Override
    public SseEmitter streamMessageWithConversation(ChatRequestDTO request) {
        return streamMessage(request);
    }
    
    @Override
    public void validateRequest(ChatRequestDTO request) {
        if (request == null) {
            throw new ServiceException("INVALID_REQUEST", "Request cannot be null");
        }
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new ServiceException("EMPTY_MESSAGE", "Message content cannot be empty");
        }
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new ServiceException("MISSING_USER_ID", "User ID is required");
        }
    }
    
    private String getModelCode(ChatRequestDTO request) {
        if (request.getModelCode() != null && !request.getModelCode().isEmpty()) {
            return request.getModelCode();
        }
        
        String provider = request.getModelProvider();
        if ("deepseek".equalsIgnoreCase(provider)) {
            return "deepseek-chat";
        } else {
            return "qwen-plus";
        }
    }
    
    private DashScopeChatOptions buildChatOptions(String modelCode) {
        String provider = MODEL_PROVIDER_MAP.getOrDefault(modelCode, "dashscope");
        
        return DashScopeChatOptions.builder()
            .withModel(modelCode)
            .withTemperature(getTemperatureForProvider(provider))
            .build();
    }
    
    private Double getTemperatureForProvider(String provider) {
        return temperature;
    }
    
    private Integer getMaxTokensForProvider(String provider) {
        return maxTokens;
    }
    
    private ChatResponseDTO convertToChatResponseDTO(ChatResponse aiResponse, String userId) {
        ChatResponseDTO response = new ChatResponseDTO();
        response.setMessageId(UUID.randomUUID().toString());
        response.setCreatedAt(System.currentTimeMillis());
        response.setStatus("success");
        response.setAnswer(aiResponse.getResult().getOutput().getText());
        
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
