package com.example.aialibaba.service;

import com.example.aialibaba.model.dto.wecom.response.WeComStreamResponse;
import com.example.aialibaba.model.dto.wecom.response.WeComTextResponse;

/**
 * Service for sending async responses to WeChat Work via response_url.
 * Used for long-running operations that exceed the 5-second timeout.
 */
public interface WeComResponseService {
    
    /**
     * Send a text response to the given response URL
     * @param responseUrl the URL to send the response to
     * @param response the text response
     * @return true if successful, false otherwise
     */
    boolean sendTextResponse(String responseUrl, WeComTextResponse response);
    
    /**
     * Send a streaming response chunk to the given response URL
     * @param responseUrl the URL to send the response to
     * @param response the stream response chunk
     * @return true if successful, false otherwise
     */
    boolean sendStreamResponse(String responseUrl, WeComStreamResponse response);
    
    /**
     * Send a raw JSON response to the given response URL
     * @param responseUrl the URL to send the response to
     * @param jsonResponse the raw JSON response
     * @return true if successful, false otherwise
     */
    boolean sendRawResponse(String responseUrl, String jsonResponse);
}
