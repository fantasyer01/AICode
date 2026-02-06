package com.example.aialibaba.service;

import com.example.aialibaba.model.dto.wecom.request.WeComCallbackMessage;

/**
 * Service for orchestrating WeChat Work message processing.
 * Handles message routing, deduplication, and response generation.
 */
public interface WeComMessageService {
    
    /**
     * Process an incoming callback message from WeChat Work
     * @param msgSignature the message signature
     * @param timestamp the timestamp
     * @param nonce the nonce
     * @param postData the encrypted POST data
     * @return the encrypted response to send back
     */
    String processCallback(String msgSignature, String timestamp, String nonce, String postData);
    
    /**
     * Check if a message has already been processed (for deduplication)
     * @param msgId the message ID
     * @return true if already processed, false otherwise
     */
    boolean isDuplicateMessage(String msgId);
    
    /**
     * Mark a message as processed
     * @param msgId the message ID
     */
    void markMessageProcessed(String msgId);
}
