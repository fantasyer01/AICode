package com.example.aialibaba.controller;

import com.example.aialibaba.handler.wecom.WeComMessageHandler;
import com.example.aialibaba.model.dto.wecom.request.WeComCallbackMessage;
import com.example.aialibaba.model.dto.wecom.response.WeComTextResponse;
import com.example.aialibaba.service.WeComCryptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for health check and service information endpoints
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Health & Info", description = "Health check and service information endpoints")
public class HealthController {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
    
    private final WeComCryptService weComCryptService;
    private final List<WeComMessageHandler> messageHandlers;
    
    public HealthController(WeComCryptService weComCryptService,
                          List<WeComMessageHandler> messageHandlers) {
        this.weComCryptService = weComCryptService;
        this.messageHandlers = messageHandlers != null ? messageHandlers.stream()
                .sorted(Comparator.comparingInt(WeComMessageHandler::getPriority))
                .toList() : List.of();
    }
    
    /**
     * Unified health check endpoint for all services
     */
    @GetMapping("/health")
    @Operation(summary = "Service health check", 
               description = "Returns health status of all services including chat, streaming, and WeCom integration")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        logger.debug("Health check requested");
        
        Map<String, Object> health = new HashMap<>();
        health.put("status", "ok");
        health.put("timestamp", System.currentTimeMillis());
        
        // Chat service status
        Map<String, String> chatStatus = new HashMap<>();
        chatStatus.put("status", "operational");
        chatStatus.put("message", "Chat service is running");
        health.put("chat", chatStatus);
        
        // Streaming service status
        Map<String, String> streamStatus = new HashMap<>();
        streamStatus.put("status", "operational");
        streamStatus.put("message", "Streaming chat service is running");
        health.put("streaming", streamStatus);
        
        // WeCom integration status
        Map<String, Object> wecomStatus = new HashMap<>();
        boolean configured = weComCryptService.isConfigured();
        wecomStatus.put("status", configured ? "operational" : "not_configured");
        wecomStatus.put("configured", configured);
        if (!configured) {
            wecomStatus.put("message", "WeCom not configured");
        }
        health.put("wecom", wecomStatus);
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * Get service information including streaming capabilities
     */
    @GetMapping("/chat/info")
    @Operation(summary = "Get service information")
    public ResponseEntity<Map<String, Object>> getServiceInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "Spring AI Alibaba Dify Integration");
        info.put("version", "1.0.0");
        info.put("status", "operational");
        info.put("timestamp", System.currentTimeMillis());
        
        // Add streaming support information
        Map<String, Object> streaming = new HashMap<>();
        streaming.put("supported", true);
        streaming.put("endpoint", "/api/v1/stream/chat");
        streaming.put("format", "Server-Sent Events (SSE)");
        streaming.put("modes", new String[]{"blocking", "streaming"});
        info.put("streaming", streaming);
        
        logger.debug("Service info requested");
        return ResponseEntity.ok(info);
    }
    
    /**
     * Test endpoint for WeCom message processing.
     * Accepts unencrypted JSON messages directly and processes them through the handlers.
     * This endpoint is for development/testing only.
     * 
     * @param message The unencrypted message in WeComCallbackMessage format
     * @return The response object (text, markdown, etc.)
     */
    @PostMapping(value = "/wecom/test", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test WeCom message processing", 
               description = "Test endpoint for processing unencrypted WeCom messages (development only)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Message processed successfully"),
        @ApiResponse(responseCode = "500", description = "Message processing failed")
    })
    public Object testWeComMessage(@RequestBody WeComCallbackMessage message) {
        logger.info("Received test message - msgType: {}, from: {}", 
                message.getMsgType(), message.getUserId());
        
        String msgType = message.getMsgType();
        
        for (WeComMessageHandler handler : messageHandlers) {
            if (handler.supports(msgType)) {
                logger.debug("Routing test message to handler: {}", handler.getClass().getSimpleName());
                Object response = handler.handle(message);
                if (response != null) {
                    return response;
                }
            }
        }
        
        // Default fallback
        return WeComTextResponse.of("I received your message, but couldn't process it.");
    }
}
