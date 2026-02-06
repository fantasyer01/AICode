package com.example.aialibaba.handler.wecom;

import com.example.aialibaba.model.dto.wecom.WeComMessageType;
import com.example.aialibaba.model.dto.wecom.request.WeComCallbackMessage;
import com.example.aialibaba.model.dto.wecom.response.WeComTextResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handler for image messages from WeChat Work intelligent robot.
 */
@Component
public class ImageMessageHandler implements WeComMessageHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageMessageHandler.class);
    
    @Override
    public boolean supports(String msgType) {
        return WeComMessageType.IMAGE.getValue().equalsIgnoreCase(msgType);
    }
    
    @Override
    public Object handle(WeComCallbackMessage message) {
        String userId = message.getUserId();
        
        logger.info("Processing image message from user: {}", userId);
        
        // For now, return a message indicating image processing is not yet supported
        // In the future, this could be extended to process images via vision models
        return WeComTextResponse.of("I received your image, but image processing is not yet supported. Please send me a text message instead.");
    }
    
    @Override
    public int getPriority() {
        return 20;
    }
}
