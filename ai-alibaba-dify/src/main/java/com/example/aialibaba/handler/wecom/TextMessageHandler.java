package com.example.aialibaba.handler.wecom;

import com.example.aialibaba.model.dto.ChatRequestDTO;
import com.example.aialibaba.model.dto.ChatResponseDTO;
import com.example.aialibaba.model.dto.wecom.WeComMessageType;
import com.example.aialibaba.model.dto.wecom.request.WeComCallbackMessage;
import com.example.aialibaba.model.dto.wecom.response.WeComTextResponse;
import com.example.aialibaba.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handler for text messages from WeChat Work intelligent robot.
 */
@Component
public class TextMessageHandler implements WeComMessageHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(TextMessageHandler.class);
    
    private final ChatService chatService;
    
    public TextMessageHandler(ChatService chatService) {
        this.chatService = chatService;
    }
    
    @Override
    public boolean supports(String msgType) {
        return WeComMessageType.TEXT.getValue().equalsIgnoreCase(msgType);
    }
    
    @Override
    public Object handle(WeComCallbackMessage message) {
        String textContent = message.getTextContent();
        String userId = message.getUserId();
        
        logger.info("Processing text message from user: {}, content: {}", userId, textContent);
        
        if (textContent == null || textContent.isBlank()) {
            return WeComTextResponse.of("I didn't receive any text content. Please try again.");
        }
        
        try {
            ChatRequestDTO chatRequest = ChatRequestDTO.builder()
                    .message(textContent)
                    .userId(userId)
                    .serviceType("model")
                    .modelCode("qwen-plus")
                    .build();
            
            ChatResponseDTO chatResponse = chatService.sendMessage(chatRequest);
            
            return WeComTextResponse.of(chatResponse.getAnswer());
            
        } catch (Exception e) {
            logger.error("Error processing text message", e);
            return WeComTextResponse.of("Sorry, I encountered an error processing your message. Please try again later.");
        }
    }
    
    @Override
    public int getPriority() {
        return 10;
    }
}
