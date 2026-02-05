package com.example.aialibaba.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for health check and service information endpoints
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Health & Info", description = "Health check and service information endpoints")
public class HealthController {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
    
    /**
     * Health check endpoint for chat service
     */
    @GetMapping("/chat/health")
    @Operation(summary = "Chat service health check")
    public ResponseEntity<String> chatHealthCheck() {
        logger.debug("Chat health check requested");
        return ResponseEntity.ok("Chat service is operational");
    }
    
    /**
     * Health check endpoint for streaming service
     */
    @GetMapping("/stream/health")
    @Operation(summary = "Streaming service health check")
    public ResponseEntity<String> streamHealthCheck() {
        logger.debug("Streaming health check requested");
        return ResponseEntity.ok("Streaming chat service is operational");
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
}
