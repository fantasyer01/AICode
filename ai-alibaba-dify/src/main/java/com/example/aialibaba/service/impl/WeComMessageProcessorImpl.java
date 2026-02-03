package com.example.aialibaba.service.impl;

import com.example.aialibaba.model.dto.ChatRequestDTO;
import com.example.aialibaba.model.dto.ChatResponseDTO;
import com.example.aialibaba.model.dto.wecom.WeComXmlMessage;
import com.example.aialibaba.model.dto.wecom.WeComXmlResponse;
import com.example.aialibaba.service.ChatService;
import com.example.aialibaba.service.WeComCryptService;
import com.example.aialibaba.service.WeComMessageProcessor;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementation of WeComMessageProcessor
 */
@Service
public class WeComMessageProcessorImpl implements WeComMessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(WeComMessageProcessorImpl.class);
    private final WeComCryptService weComCryptService;
    private final ChatService chatService;
    private final XmlMapper xmlMapper = new XmlMapper();

    public WeComMessageProcessorImpl(WeComCryptService weComCryptService, ChatService chatService) {
        this.weComCryptService = weComCryptService;
        this.chatService = chatService;
    }

    @Override
    public String processMessage(String msgSignature, String timeStamp, String nonce, String postData) {
        try {
            // 1. Decrypt message
            String decryptedXml = weComCryptService.decryptMsg(msgSignature, timeStamp, nonce, postData);
            logger.debug("Decrypted WeCom XML: {}", decryptedXml);

            // 2. Parse XML to DTO
            WeComXmlMessage incomingMsg = xmlMapper.readValue(decryptedXml, WeComXmlMessage.class);

            // 3. Call AI service
            ChatRequestDTO chatRequest = ChatRequestDTO.builder()
                    .message(incomingMsg.getContent())
                    .userId(incomingMsg.getFromUserName())
                    .appCode("default") // Default app code for WeCom
                    .build();

            ChatResponseDTO chatResponse = chatService.sendMessage(chatRequest);

            // 4. Build response XML
            WeComXmlResponse response = WeComXmlResponse.builder()
                    .toUserName(incomingMsg.getFromUserName())
                    .fromUserName(incomingMsg.getToUserName())
                    .createTime(System.currentTimeMillis() / 1000L)
                    .msgType("text")
                    .content(chatResponse.getAnswer())
                    .build();

            String responseXml = xmlMapper.writeValueAsString(response);
            logger.debug("Response WeCom XML: {}", responseXml);

            // 5. Encrypt response XML
            return weComCryptService.encryptMsg(responseXml, timeStamp, nonce);

        } catch (Exception e) {
            logger.error("Error processing WeCom message", e);
            return ""; // Return empty to acknowledge but signify error
        }
    }
}
