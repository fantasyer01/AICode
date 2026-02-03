package com.example.aialibaba.service.impl;

import com.example.aialibaba.exception.ServiceException;
import com.example.aialibaba.model.dto.ChatRequestDTO;
import com.example.aialibaba.model.dto.ChatResponseDTO;
import com.example.aialibaba.service.AiModelService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of AiModelService for DeepSeek API
 */
@Service
public class DeepSeekAiServiceImpl implements AiModelService {
    
    private final RestTemplate restTemplate;
    
    @Value("${spring.ai.deepseek.api-key:}")
    private String apiKey;
    
    @Value("${ai-models.deepseek.temperature:0.7}")
    private Double temperature;
    
    @Value("${ai-models.deepseek.max-tokens:1000}")
    private Integer maxTokens;
    
    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions";
    
    public DeepSeekAiServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @Override
    public ChatResponseDTO sendMessage(ChatRequestDTO request) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new ServiceException("AI_SERVICE_NOT_CONFIGURED", 
                "DeepSeek AI service is not configured. Please set the DEEPSEEK_API_KEY environment variable or spring.ai.deepseek.api-key property.");
        }
        
        try {
            // Prepare request body for DeepSeek API
            Map<String, Object> requestBody = buildDeepSeekRequestBody(request);
            
            // Prepare headers
            HttpHeaders headers = buildDeepSeekHeaders();
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // Make API call to DeepSeek
            ResponseEntity<Map> response = restTemplate.postForEntity(
                DEEPSEEK_API_URL,
                entity,
                Map.class
            );
            
            // Process response
            return processDeepSeekResponse(response.getBody(), request.getUserId());
            
        } catch (RestClientException e) {
            throw new ServiceException("DEEPSEEK_API_ERROR", "Failed to communicate with DeepSeek API", e);
        } catch (Exception e) {
            throw new ServiceException("UNEXPECTED_ERROR", "An unexpected error occurred", e);
        }
    }
    
    /**
     * Build request body for DeepSeek API call
     */
    private Map<String, Object> buildDeepSeekRequestBody(ChatRequestDTO request) {
        Map<String, Object> body = new HashMap<>();
        
        // Build messages array
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", request.getMessage());
        
        body.put("messages", new Object[]{userMessage});
        body.put("model", request.getModelCode() != null ? request.getModelCode() : "deepseek-chat");
        body.put("temperature", temperature);
        body.put("max_tokens", maxTokens);
        
        return body;
    }
    
    /**
     * Build headers for DeepSeek API call
     */
    private HttpHeaders buildDeepSeekHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        return headers;
    }
    
    /**
     * Process DeepSeek API response
     */
    @SuppressWarnings("unchecked")
    private ChatResponseDTO processDeepSeekResponse(Map<String, Object> responseBody, String userId) {
        if (responseBody == null) {
            throw new ServiceException("EMPTY_RESPONSE", "Received empty response from DeepSeek");
        }
        
        ChatResponseDTO response = new ChatResponseDTO();
        response.setMessageId(UUID.randomUUID().toString());
        response.setCreatedAt(System.currentTimeMillis());
        response.setStatus("success");
        
        // Extract answer from response
        Object choicesObj = responseBody.get("choices");
        if (choicesObj instanceof Object[]) {
            Object[] choices = (Object[]) choicesObj;
            if (choices.length > 0 && choices[0] instanceof Map) {
                Map<String, Object> firstChoice = (Map<String, Object>) choices[0];
                Object messageObj = firstChoice.get("message");
                if (messageObj instanceof Map) {
                    Map<String, Object> message = (Map<String, Object>) messageObj;
                    response.setAnswer((String) message.get("content"));
                }
            }
        }
        
        // Extract usage information if available
        Object usageObj = responseBody.get("usage");
        if (usageObj instanceof Map) {
            Map<String, Object> usageMap = (Map<String, Object>) usageObj;
            ChatResponseDTO.Usage usage = new ChatResponseDTO.Usage();
            usage.setPromptTokens(((Number) usageMap.get("prompt_tokens")).intValue());
            usage.setCompletionTokens(((Number) usageMap.get("completion_tokens")).intValue());
            usage.setTotalTokens(((Number) usageMap.get("total_tokens")).intValue());
            response.setUsage(usage);
        }
        
        return response;
    }
}