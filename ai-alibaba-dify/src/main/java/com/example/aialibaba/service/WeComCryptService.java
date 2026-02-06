package com.example.aialibaba.service;

/**
 * Service for WeCom message encryption, decryption and signature verification.
 * Uses the official WeChat Work SDK for cryptographic operations.
 */
public interface WeComCryptService {
    
    /**
     * Check if the WeCom service is properly configured
     * @return true if configured, false otherwise
     */
    boolean isConfigured();
    
    /**
     * Verify URL for WeCom callback configuration
     * @param msgSignature the signature from WeChat
     * @param timestamp the timestamp from WeChat
     * @param nonce the nonce from WeChat
     * @param echoStr the echo string to decrypt and return
     * @return the decrypted echo string
     */
    String verifyUrl(String msgSignature, String timestamp, String nonce, String echoStr);
    
    /**
     * Decrypt incoming message from WeChat
     * @param msgSignature the signature from WeChat
     * @param timestamp the timestamp from WeChat
     * @param nonce the nonce from WeChat
     * @param postData the encrypted POST data (XML format from WeChat)
     * @return the decrypted message content (JSON for intelligent robot)
     */
    String decryptMsg(String msgSignature, String timestamp, String nonce, String postData);
    
    /**
     * Encrypt outgoing message to WeChat
     * @param replyMsg the message to encrypt (JSON format)
     * @param timestamp the timestamp to use
     * @param nonce the nonce to use
     * @return the encrypted response (XML format)
     */
    String encryptMsg(String replyMsg, String timestamp, String nonce);
}
