package com.example.aialibaba.service.impl;

import com.example.aialibaba.config.DifyConfig;
import com.example.aialibaba.exception.ServiceException;
import com.example.aialibaba.model.dto.ChatRequestDTO;
import com.example.aialibaba.model.dto.ChatResponseDTO;
import com.example.aialibaba.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of ChatService for Dify integration
 */
@Service
public class DifyChatServiceImpl implements ChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(DifyChatServiceImpl.class);
    
    private final RestTemplate restTemplate;
    private final DifyConfig difyConfig;
    
    public DifyChatServiceImpl(RestTemplate restTemplate, DifyConfig difyConfig) {
        this.restTemplate = restTemplate;
        this.difyConfig = difyConfig;
    }
    
    @Override
    public ChatResponseDTO sendMessage(ChatRequestDTO request) {
        logger.info("Sending message to Dify - User: {}, AppCode: {}, Model: {}, Message: {}", 
                   request.getUserId(), 
                   request.getAppCode() != null ? request.getAppCode() : "default",
                   request.getModelCode() != null ? request.getModelCode() : "default",
                   request.getMessage());
        
        validateRequest(request);
        
        try {
            // Get application configuration
            String appCode = request.getAppCode() != null ? request.getAppCode() : "default";
            DifyConfig.AppConfig appConfig = difyConfig.getApps().get(appCode);
            if (appConfig == null) {
                throw new ServiceException("APP_CONFIG_NOT_FOUND", "No configuration found for app code: " + appCode);
            }
            
            // Log model selection if provided
            if (request.getModelCode() != null) {
                logger.info("Using AI model: {} from provider: {}", 
                           request.getModelCode(), 
                           request.getModelProvider() != null ? request.getModelProvider() : "default");
            }

            // Prepare request body for Dify API
            Map<String, Object> requestBody = buildDifyRequestBody(request, appConfig);
            
            // Prepare headers
            HttpHeaders headers = buildDifyHeaders(request, appConfig);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // Make API call to Dify
            ResponseEntity<Map> response = restTemplate.exchange(
                    difyConfig.getChatEndpoint(),
                    HttpMethod.POST,
                    entity,
                    Map.class
            );
            
            logger.info("Received response from Dify with status: {}", response.getStatusCode());
            
            // Process response
            return processDifyResponse(response.getBody(), request.getUserId());
            
        } catch (RestClientException e) {
            logger.error("Failed to communicate with Dify API", e);
            throw new ServiceException("DIFY_API_ERROR", "Failed to communicate with AI service", e);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during Dify API call", e);
            throw new ServiceException("UNEXPECTED_ERROR", "An unexpected error occurred", e);
        }
    }
    
    @Override
    public ChatResponseDTO sendMessageWithConversation(ChatRequestDTO request) {
        // For conversation support, we can use the same endpoint
        // Dify handles conversation context internally
        return sendMessage(request);
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
    
    /**
     * Build request body for Dify API call
     */
    private Map<String, Object> buildDifyRequestBody(ChatRequestDTO request, DifyConfig.AppConfig appConfig) {
        Map<String, Object> body = new HashMap<>();
        
        // Use inputs from request or default to empty map
        Map<String, Object> inputs = request.getInputs() != null ? request.getInputs() : new HashMap<>();
        
        // Dynamically handle varied input parameters
        // If query is not enough, users can pass more in 'inputs'
        body.put("inputs", inputs);
        
        body.put("query", request.getMessage());
        body.put("response_mode", "blocking");
        body.put("conversation_id", request.getConversationId());
        body.put("user", request.getUserId());
        
        // Add model parameters from API configuration
        body.put("temperature", difyConfig.getApi().getTemperature());
        body.put("max_tokens", difyConfig.getApi().getMaxTokens());
        
        logger.debug("Built Dify request body: {}", body);
        return body;
    }
    
    /**
     * Build headers for Dify API call
     */
    private HttpHeaders buildDifyHeaders(ChatRequestDTO request, DifyConfig.AppConfig appConfig) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Use API key from configuration only
        String apiKey = appConfig.getApiKey();
        headers.set("Authorization", "Bearer " + apiKey);
        
        // Add request ID for tracing
        headers.set("X-Request-ID", UUID.randomUUID().toString());
        
        return headers;
    }
    
    /**
     * Process Dify API response
     */
    @SuppressWarnings("unchecked")
    private ChatResponseDTO processDifyResponse(Map<String, Object> responseBody, String userId) {
        if (responseBody == null) {
            throw new ServiceException("EMPTY_RESPONSE", "Received empty response from Dify");
        }
        
        ChatResponseDTO response = new ChatResponseDTO();
        response.setMessageId((String) responseBody.get("message_id"));
        response.setConversationId((String) responseBody.get("conversation_id"));
        response.setCreatedAt(System.currentTimeMillis());
        response.setStatus("success");
        
        // Extract answer from response
        Object answerObj = responseBody.get("answer");
        if (answerObj instanceof String) {
            response.setAnswer((String) answerObj);
        } else {
            // Handle streaming or other response formats
            response.setAnswer("Response received but format not recognized");
            logger.warn("Unexpected answer format in Dify response: {}", answerObj);
        }
        
        // Extract usage information if available
        Object usageObj = responseBody.get("usage");
        if (usageObj instanceof Map) {
            Map<String, Object> usageMap = (Map<String, Object>) usageObj;
            ChatResponseDTO.Usage usage = new ChatResponseDTO.Usage();
            usage.setPromptTokens((Integer) usageMap.get("prompt_tokens"));
            usage.setCompletionTokens((Integer) usageMap.get("completion_tokens"));
            usage.setTotalTokens((Integer) usageMap.get("total_tokens"));
            response.setUsage(usage);
            
            // Production-grade logging for token monitoring
            logger.info("Token Usage - Prompt: {}, Completion: {}, Total: {}", 
                       usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
        }
        
        logger.info("Successfully processed Dify response for user: {}", userId);
        return response;
    }
}