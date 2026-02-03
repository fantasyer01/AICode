package com.example.aialibaba.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for chat response from Dify
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatResponseDTO implements Serializable {
    
    private String messageId;
    private String answer;
    private String conversationId;
    private Long createdAt;
    private String status;
    
    // Error information
    private String errorCode;
    private String errorMessage;
    
    // Usage statistics
    private Usage usage;
    
    /**
     * Usage statistics for the API call
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage implements Serializable {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }

    // Static factory methods for success and error responses
    public static ChatResponseDTO success(String answer, String conversationId) {
        return ChatResponseDTO.builder()
                .answer(answer)
                .conversationId(conversationId)
                .status("success")
                .createdAt(System.currentTimeMillis())
                .build();
    }
    
    public static ChatResponseDTO error(String errorCode, String errorMessage) {
        return ChatResponseDTO.builder()
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .status("error")
                .createdAt(System.currentTimeMillis())
                .build();
    }
}