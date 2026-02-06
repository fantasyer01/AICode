package com.example.aialibaba.service.impl;

import com.example.aialibaba.config.WeComConfig;
import com.example.aialibaba.exception.ServiceException;
import com.example.aialibaba.service.WeComCryptService;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.bean.message.WxCpXmlMessage;
import me.chanjar.weixin.cp.bean.message.WxCpXmlOutTextMessage;
import me.chanjar.weixin.cp.util.crypto.WxCpCryptUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of WeComCryptService using the official WeChat Work SDK (weixin-java-cp).
 */
@Service
public class WeComCryptServiceImpl implements WeComCryptService {
    
    private static final Logger logger = LoggerFactory.getLogger(WeComCryptServiceImpl.class);
    
    private final WeComConfig weComConfig;
    private final WxCpService wxCpService;
    private final WxCpCryptUtil wxCpCryptUtil;
    
    @Autowired(required = false)
    public WeComCryptServiceImpl(WeComConfig weComConfig, 
                                  @Autowired(required = false) WxCpService wxCpService) {
        this.weComConfig = weComConfig;
        this.wxCpService = wxCpService;
        
        if (wxCpService != null && weComConfig.isConfigured()) {
            this.wxCpCryptUtil = new WxCpCryptUtil(wxCpService.getWxCpConfigStorage());
            logger.info("WeCom crypt service initialized with official SDK");
        } else {
            this.wxCpCryptUtil = null;
            logger.warn("WeCom crypt service not initialized - configuration missing");
        }
    }
    
    @Override
    public boolean isConfigured() {
        return wxCpCryptUtil != null && weComConfig.isConfigured();
    }
    
    @Override
    public String verifyUrl(String msgSignature, String timestamp, String nonce, String echoStr) {
        checkConfigured();
        
        try {
            logger.debug("Verifying WeCom URL - timestamp: {}, nonce: {}", timestamp, nonce);
            
            // Use the SDK to decrypt the echostr
            String result = wxCpCryptUtil.decrypt(echoStr);
            
            logger.info("WeCom URL verification successful");
            return result;
            
        } catch (Exception e) {
            logger.error("Failed to verify WeCom URL", e);
            throw new ServiceException("WECOM_VERIFY_URL_ERROR", "Failed to verify WeCom URL: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String decryptMsg(String msgSignature, String timestamp, String nonce, String postData) {
        checkConfigured();
        
        try {
            logger.debug("Decrypting WeCom message - timestamp: {}, nonce: {}", timestamp, nonce);
            
            // Parse the encrypted XML message
            WxCpXmlMessage inMessage = WxCpXmlMessage.fromEncryptedXml(
                    postData, 
                    wxCpService.getWxCpConfigStorage(), 
                    timestamp, 
                    nonce, 
                    msgSignature
            );
            
            // For intelligent robot, the content is JSON stored in the Content field
            String content = inMessage.getContent();
            
            logger.debug("Successfully decrypted WeCom message, content length: {}", 
                    content != null ? content.length() : 0);
            
            return content;
            
        } catch (Exception e) {
            logger.error("Failed to decrypt WeCom message", e);
            throw new ServiceException("WECOM_DECRYPT_ERROR", "Failed to decrypt WeCom message: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String encryptMsg(String replyMsg, String timestamp, String nonce) {
        checkConfigured();
        
        try {
            logger.debug("Encrypting WeCom response - timestamp: {}, nonce: {}", timestamp, nonce);
            
            // Encrypt the reply message
            String encryptedXml = wxCpCryptUtil.encrypt(replyMsg);
            
            logger.debug("Successfully encrypted WeCom response");
            return encryptedXml;
            
        } catch (Exception e) {
            logger.error("Failed to encrypt WeCom message", e);
            throw new ServiceException("WECOM_ENCRYPT_ERROR", "Failed to encrypt WeCom message: " + e.getMessage(), e);
        }
    }
    
    private void checkConfigured() {
        if (!isConfigured()) {
            throw new ServiceException("WECOM_NOT_CONFIGURED", 
                    "WeCom service is not configured. Please set wecom.bot.token, wecom.bot.encoding-aes-key, and wecom.bot.corp-id in configuration.");
        }
    }
}
