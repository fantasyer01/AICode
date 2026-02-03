package com.example.aialibaba.controller;

import com.example.aialibaba.service.WeComCryptService;
import com.example.aialibaba.service.WeComMessageProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Enterprise WeChat (WeCom) callback integration
 */
@RestController
@RequestMapping("/api/wecom")
@Tag(name = "WeCom Integration", description = "Endpoints for Enterprise WeChat robot integration")
public class WeComController {

    private static final Logger logger = LoggerFactory.getLogger(WeComController.class);
    private final WeComCryptService weComCryptService;
    private final WeComMessageProcessor messageProcessor;

    public WeComController(WeComCryptService weComCryptService, WeComMessageProcessor messageProcessor) {
        this.weComCryptService = weComCryptService;
        this.messageProcessor = messageProcessor;
    }

    /**
     * URL verification for WeCom callback (GET)
     */
    @GetMapping("/callback")
    public String verifyUrl(
            @RequestParam("msg_signature") String msgSignature,
            @RequestParam("timestamp") String timeStamp,
            @RequestParam("nonce") String nonce,
            @RequestParam("echostr") String echoStr) {
        
        logger.info("Received WeCom URL verification request");
        return weComCryptService.verifyUrl(msgSignature, timeStamp, nonce, echoStr);
    }

    /**
     * Receive messages from WeCom (POST)
     */
    @PostMapping("/callback")
    public String receiveMessage(
            @RequestParam("msg_signature") String msgSignature,
            @RequestParam("timestamp") String timeStamp,
            @RequestParam("nonce") String nonce,
            @RequestBody String postData) {
        
        logger.info("Received message from WeCom");
        return messageProcessor.processMessage(msgSignature, timeStamp, nonce, postData);
    }
}
