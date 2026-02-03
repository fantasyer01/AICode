package com.example.aialibaba.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Enterprise WeChat (WeCom)
 */
@Configuration
@ConfigurationProperties(prefix = "wecom.bot")
@Data
public class WeComConfig {
    private String token;
    private String encodingAesKey;
    private String corpId;
}
