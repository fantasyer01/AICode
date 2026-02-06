package com.example.aialibaba.handler.wecom;

import com.example.aialibaba.model.dto.wecom.WeComMessageType;
import com.example.aialibaba.model.dto.wecom.request.WeComCallbackMessage;
import com.example.aialibaba.model.dto.wecom.response.WeComTextResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handler for event messages from WeChat Work intelligent robot.
 * Handles events like enter_chat, etc.
 */
@Component
public class EventMessageHandler implements WeComMessageHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(EventMessageHandler.class);
    
    @Override
    public boolean supports(String msgType) {
        return WeComMessageType.EVENT.getValue().equalsIgnoreCase(msgType);
    }
    
    @Override
    public Object handle(WeComCallbackMessage message) {
        String eventType = message.getEvent() != null ? message.getEvent().getEventType() : "unknown";
        String userId = message.getUserId();
        
        logger.info("Processing event message from user: {}, event type: {}", userId, eventType);
        
        switch (eventType.toLowerCase()) {
            case "enter_chat":
                return handleEnterChat(message);
            default:
                logger.info("Unhandled event type: {}", eventType);
                return null;
        }
    }
    
    private Object handleEnterChat(WeComCallbackMessage message) {
        String userName = message.getUserName();
        String greeting = userName != null 
                ? String.format("Hello %s! How can I help you today?", userName)
                : "Hello! How can I help you today?";
        
        return WeComTextResponse.of(greeting);
    }
    
    @Override
    public int getPriority() {
        return 5;
    }
}
