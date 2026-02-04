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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

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
            
            // Prepare headers
            HttpHeaders headers = buildDifyHeaders(request, appConfig);

            // Prepare request body for Dify API
            Map<String, Object> requestBody = buildDifyRequestBody(request);
            
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
    
    /**
     * Public method to expose streaming capability
     */
    public SseEmitter streamMessageWithConversation(ChatRequestDTO request) {
        // For conversation support, we can use the same streaming method
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
    
    /**
     * Build request body for Dify API call
     */
    private Map<String, Object> buildDifyRequestBody(ChatRequestDTO request) {
        Map<String, Object> body = new HashMap<>();
        
        // Use inputs from request or default to empty map
        Map<String, Object> inputs = request.getInputs() != null ? request.getInputs() : new HashMap<>();
        
        // Dynamically handle varied input parameters
        // If query is not enough, users can pass more in 'inputs'
        body.put("inputs", inputs);
        
        body.put("query", request.getMessage());
        
        // Use response mode from configuration
        String responseMode = difyConfig.getApi().getResponseMode();
        body.put("response_mode", difyConfig.getApi().getResponseMode());
        
        body.put("conversation_id", request.getConversationId());
        body.put("user", request.getUserId());
        
        // Add model parameters from API configuration
        body.put("temperature", difyConfig.getApi().getTemperature());
        body.put("max_tokens", difyConfig.getApi().getMaxTokens());
        
        logger.debug("Built Dify request body with response_mode '{}': {}", responseMode, body);
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
    
    /**
     * Send message with streaming response using Server-Sent Events
     */
    public SseEmitter streamMessage(ChatRequestDTO request) {
        logger.info("Starting streaming message to Dify - User: {}, AppCode: {}",
                   request.getUserId(),
                   request.getAppCode() != null ? request.getAppCode() : "default");
        
        validateRequest(request);
        
        SseEmitter emitter = new SseEmitter(60000L);
        
        CompletableFuture.runAsync(() -> {
            try {
                // Get application configuration
                String appCode = request.getAppCode() != null ? request.getAppCode() : "default";
                DifyConfig.AppConfig appConfig = difyConfig.getApps().get(appCode);
                if (appConfig == null) {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("{\"error\": \"APP_CONFIG_NOT_FOUND\", \"message\": \"No configuration found for app code: " + appCode + "\"}"));
                    emitter.complete();
                    return;
                }
                
                // Prepare request headers and body
                HttpHeaders headers = buildDifyHeaders(request, appConfig);
                Map<String, Object> requestBody = buildDifyRequestBody(request);
                
                // Make streaming HTTP request
                streamDifyResponse(requestBody, headers, emitter, request.getUserId());
                
            } catch (Exception e) {
                logger.error("Error during streaming", e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("{\"error\": \"STREAMING_ERROR\", \"message\": \"" + e.getMessage() + "\"}"));
                } catch (IOException ioException) {
                    logger.error("Failed to send error event", ioException);
                }
                emitter.completeWithError(e);
            }
        });
        
        return emitter;
    }
    
    /**
     * Stream Dify API response using chunked transfer
     */
    private void streamDifyResponse(Map<String, Object> requestBody, 
                                   HttpHeaders headers,
                                   SseEmitter emitter, 
                                   String userId) throws IOException {
        
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        
        try {
            // Convert request body to JSON
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            
            // Create HTTP connection
            URL url = new URL(difyConfig.getChatEndpoint());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            // Use HTTP read timeout for streaming connection
            int readTimeout = 60000; // Default 60 seconds
            try {
                readTimeout = Integer.parseInt(System.getProperty("spring.http.read-timeout", "60000"));
            } catch (NumberFormatException e) {
                logger.warn("Invalid HTTP read timeout value for connection, using default 60000ms");
            }
            connection.setReadTimeout(readTimeout);
            
            // Set headers
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "text/event-stream");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setRequestProperty("Connection", "keep-alive");
            
            // Add authorization header
            String authHeader = headers.getFirst("Authorization");
            if (authHeader != null) {
                connection.setRequestProperty("Authorization", authHeader);
            }
            
            // Send request body
            try (java.io.OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Read streaming response
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new ServiceException("HTTP_ERROR", "Dify API returned status: " + responseCode);
            }
            
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            
            logger.info("===== Starting to read Dify SSE stream for user: {} =====", userId);
            
            String line;
            String currentEvent = "message";
            int lineCount = 0;
            
            while ((line = reader.readLine()) != null) {
                lineCount++;
                logger.debug("Line #{}: {}", lineCount, line);
                
                // Parse SSE format: event: <event-name> and data: <json-data>
                if (line.startsWith("event: ")) {
                    currentEvent = line.substring(7).trim();
                    logger.info("Received event type from Dify: {}", currentEvent);
                } else if (line.startsWith("data: ")) {
                    String eventData = line.substring(6); // Remove "data: " prefix
                    logger.info("Received data from Dify: {}", eventData);
                    
                    if (!eventData.trim().isEmpty() && !eventData.equals("[DONE]")) {
                        try {
                            // Parse the Dify event data
                            Map<String, Object> difyData = objectMapper.readValue(eventData, Map.class);
                            logger.debug("Parsed Dify data: {}", difyData);
                            
                            // Transform based on event type
                            Map<String, Object> transformedData = new HashMap<>();
                            
                            // Keep Dify's original event field (like 'ping', 'message', 'agent_thought', etc.)
                            // If not present, use the SSE event type
                            if (difyData.containsKey("event")) {
                                transformedData.put("event", difyData.get("event"));
                            } else {
                                transformedData.put("event", currentEvent);
                            }
                            
                            // Extract common fields
                            if (difyData.containsKey("answer")) {
                                String answer = (String) difyData.get("answer");
                                transformedData.put("answer", answer);
                                logger.info("Extracted answer field: '{}'", answer);
                            }
                            if (difyData.containsKey("conversation_id")) {
                                transformedData.put("conversation_id", difyData.get("conversation_id"));
                            }
                            if (difyData.containsKey("message_id")) {
                                transformedData.put("message_id", difyData.get("message_id"));
                            }
                            if (difyData.containsKey("created_at")) {
                                transformedData.put("created_at", difyData.get("created_at"));
                            }
                            
                            // For message_end event, mark as completed
                            if ("message_end".equals(currentEvent)) {
                                transformedData.put("status", "completed");
                                logger.info("Message end detected, marking as completed");
                            }
                            
                            // Send transformed data to frontend
                            String transformedJson = objectMapper.writeValueAsString(transformedData);
                            logger.info("Sending SSE to frontend - event: message, data: {}", transformedJson);
                            
                            emitter.send(SseEmitter.event()
                                    .name("message")
                                    .data(transformedJson));
                            
                            logger.debug("Successfully sent SSE event to frontend");
                            
                        } catch (Exception parseEx) {
                            logger.warn("Failed to parse Dify event data: {}", eventData, parseEx);
                            // Send raw data as fallback
                            emitter.send(SseEmitter.event()
                                    .name("message")
                                    .data(eventData));
                        }
                    }
                }
            }
            
            reader.close();
            emitter.send(SseEmitter.event().name("end").data("{}"));
            emitter.complete();
            logger.info("Completed streaming for user: {}", userId);
            
        } catch (Exception e) {
            logger.error("Error in streaming response for user: {}", userId, e);
            throw new IOException("Streaming failed", e);
        }
    }
}