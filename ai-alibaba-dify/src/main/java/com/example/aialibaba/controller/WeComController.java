package com.example.aialibaba.controller;

import com.example.aialibaba.handler.wecom.WeComMessageHandler;
import com.example.aialibaba.model.dto.wecom.request.WeComCallbackMessage;
import com.example.aialibaba.model.dto.wecom.response.WeComTextResponse;
import com.example.aialibaba.service.WeComCryptService;
import com.example.aialibaba.service.WeComMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

/**
 * Controller for Enterprise WeChat (WeCom) intelligent robot callback integration.
 * Handles URL verification and message callbacks from WeChat Work.
 * 
 * @see <a href="https://developer.work.weixin.qq.com/document/path/101039">WeCom Bot Documentation</a>
 */
@RestController
@RequestMapping("/api/wecom")
@Tag(name = "WeCom Integration", description = "Endpoints for Enterprise WeChat intelligent robot integration")
public class WeComController {

    private static final Logger logger = LoggerFactory.getLogger(WeComController.class);
    
    private final WeComCryptService weComCryptService;
    private final WeComMessageService weComMessageService;
    private final List<WeComMessageHandler> messageHandlers;

    public WeComController(WeComCryptService weComCryptService, 
                           WeComMessageService weComMessageService,
                           List<WeComMessageHandler> messageHandlers) {
        this.weComCryptService = weComCryptService;
        this.weComMessageService = weComMessageService;
        this.messageHandlers = messageHandlers.stream()
                .sorted(Comparator.comparingInt(WeComMessageHandler::getPriority))
                .toList();
    }

    /**
     * URL verification endpoint for WeCom callback configuration (GET request).
     * WeChat Work sends a GET request to verify the callback URL is valid.
     * 
     * @param msgSignature Message signature for verification
     * @param timeStamp Timestamp from WeChat
     * @param nonce Random string from WeChat
     * @param echoStr Encrypted echo string to decrypt and return
     * @return Decrypted echo string if verification successful
     */
    @GetMapping("/callback")
    @Operation(summary = "Verify WeCom callback URL", 
               description = "Endpoint for WeChat Work to verify the callback URL configuration")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Verification successful"),
        @ApiResponse(responseCode = "500", description = "Verification failed")
    })
    public String verifyUrl(
            @Parameter(description = "Message signature") 
            @RequestParam("msg_signature") String msgSignature,
            @Parameter(description = "Timestamp") 
            @RequestParam("timestamp") String timeStamp,
            @Parameter(description = "Nonce") 
            @RequestParam("nonce") String nonce,
            @Parameter(description = "Encrypted echo string") 
            @RequestParam("echostr") String echoStr) {
        
        logger.info("Received WeCom URL verification request - timestamp: {}, nonce: {}", 
                timeStamp, nonce);
        
        return weComCryptService.verifyUrl(msgSignature, timeStamp, nonce, echoStr);
    }

    /**
     * Message callback endpoint for receiving messages from WeCom (POST request).
     * WeChat Work sends encrypted messages via POST when users interact with the bot.
     * 
     * @param msgSignature Message signature for verification
     * @param timeStamp Timestamp from WeChat
     * @param nonce Random string from WeChat
     * @param postData Encrypted message data (XML format containing JSON content)
     * @return Encrypted response (XML format containing JSON content)
     */
    @PostMapping(value = "/callback", 
                 consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE},
                 produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "Receive WeCom messages", 
               description = "Endpoint for receiving and processing messages from WeChat Work intelligent robot")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Message processed successfully"),
        @ApiResponse(responseCode = "500", description = "Message processing failed")
    })
    public String receiveMessage(
            @Parameter(description = "Message signature") 
            @RequestParam("msg_signature") String msgSignature,
            @Parameter(description = "Timestamp") 
            @RequestParam("timestamp") String timeStamp,
            @Parameter(description = "Nonce") 
            @RequestParam("nonce") String nonce,
            @Parameter(description = "Encrypted message data") 
            @RequestBody String postData) {
        
        logger.info("Received message from WeCom - timestamp: {}, nonce: {}, data length: {}", 
                timeStamp, nonce, postData != null ? postData.length() : 0);
        
        return weComMessageService.processCallback(msgSignature, timeStamp, nonce, postData);
    }
    
    /**
     * Health check endpoint for WeCom integration
     */
    @GetMapping("/health")
    @Operation(summary = "Check WeCom integration health", 
               description = "Returns the configuration status of WeCom integration")
    public String healthCheck() {
        boolean configured = weComCryptService.isConfigured();
        return configured 
                ? "{\"status\":\"ok\",\"configured\":true}" 
                : "{\"status\":\"ok\",\"configured\":false,\"message\":\"WeCom not configured\"}";
    }
    
    /**
     * Test endpoint for frontend integration testing.
     * Accepts unencrypted JSON messages directly and processes them through the handlers.
     * This endpoint is for development/testing only.
     * 
     * @param message The unencrypted message in WeComCallbackMessage format
     * @return The response object (text, markdown, etc.)
     */
    @PostMapping(value = "/test", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test WeCom message processing", 
               description = "Test endpoint for processing unencrypted WeCom messages (development only)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Message processed successfully"),
        @ApiResponse(responseCode = "500", description = "Message processing failed")
    })
    public Object testMessage(@RequestBody WeComCallbackMessage message) {
        logger.info("Received test message - msgType: {}, from: {}", 
                message.getMsgType(), message.getUserId());
        
        String msgType = message.getMsgType();
        
        for (WeComMessageHandler handler : messageHandlers) {
            if (handler.supports(msgType)) {
                logger.debug("Routing test message to handler: {}", handler.getClass().getSimpleName());
                Object response = handler.handle(message);
                if (response != null) {
                    return response;
                }
            }
        }
        
        // Default fallback
        return WeComTextResponse.of("I received your message, but couldn't process it.");
    }
}
