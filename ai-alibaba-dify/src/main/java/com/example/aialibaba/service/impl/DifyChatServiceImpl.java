package com.example.aialibaba.service.impl;

import com.example.aialibaba.config.DifyConfig;
import com.example.aialibaba.exception.ServiceException;
import com.example.aialibaba.model.dto.ChatRequestDTO;
import com.example.aialibaba.model.dto.ChatResponseDTO;
import com.example.aialibaba.service.ChatService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of ChatService for Dify integration
 * Uses OkHttp3 for HTTP communication with unified blocking/streaming support
 */
@Service
public class DifyChatServiceImpl implements ChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(DifyChatServiceImpl.class);
    private static final okhttp3.MediaType JSON_MEDIA_TYPE = okhttp3.MediaType.parse("application/json; charset=utf-8");
    
    private final OkHttpClient okHttpClient;
    private final DifyConfig difyConfig;
    private final ObjectMapper objectMapper;
    
    public DifyChatServiceImpl(OkHttpClient okHttpClient, DifyConfig difyConfig) {
        this.okHttpClient = okHttpClient;
        this.difyConfig = difyConfig;
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public ChatResponseDTO sendMessage(ChatRequestDTO request) {
        logger.info("Sending message to Dify - User: {}, AppCode: {}, Message: {}", 
                   request.getUserId(), 
                   request.getAppCode() != null ? request.getAppCode() : "default",
                   request.getMessage());
        
        return (ChatResponseDTO) executeRequest(request, false);
    }
    
    @Override
    public ChatResponseDTO sendMessageWithConversation(ChatRequestDTO request) {
        return sendMessage(request);
    }
    
    @Override
    public SseEmitter streamMessage(ChatRequestDTO request) {
        logger.info("Starting streaming message to Dify - User: {}, AppCode: {}",
                   request.getUserId(),
                   request.getAppCode() != null ? request.getAppCode() : "default");
        
        return (SseEmitter) executeRequest(request, true);
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
    
    /**
     * Unified entry point - routes to blocking or streaming based on forceStreaming parameter
     * @param request the chat request
     * @param forceStreaming true for streaming mode, false for blocking mode
     */
    private Object executeRequest(ChatRequestDTO request, boolean forceStreaming) {
        validateRequest(request);
        
        String appCode = request.getAppCode() != null ? request.getAppCode() : "default";
        DifyConfig.AppConfig appConfig = getAppConfig(appCode);
        
        Map<String, Object> requestBody = buildDifyRequestBody(request);
        
        // Use forceStreaming parameter to determine mode, not config
        String responseMode = forceStreaming ? "streaming" : "blocking";
        requestBody.put("response_mode", responseMode);
        
        if (forceStreaming) {
            return handleStreamingRequest(requestBody, appConfig, request.getUserId());
        } else {
            return handleBlockingRequest(requestBody, appConfig, request.getUserId());
        }
    }
    
    /**
     * Get application configuration with validation
     */
    private DifyConfig.AppConfig getAppConfig(String appCode) {
        DifyConfig.AppConfig appConfig = difyConfig.getApps().get(appCode);
        if (appConfig == null) {
            throw new ServiceException("APP_CONFIG_NOT_FOUND", "No configuration found for app code: " + appCode);
        }
        return appConfig;
    }
    
    /**
     * Build request body for Dify API call
     */
    private Map<String, Object> buildDifyRequestBody(ChatRequestDTO request) {
        Map<String, Object> body = new HashMap<>();
        
        Map<String, Object> inputs = request.getInputs() != null ? request.getInputs() : new HashMap<>();
        body.put("inputs", inputs);
        body.put("query", request.getMessage());
        body.put("conversation_id", request.getConversationId());
        body.put("user", request.getUserId());
        body.put("temperature", difyConfig.getApi().getTemperature());
        body.put("max_tokens", difyConfig.getApi().getMaxTokens());
        
        logger.debug("Built Dify request body: {}", body);
        return body;
    }
    
    /**
     * Handle blocking request using OkHttp3
     */
    private ChatResponseDTO handleBlockingRequest(Map<String, Object> requestBody,
                                                   DifyConfig.AppConfig appConfig,
                                                   String userId) {
        try {
            RequestBody body = RequestBody.create(
                objectMapper.writeValueAsBytes(requestBody),
                JSON_MEDIA_TYPE
            );
            
            Request request = new Request.Builder()
                .url(difyConfig.getChatEndpoint())
                .post(body)
                .header("Authorization", "Bearer " + appConfig.getApiKey())
                .header("Content-Type", "application/json")
                .header("X-Request-ID", UUID.randomUUID().toString())
                .build();
            
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new ServiceException("HTTP_ERROR", "Dify API returned status: " + response.code());
                }
                
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    throw new ServiceException("EMPTY_RESPONSE", "Received empty response from Dify");
                }
                
                Map<String, Object> responseMap = objectMapper.readValue(
                    responseBody.string(),
                    new TypeReference<Map<String, Object>>() {}
                );
                
                logger.info("Received response from Dify with status: {}", response.code());
                return processDifyResponse(responseMap, userId);
            }
            
        } catch (ServiceException e) {
            throw e;
        } catch (IOException e) {
            logger.error("Failed to communicate with Dify API", e);
            throw new ServiceException("DIFY_API_ERROR", "Failed to communicate with AI service", e);
        } catch (Exception e) {
            logger.error("Unexpected error during Dify API call", e);
            throw new ServiceException("UNEXPECTED_ERROR", "An unexpected error occurred", e);
        }
    }
    
    /**
     * Handle streaming request using OkHttp3 async
     */
    private SseEmitter handleStreamingRequest(Map<String, Object> requestBody,
                                              DifyConfig.AppConfig appConfig,
                                              String userId) {
        SseEmitter emitter = new SseEmitter(60000L);
        
        try {
            RequestBody body = RequestBody.create(
                objectMapper.writeValueAsBytes(requestBody),
                JSON_MEDIA_TYPE
            );
            
            Request request = new Request.Builder()
                .url(difyConfig.getChatEndpoint())
                .post(body)
                .header("Authorization", "Bearer " + appConfig.getApiKey())
                .header("Accept", "text/event-stream")
                .header("Cache-Control", "no-cache")
                .header("X-Request-ID", UUID.randomUUID().toString())
                .build();
            
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    processStreamResponse(response, emitter, userId);
                }
                
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    logger.error("Streaming request failed for user: {}", userId, e);
                    try {
                        emitter.send(SseEmitter.event()
                            .name("error")
                            .data("{\"error\": \"STREAMING_ERROR\", \"message\": \"" + e.getMessage() + "\"}"));
                    } catch (IOException ioEx) {
                        logger.error("Failed to send error event", ioEx);
                    }
                    emitter.completeWithError(e);
                }
            });
            
        } catch (Exception e) {
            logger.error("Failed to initiate streaming request", e);
            emitter.completeWithError(e);
        }
        
        return emitter;
    }
    
    /**
     * Process streaming response from Dify
     */
    private void processStreamResponse(Response response, SseEmitter emitter, String userId) {
        try {
            if (!response.isSuccessful()) {
                throw new ServiceException("HTTP_ERROR", "Dify API returned status: " + response.code());
            }
            
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new ServiceException("EMPTY_RESPONSE", "Received empty response from Dify");
            }
            
            logger.info("===== Starting to read Dify SSE stream for user: {} =====", userId);
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(responseBody.byteStream(), StandardCharsets.UTF_8))) {
                
                String line;
                String currentEvent = "message";
                int lineCount = 0;
                
                while ((line = reader.readLine()) != null) {
                    lineCount++;
                    logger.debug("Line #{}: {}", lineCount, line);
                    
                    if (line.startsWith("event: ")) {
                        currentEvent = line.substring(7).trim();
                        logger.debug("Received event type from Dify: {}", currentEvent);
                    } else if (line.startsWith("data: ")) {
                        String eventData = line.substring(6);
                        
                        if (!eventData.trim().isEmpty() && !eventData.equals("[DONE]")) {
                            processAndSendEvent(eventData, currentEvent, emitter);
                        }
                    }
                }
            }
            
            emitter.send(SseEmitter.event().name("end").data("{}"));
            emitter.complete();
            logger.info("Completed streaming for user: {}", userId);
            
        } catch (Exception e) {
            logger.error("Error in streaming response for user: {}", userId, e);
            try {
                emitter.send(SseEmitter.event()
                    .name("error")
                    .data("{\"error\": \"STREAMING_ERROR\", \"message\": \"" + e.getMessage() + "\"}"));
            } catch (IOException ioEx) {
                logger.error("Failed to send error event", ioEx);
            }
            emitter.completeWithError(e);
        } finally {
            response.close();
        }
    }
    
    /**
     * Process and send a single SSE event
     */
    @SuppressWarnings("unchecked")
    private void processAndSendEvent(String eventData, String currentEvent, SseEmitter emitter) {
        try {
            Map<String, Object> difyData = objectMapper.readValue(eventData, Map.class);
            Map<String, Object> transformedData = new HashMap<>();
            
            // Keep Dify's original event field or use SSE event type
            if (difyData.containsKey("event")) {
                transformedData.put("event", difyData.get("event"));
            } else {
                transformedData.put("event", currentEvent);
            }
            
            // Extract common fields
            if (difyData.containsKey("answer")) {
                transformedData.put("answer", difyData.get("answer"));
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
                logger.debug("Message end detected, marking as completed");
            }
            
            String transformedJson = objectMapper.writeValueAsString(transformedData);
            logger.debug("Sending SSE to frontend - event: message, data: {}", transformedJson);
            
            emitter.send(SseEmitter.event()
                .name("message")
                .data(transformedJson));
            
        } catch (Exception parseEx) {
            logger.warn("Failed to parse Dify event data: {}", eventData, parseEx);
            try {
                emitter.send(SseEmitter.event()
                    .name("message")
                    .data(eventData));
            } catch (IOException e) {
                logger.error("Failed to send raw event data", e);
            }
        }
    }
    
    /**
     * Process Dify API response for blocking mode
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
        
        Object answerObj = responseBody.get("answer");
        if (answerObj instanceof String) {
            response.setAnswer((String) answerObj);
        } else {
            response.setAnswer("Response received but format not recognized");
            logger.warn("Unexpected answer format in Dify response: {}", answerObj);
        }
        
        Object usageObj = responseBody.get("usage");
        if (usageObj instanceof Map) {
            Map<String, Object> usageMap = (Map<String, Object>) usageObj;
            ChatResponseDTO.Usage usage = new ChatResponseDTO.Usage();
            usage.setPromptTokens(getIntValue(usageMap, "prompt_tokens"));
            usage.setCompletionTokens(getIntValue(usageMap, "completion_tokens"));
            usage.setTotalTokens(getIntValue(usageMap, "total_tokens"));
            response.setUsage(usage);
            
            logger.info("Token Usage - Prompt: {}, Completion: {}, Total: {}", 
                       usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
        }
        
        logger.info("Successfully processed Dify response for user: {}", userId);
        return response;
    }
    
    /**
     * Safely get integer value from map
     */
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }
}
