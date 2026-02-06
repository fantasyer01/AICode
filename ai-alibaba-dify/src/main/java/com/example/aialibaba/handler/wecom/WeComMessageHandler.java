package com.example.aialibaba.handler.wecom;

import com.example.aialibaba.model.dto.wecom.request.WeComCallbackMessage;

/**
 * Strategy interface for handling different types of WeChat Work messages.
 */
public interface WeComMessageHandler {
    
    /**
     * Check if this handler supports the given message type
     * @param msgType the message type string
     * @return true if this handler can process the message type
     */
    boolean supports(String msgType);
    
    /**
     * Handle the incoming message and return a response
     * @param message the incoming callback message
     * @return the response object (can be text, markdown, stream, etc.)
     */
    Object handle(WeComCallbackMessage message);
    
    /**
     * Get the handler priority (lower values = higher priority)
     * @return the priority value
     */
    default int getPriority() {
        return 100;
    }
}
