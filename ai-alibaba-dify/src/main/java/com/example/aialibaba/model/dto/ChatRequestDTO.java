package com.example.aialibaba.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for chat request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chat Request Information")
public class ChatRequestDTO {
    
    @NotBlank(message = "Message content cannot be empty")
    @Schema(description = "The message content to send", example = "Hello, how are you?")
    private String message;
    
    @NotNull(message = "User ID cannot be null")
    @Schema(description = "Unique identifier for the user", example = "user_123")
    private String userId;
    
    @Schema(description = "Conversation ID for continuing a chat", example = "conv_456")
    private String conversationId;
    
    // Dify application specific parameters
    @Schema(description = "Application code defined in config", example = "customer-service")
    private String appCode; 
    
    // Generic inputs for Dify variables
    @Builder.Default
    @Schema(description = "Dynamic input variables for Dify")
    private Map<String, Object> inputs = new HashMap<>();
    
    // AI Model type selection: "dify" or "spring-ai"
    @Builder.Default
    @Schema(description = "Service type to use: 'dify' or 'spring-ai'", defaultValue = "dify")
    private String serviceType = "dify";
    
    // Specific AI Model selection
    @Schema(description = "Selected AI model code", example = "qwen-plus")
    private String modelCode;
    
    @Schema(description = "AI model provider", example = "dashscope")
    private String modelProvider;
}