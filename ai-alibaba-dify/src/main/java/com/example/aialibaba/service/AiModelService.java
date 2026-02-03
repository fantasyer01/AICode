package com.example.aialibaba.service;

import com.example.aialibaba.model.dto.ChatRequestDTO;
import com.example.aialibaba.model.dto.ChatResponseDTO;

/**
 * Interface for AI Model interactions
 */
public interface AiModelService {
    /**
     * Send message to the AI model
     */
    ChatResponseDTO sendMessage(ChatRequestDTO request);
}
