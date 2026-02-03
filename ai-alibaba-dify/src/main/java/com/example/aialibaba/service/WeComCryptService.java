package com.example.aialibaba.service;

/**
 * Service for WeCom message encryption, decryption and signature verification
 */
public interface WeComCryptService {
    /**
     * Verify URL for WeCom callback
     */
    String verifyUrl(String msgSignature, String timeStamp, String nonce, String echoStr);

    /**
     * Decrypt incoming message
     */
    String decryptMsg(String msgSignature, String timeStamp, String nonce, String postData);

    /**
     * Encrypt outgoing message
     */
    String encryptMsg(String replyMsg, String timeStamp, String nonce);
}
