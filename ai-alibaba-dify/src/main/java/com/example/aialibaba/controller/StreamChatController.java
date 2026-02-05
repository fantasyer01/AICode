package com.example.aialibaba.controller;

import com.example.aialibaba.model.dto.ChatRequestDTO;
import com.example.aialibaba.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * REST controller for streaming chat operations with Server-Sent Events
 * Routes through UnifiedChatServiceImpl for consistent service routing
 */
@RestController
@RequestMapping("/api/v1/stream")
@Tag(name = "Streaming Chat Interface", description = "Operations for streaming interactions with AI models and Dify")
public class StreamChatController {
    
    private static final Logger logger = LoggerFactory.getLogger(StreamChatController.class);
    
    private final ChatService chatService;
    
    public StreamChatController(ChatService chatService) {
        this.chatService = chatService;
    }
    
    /**
     * Stream a message response using Server-Sent Events
     */
    @PostMapping(path = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
        summary = "Stream chat message", 
        description = "Streams a message response using Server-Sent Events (SSE)"
    )
    public SseEmitter streamMessage(
            @Parameter(description = "Chat request with streaming enabled") 
            @Valid @RequestBody ChatRequestDTO request) {
        
        logger.info("Received streaming chat request from user: {}", request.getUserId());
        
        try {
            SseEmitter emitter = chatService.streamMessage(request);
            
            emitter.onCompletion(() -> 
                logger.info("SSE stream completed for user: {}", request.getUserId()));
                
            emitter.onError(throwable -> 
                logger.error("SSE stream error for user: {}", request.getUserId(), throwable));
                
            emitter.onTimeout(() -> 
                logger.warn("SSE stream timeout for user: {}", request.getUserId()));
            
            logger.info("Started SSE stream for user: {}", request.getUserId());
            return emitter;
            
        } catch (Exception e) {
            logger.error("Error initiating streaming for user: {}", request.getUserId(), e);
            SseEmitter emitter = new SseEmitter(0L);
            emitter.completeWithError(e);
            return emitter;
        }
    }
    
    /**
     * Stream a message with conversation context using Server-Sent Events
     */
    @PostMapping(path = "/chat-with-conversation", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
        summary = "Stream chat message with conversation", 
        description = "Streams a message response with conversation context using SSE"
    )
    public SseEmitter streamMessageWithConversation(
            @Parameter(description = "Chat request with conversation context and streaming enabled") 
            @Valid @RequestBody ChatRequestDTO request) {
        
        logger.info("Received streaming conversation chat request from user: {}, conversation: {}", 
                   request.getUserId(), request.getConversationId());
        
        try {
            SseEmitter emitter = chatService.streamMessageWithConversation(request);
            
            emitter.onCompletion(() -> 
                logger.info("SSE conversation stream completed for user: {}", request.getUserId()));
                
            emitter.onError(throwable -> 
                logger.error("SSE conversation stream error for user: {}", request.getUserId(), throwable));
                
            emitter.onTimeout(() -> 
                logger.warn("SSE conversation stream timeout for user: {}", request.getUserId()));
            
            logger.info("Started SSE conversation stream for user: {}", request.getUserId());
            return emitter;
            
        } catch (Exception e) {
            logger.error("Error initiating streaming conversation for user: {}", request.getUserId(), e);
            SseEmitter emitter = new SseEmitter(0L);
            emitter.completeWithError(e);
            return emitter;
        }
    }
    
    /**
     * Health check endpoint for streaming service
     */
    @GetMapping("/health")
    @Operation(summary = "Streaming service health check")
    public String healthCheck() {
        logger.debug("Streaming health check requested");
        return "Streaming chat service is operational";
    }
}
