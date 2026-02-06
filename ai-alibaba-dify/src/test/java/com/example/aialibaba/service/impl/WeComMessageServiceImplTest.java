package com.example.aialibaba.service.impl;

import com.example.aialibaba.handler.wecom.WeComMessageHandler;
import com.example.aialibaba.model.dto.wecom.request.WeComCallbackMessage;
import com.example.aialibaba.model.dto.wecom.response.WeComTextResponse;
import com.example.aialibaba.service.WeComCryptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeComMessageService Tests")
class WeComMessageServiceImplTest {

    @Mock
    private WeComCryptService weComCryptService;

    @Mock
    private WeComMessageHandler textHandler;

    @Mock
    private WeComMessageHandler defaultHandler;

    private WeComMessageServiceImpl weComMessageService;

    @BeforeEach
    void setUp() {
        // Setup handler priorities
        when(textHandler.getPriority()).thenReturn(10);
        when(defaultHandler.getPriority()).thenReturn(Integer.MAX_VALUE);
        
        weComMessageService = new WeComMessageServiceImpl(
                weComCryptService, 
                List.of(textHandler, defaultHandler)
        );
    }

    @Test
    @DisplayName("processCallback should decrypt, route, and encrypt response")
    void testProcessCallback_Success() {
        String decryptedJson = "{\"msgtype\":\"text\",\"msgid\":\"msg123\",\"text\":{\"content\":\"Hello\"},\"from\":{\"UserId\":\"user1\"}}";
        String encryptedResponse = "<xml><Encrypt>encrypted</Encrypt></xml>";
        
        when(weComCryptService.decryptMsg(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(decryptedJson);
        when(textHandler.supports("text")).thenReturn(true);
        when(textHandler.handle(any(WeComCallbackMessage.class)))
                .thenReturn(WeComTextResponse.of("Hello response"));
        when(weComCryptService.encryptMsg(anyString(), anyString(), anyString()))
                .thenReturn(encryptedResponse);

        String result = weComMessageService.processCallback("sig", "ts", "nonce", "<xml></xml>");

        assertEquals(encryptedResponse, result);
        verify(weComCryptService).decryptMsg(anyString(), anyString(), anyString(), anyString());
        verify(textHandler).supports("text");
        verify(textHandler).handle(any(WeComCallbackMessage.class));
        verify(weComCryptService).encryptMsg(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("processCallback should handle duplicate messages")
    void testProcessCallback_DuplicateMessage() {
        String decryptedJson = "{\"msgtype\":\"text\",\"msgid\":\"duplicate123\",\"text\":{\"content\":\"Hello\"},\"from\":{\"UserId\":\"user1\"}}";
        
        when(weComCryptService.decryptMsg(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(decryptedJson);
        when(textHandler.supports("text")).thenReturn(true);
        when(textHandler.handle(any(WeComCallbackMessage.class)))
                .thenReturn(WeComTextResponse.of("Response"));
        when(weComCryptService.encryptMsg(anyString(), anyString(), anyString()))
                .thenReturn("<xml></xml>");

        // First call - should process
        weComMessageService.processCallback("sig", "ts", "nonce", "<xml></xml>");
        
        // Second call with same msgid - should be detected as duplicate
        String result = weComMessageService.processCallback("sig", "ts", "nonce", "<xml></xml>");
        
        // Duplicate should return empty string
        assertEquals("", result);
    }

    @Test
    @DisplayName("isDuplicateMessage should return false for new messages")
    void testIsDuplicateMessage_NewMessage() {
        assertFalse(weComMessageService.isDuplicateMessage("new_msg_id"));
    }

    @Test
    @DisplayName("isDuplicateMessage should return false for null msgId")
    void testIsDuplicateMessage_NullMsgId() {
        assertFalse(weComMessageService.isDuplicateMessage(null));
    }

    @Test
    @DisplayName("markMessageProcessed should track message")
    void testMarkMessageProcessed() {
        String msgId = "track_msg_123";
        
        assertFalse(weComMessageService.isDuplicateMessage(msgId));
        
        weComMessageService.markMessageProcessed(msgId);
        
        assertTrue(weComMessageService.isDuplicateMessage(msgId));
    }

    @Test
    @DisplayName("processCallback should use fallback handler for unknown message type")
    void testProcessCallback_FallbackHandler() {
        String decryptedJson = "{\"msgtype\":\"unknown\",\"msgid\":\"msg456\",\"from\":{\"UserId\":\"user1\"}}";
        
        when(weComCryptService.decryptMsg(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(decryptedJson);
        when(textHandler.supports("unknown")).thenReturn(false);
        when(defaultHandler.supports("unknown")).thenReturn(true);
        when(defaultHandler.handle(any(WeComCallbackMessage.class)))
                .thenReturn(WeComTextResponse.of("Unknown type response"));
        when(weComCryptService.encryptMsg(anyString(), anyString(), anyString()))
                .thenReturn("<xml></xml>");

        weComMessageService.processCallback("sig", "ts", "nonce", "<xml></xml>");

        verify(defaultHandler).handle(any(WeComCallbackMessage.class));
    }
}
