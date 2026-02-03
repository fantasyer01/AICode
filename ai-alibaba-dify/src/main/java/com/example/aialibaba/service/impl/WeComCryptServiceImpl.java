package com.example.aialibaba.service.impl;

import com.example.aialibaba.config.WeComConfig;
import com.example.aialibaba.exception.ServiceException;
import com.example.aialibaba.service.WeComCryptService;
import com.example.aialibaba.util.WXBizMsgCrypt;
import org.springframework.stereotype.Service;

/**
 * Implementation of WeComCryptService using WXBizMsgCrypt
 */
@Service
public class WeComCryptServiceImpl implements WeComCryptService {

    private final WXBizMsgCrypt wxcpt;

    public WeComCryptServiceImpl(WeComConfig weComConfig) {
        // Check if WeCom configuration is provided
        if (weComConfig.getToken() == null || weComConfig.getToken().isEmpty() ||
            weComConfig.getEncodingAesKey() == null || weComConfig.getEncodingAesKey().isEmpty() ||
            weComConfig.getCorpId() == null || weComConfig.getCorpId().isEmpty()) {
            this.wxcpt = null; // WeCom not configured, service will throw exceptions when used
            return;
        }
        
        try {
            this.wxcpt = new WXBizMsgCrypt(weComConfig.getToken(), weComConfig.getEncodingAesKey(), weComConfig.getCorpId());
        } catch (Exception e) {
            throw new ServiceException("WECOM_CRYPT_INIT_ERROR", "Failed to initialize WeCom crypt service", e);
        }
    }

    @Override
    public String verifyUrl(String msgSignature, String timeStamp, String nonce, String echoStr) {
        if (wxcpt == null) {
            throw new ServiceException("WECOM_NOT_CONFIGURED", "WeCom service is not configured. Please set wecom.bot.token, wecom.bot.encoding-aes-key, and wecom.bot.corp-id in configuration.");
        }
        try {
            return wxcpt.VerifyURL(msgSignature, timeStamp, nonce, echoStr);
        } catch (Exception e) {
            throw new ServiceException("WECOM_VERIFY_URL_ERROR", "Failed to verify WeCom URL", e);
        }
    }

    @Override
    public String decryptMsg(String msgSignature, String timeStamp, String nonce, String postData) {
        if (wxcpt == null) {
            throw new ServiceException("WECOM_NOT_CONFIGURED", "WeCom service is not configured. Please set wecom.bot.token, wecom.bot.encoding-aes-key, and wecom.bot.corp-id in configuration.");
        }
        try {
            return wxcpt.DecryptMsg(msgSignature, timeStamp, nonce, postData);
        } catch (Exception e) {
            throw new ServiceException("WECOM_DECRYPT_ERROR", "Failed to decrypt WeCom message", e);
        }
    }

    @Override
    public String encryptMsg(String replyMsg, String timeStamp, String nonce) {
        if (wxcpt == null) {
            throw new ServiceException("WECOM_NOT_CONFIGURED", "WeCom service is not configured. Please set wecom.bot.token, wecom.bot.encoding-aes-key, and wecom.bot.corp-id in configuration.");
        }
        try {
            return wxcpt.EncryptMsg(replyMsg, timeStamp, nonce);
        } catch (Exception e) {
            throw new ServiceException("WECOM_ENCRYPT_ERROR", "Failed to encrypt WeCom message", e);
        }
    }
}
