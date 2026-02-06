package com.example.aialibaba.config;

import lombok.Data;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.api.impl.WxCpServiceImpl;
import me.chanjar.weixin.cp.config.impl.WxCpDefaultConfigImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Enterprise WeChat (WeCom) integration using official SDK.
 */
@Configuration
@ConfigurationProperties(prefix = "wecom.bot")
@Data
public class WeComConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(WeComConfig.class);
    
    /**
     * Token for message signature verification
     */
    private String token;
    
    /**
     * AES key for message encryption/decryption (43 characters)
     */
    private String encodingAesKey;
    
    /**
     * Corporation ID
     */
    private String corpId;
    
    /**
     * Agent ID for the robot application
     */
    private Integer agentId;
    
    /**
     * App secret for API access (optional, for active message sending)
     */
    private String secret;
    
    /**
     * Check if WeCom is properly configured
     */
    public boolean isConfigured() {
        return token != null && !token.isEmpty()
                && encodingAesKey != null && !encodingAesKey.isEmpty()
                && corpId != null && !corpId.isEmpty();
    }
    
    /**
     * Create WxCpService bean when WeCom is configured
     */
    @Bean
    @ConditionalOnProperty(prefix = "wecom.bot", name = {"token", "encoding-aes-key", "corp-id"})
    public WxCpService wxCpService() {
        logger.info("Initializing WeCom (Enterprise WeChat) service with corp ID: {}", corpId);
        
        WxCpDefaultConfigImpl config = new WxCpDefaultConfigImpl();
        config.setCorpId(corpId);
        config.setToken(token);
        config.setAesKey(encodingAesKey);
        
        if (agentId != null) {
            config.setAgentId(agentId);
        }
        
        if (secret != null && !secret.isEmpty()) {
            config.setCorpSecret(secret);
        }
        
        WxCpService wxCpService = new WxCpServiceImpl();
        wxCpService.setWxCpConfigStorage(config);
        
        logger.info("WeCom service initialized successfully");
        return wxCpService;
    }
}
