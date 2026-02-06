package com.example.aialibaba.handler.wecom;

import com.example.aialibaba.model.dto.wecom.request.WeComCallbackMessage;
import com.example.aialibaba.model.dto.wecom.response.WeComTextResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Default fallback handler for unrecognized message types.
 */
@Component
public class DefaultMessageHandler implements WeComMessageHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultMessageHandler.class);
    
    @Override
    public boolean supports(String msgType) {
        // This handler supports all message types as a fallback
        return true;
    }
    
    @Override
    public Object handle(WeComCallbackMessage message) {
        String msgType = message.getMsgType();
        String userId = message.getUserId();
        
        logger.info("Default handler processing message type: {} from user: {}", msgType, userId);
        
        return WeComTextResponse.of("I received your message, but I'm not sure how to process this type of content. Please try sending a text message.");
    }
    
    @Override
    public int getPriority() {
        return Integer.MAX_VALUE; // Lowest priority, used as fallback
    }
}
