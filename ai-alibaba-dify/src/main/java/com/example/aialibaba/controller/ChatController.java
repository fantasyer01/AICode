package com.example.aialibaba.controller;

import com.example.aialibaba.model.dto.ChatRequestDTO;
import com.example.aialibaba.model.dto.ChatResponseDTO;
import com.example.aialibaba.service.ChatService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for chat operations with Dify and AI integration
 */
@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "Chat Interface", description = "Operations for interacting with AI models and Dify")
public class ChatController {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    
    private final ChatService chatService;
    
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }
    
    /**
     * Send a message and get response (blocking mode)
     * For streaming responses, use POST /api/v1/stream/chat instead
     */
    @PostMapping("/send")
    @Operation(
        summary = "Send chat message (blocking)", 
        description = "Sends a message to the configured AI service and waits for complete response. " +
                     "For streaming responses, use POST /api/v1/stream/chat"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful response"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ChatResponseDTO> sendMessage(
            @Valid @RequestBody ChatRequestDTO request) {
        
        logger.info("Received chat request from user: {}", request.getUserId());
        
        try {
            ChatResponseDTO response = chatService.sendMessage(request);
            logger.info("Successfully processed chat request for user: {}", request.getUserId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing chat request for user: {}", request.getUserId(), e);
            throw e; // Let global exception handler handle it
        }
    }
    
    /**
     * Send a message with conversation context
     */
    @PostMapping("/send-with-conversation")
    public ResponseEntity<ChatResponseDTO> sendMessageWithConversation(
            @Valid @RequestBody ChatRequestDTO request) {
        
        logger.info("Received conversation chat request from user: {}, conversation: {}", 
                   request.getUserId(), request.getConversationId());
        
        try {
            ChatResponseDTO response = chatService.sendMessageWithConversation(request);
            logger.info("Successfully processed conversation chat request for user: {}", 
                       request.getUserId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing conversation chat request for user: {}", 
                        request.getUserId(), e);
            throw e; // Let global exception handler handle it
        }
    }
}