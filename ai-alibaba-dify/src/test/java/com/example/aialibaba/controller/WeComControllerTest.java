package com.example.aialibaba.controller;

import com.example.aialibaba.handler.wecom.WeComMessageHandler;
import com.example.aialibaba.model.dto.wecom.request.WeComCallbackMessage;
import com.example.aialibaba.model.dto.wecom.response.WeComTextResponse;
import com.example.aialibaba.service.WeComCryptService;
import com.example.aialibaba.service.WeComMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeComController.class)
@DisplayName("WeComController Tests")
class WeComControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WeComCryptService weComCryptService;

    @MockBean
    private WeComMessageService weComMessageService;

    @MockBean
    private List<WeComMessageHandler> messageHandlers;

    @BeforeEach
    void setUp() {
        when(weComCryptService.isConfigured()).thenReturn(true);
    }

    @Test
    @DisplayName("URL verification should return decrypted echostr")
    void testVerifyUrl_Success() throws Exception {
        String expectedEchoStr = "decrypted_echo_string";
        when(weComCryptService.verifyUrl(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(expectedEchoStr);

        mockMvc.perform(get("/api/wecom/callback")
                        .param("msg_signature", "test_signature")
                        .param("timestamp", "1234567890")
                        .param("nonce", "test_nonce")
                        .param("echostr", "encrypted_echo"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedEchoStr));
    }

    @Test
    @DisplayName("Message callback should process and return encrypted response")
    void testReceiveMessage_Success() throws Exception {
        String encryptedResponse = "<xml><Encrypt>encrypted_response</Encrypt></xml>";
        when(weComMessageService.processCallback(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(encryptedResponse);

        String postData = "<xml><Encrypt>encrypted_message</Encrypt></xml>";

        mockMvc.perform(post("/api/wecom/callback")
                        .param("msg_signature", "test_signature")
                        .param("timestamp", "1234567890")
                        .param("nonce", "test_nonce")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(postData))
                .andExpect(status().isOk())
                .andExpect(content().string(encryptedResponse));
    }

    @Test
    @DisplayName("Health check should return configured status")
    void testHealthCheck_Configured() throws Exception {
        when(weComCryptService.isConfigured()).thenReturn(true);

        mockMvc.perform(get("/api/wecom/health"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":\"ok\",\"configured\":true}"));
    }

    @Test
    @DisplayName("Health check should return not configured status")
    void testHealthCheck_NotConfigured() throws Exception {
        when(weComCryptService.isConfigured()).thenReturn(false);

        mockMvc.perform(get("/api/wecom/health"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":\"ok\",\"configured\":false}"));
    }

    @Test
    @DisplayName("Test endpoint should process text message and return response")
    void testTestMessage_TextMessage() throws Exception {
        WeComCallbackMessage testMessage = new WeComCallbackMessage();
        testMessage.setMsgType("text");
        testMessage.setMsgId("test_msg_123");
        
        WeComCallbackMessage.TextContent textContent = new WeComCallbackMessage.TextContent();
        textContent.setContent("Hello test");
        testMessage.setText(textContent);
        
        WeComCallbackMessage.FromUser fromUser = new WeComCallbackMessage.FromUser();
        fromUser.setUserId("test_user");
        testMessage.setFrom(fromUser);

        String requestJson = objectMapper.writeValueAsString(testMessage);

        mockMvc.perform(post("/api/wecom/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msgtype").value("text"));
    }
}
