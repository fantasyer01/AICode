package com.example.aialibaba.service.impl;

import com.example.aialibaba.handler.wecom.WeComMessageHandler;
import com.example.aialibaba.model.dto.wecom.request.WeComCallbackMessage;
import com.example.aialibaba.model.dto.wecom.response.WeComTextResponse;
import com.example.aialibaba.service.WeComCryptService;
import com.example.aialibaba.service.WeComMessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of WeComMessageService with message routing and deduplication.
 */
@Service
public class WeComMessageServiceImpl implements WeComMessageService {
    
    private static final Logger logger = LoggerFactory.getLogger(WeComMessageServiceImpl.class);
    
    private static final long MESSAGE_TTL_MS = TimeUnit.MINUTES.toMillis(5);
    
    private final WeComCryptService weComCryptService;
    private final List<WeComMessageHandler> messageHandlers;
    private final ObjectMapper objectMapper;
    
    // Simple in-memory cache for message deduplication
    // In production, consider using Redis for distributed scenarios
    private final Map<String, Long> processedMessages = new ConcurrentHashMap<>();
    
    public WeComMessageServiceImpl(WeComCryptService weComCryptService,
                                    List<WeComMessageHandler> messageHandlers) {
        this.weComCryptService = weComCryptService;
        this.messageHandlers = messageHandlers.stream()
                .sorted(Comparator.comparingInt(WeComMessageHandler::getPriority))
                .toList();
        
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        logger.info("WeComMessageService initialized with {} handlers", messageHandlers.size());
        
        // Start cleanup thread for expired messages
        startCleanupThread();
    }
    
    @Override
    public String processCallback(String msgSignature, String timestamp, String nonce, String postData) {
        try {
            // 1. Decrypt the message
            String decryptedContent = weComCryptService.decryptMsg(msgSignature, timestamp, nonce, postData);
            
            logger.debug("Decrypted callback content: {}", decryptedContent);
            
            // 2. Parse the JSON message
            WeComCallbackMessage callbackMessage = parseMessage(decryptedContent);
            
            if (callbackMessage == null) {
                logger.error("Failed to parse callback message");
                return createErrorResponse(timestamp, nonce);
            }
            
            // 3. Check for duplicate message
            String msgId = callbackMessage.getMsgId();
            if (msgId != null && isDuplicateMessage(msgId)) {
                logger.info("Duplicate message detected, msgId: {}", msgId);
                return ""; // Return empty for duplicate messages
            }
            
            // 4. Mark message as processed
            if (msgId != null) {
                markMessageProcessed(msgId);
            }
            
            // 5. Route to appropriate handler
            Object response = routeMessage(callbackMessage);
            
            // 6. Encrypt and return response
            if (response == null) {
                return ""; // Return empty if no response needed
            }
            
            String responseJson = serializeResponse(response);
            return weComCryptService.encryptMsg(responseJson, timestamp, nonce);
            
        } catch (Exception e) {
            logger.error("Error processing WeCom callback", e);
            return createErrorResponse(timestamp, nonce);
        }
    }
    
    @Override
    public boolean isDuplicateMessage(String msgId) {
        if (msgId == null || msgId.isEmpty()) {
            return false;
        }
        
        Long processedTime = processedMessages.get(msgId);
        if (processedTime == null) {
            return false;
        }
        
        // Check if the message is still within the TTL window
        return System.currentTimeMillis() - processedTime < MESSAGE_TTL_MS;
    }
    
    @Override
    public void markMessageProcessed(String msgId) {
        if (msgId != null && !msgId.isEmpty()) {
            processedMessages.put(msgId, System.currentTimeMillis());
        }
    }
    
    private WeComCallbackMessage parseMessage(String content) {
        try {
            return objectMapper.readValue(content, WeComCallbackMessage.class);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse message JSON: {}", e.getMessage());
            return null;
        }
    }
    
    private Object routeMessage(WeComCallbackMessage message) {
        String msgType = message.getMsgType();
        
        for (WeComMessageHandler handler : messageHandlers) {
            if (handler.supports(msgType)) {
                logger.debug("Routing message type '{}' to handler: {}", 
                        msgType, handler.getClass().getSimpleName());
                return handler.handle(message);
            }
        }
        
        logger.warn("No handler found for message type: {}", msgType);
        return WeComTextResponse.of("I'm not sure how to handle this type of message.");
    }
    
    private String serializeResponse(Object response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize response: {}", e.getMessage());
            return "{\"msgtype\":\"text\",\"text\":{\"content\":\"An error occurred.\"}}";
        }
    }
    
    private String createErrorResponse(String timestamp, String nonce) {
        try {
            String errorJson = "{\"msgtype\":\"text\",\"text\":{\"content\":\"Sorry, an error occurred processing your message.\"}}";
            return weComCryptService.encryptMsg(errorJson, timestamp, nonce);
        } catch (Exception e) {
            logger.error("Failed to create error response", e);
            return "";
        }
    }
    
    private void startCleanupThread() {
        Thread cleanupThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(TimeUnit.MINUTES.toMillis(1));
                    cleanupExpiredMessages();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "wecom-message-cleanup");
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }
    
    private void cleanupExpiredMessages() {
        long now = System.currentTimeMillis();
        int removed = 0;
        
        for (Map.Entry<String, Long> entry : processedMessages.entrySet()) {
            if (now - entry.getValue() > MESSAGE_TTL_MS) {
                processedMessages.remove(entry.getKey());
                removed++;
            }
        }
        
        if (removed > 0) {
            logger.debug("Cleaned up {} expired message IDs", removed);
        }
    }
}
