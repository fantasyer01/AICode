package com.example.aialibaba.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.example.aialibaba.exception.ServiceException;
import com.example.aialibaba.model.dto.ChatRequestDTO;
import com.example.aialibaba.model.dto.ChatResponseDTO;
import com.example.aialibaba.service.AiModelService;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Implementation of AiModelService using Spring AI Alibaba DashScope
 */
@Service
public class DashScopeAiServiceImpl implements AiModelService {

    private final DashScopeChatModel chatModel;
    
    @Value("${spring.ai.dashscope.api-key:}")
    private String apiKey;
    
    @Value("${ai-models.dashscope.temperature:0.7}")
    private Double temperature;
    
    @Value("${ai-models.dashscope.max-tokens:1000}")
    private Integer maxTokens;

    public DashScopeAiServiceImpl(@Autowired(required = false) DashScopeChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public ChatResponseDTO sendMessage(ChatRequestDTO request) {
        if (chatModel == null || apiKey == null || apiKey.isEmpty()) {
            throw new ServiceException("AI_SERVICE_NOT_CONFIGURED", "DashScope AI service is not configured. Please set the AI_DASHSCOPE_API_KEY environment variable or spring.ai.dashscope.api-key property.");
        }
        // Get the model code from request, fallback to qwen-plus if not specified
        String modelCode = request.getModelCode() != null ? request.getModelCode() : "qwen-plus";
        
        DashScopeChatOptions options = DashScopeChatOptions.builder()
                .withModel(modelCode)
                .withTemperature(temperature)
                .build();

        UserMessage userMessage = new UserMessage(request.getMessage());
        Prompt prompt = new Prompt(userMessage, options);
        
        ChatResponse aiResponse = chatModel.call(prompt);
        
        ChatResponseDTO responseDTO = new ChatResponseDTO();
        responseDTO.setAnswer(aiResponse.getResult().getOutput().getText());
        responseDTO.setMessageId(UUID.randomUUID().toString());
        responseDTO.setCreatedAt(System.currentTimeMillis());
        responseDTO.setStatus("success");
        
        if (aiResponse.getMetadata().getUsage() != null) {
            ChatResponseDTO.Usage usage = new ChatResponseDTO.Usage();
            usage.setPromptTokens(aiResponse.getMetadata().getUsage().getPromptTokens().intValue());
            usage.setCompletionTokens(aiResponse.getMetadata().getUsage().getCompletionTokens().intValue());
            usage.setTotalTokens(aiResponse.getMetadata().getUsage().getTotalTokens().intValue());
            responseDTO.setUsage(usage);
        }
        
        return responseDTO;
    }
}
