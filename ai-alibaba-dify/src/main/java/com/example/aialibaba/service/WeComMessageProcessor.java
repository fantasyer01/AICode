package com.example.aialibaba.service;

import com.example.aialibaba.model.dto.wecom.WeComXmlMessage;

/**
 * Interface for processing WeCom messages
 */
public interface WeComMessageProcessor {
    /**
     * Process incoming WeCom message and return response XML
     */
    String processMessage(String msgSignature, String timeStamp, String nonce, String postData);
}
