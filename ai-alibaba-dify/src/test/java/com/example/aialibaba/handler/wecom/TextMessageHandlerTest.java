package com.example.aialibaba.handler.wecom;

import com.example.aialibaba.model.dto.ChatRequestDTO;
import com.example.aialibaba.model.dto.ChatResponseDTO;
import com.example.aialibaba.model.dto.wecom.request.WeComCallbackMessage;
import com.example.aialibaba.model.dto.wecom.response.WeComTextResponse;
import com.example.aialibaba.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TextMessageHandler Tests")
class TextMessageHandlerTest {

    @Mock
    private ChatService chatService;

    private TextMessageHandler textMessageHandler;

    @BeforeEach
    void setUp() {
        textMessageHandler = new TextMessageHandler(chatService);
    }

    @Test
    @DisplayName("supports should return true for text type")
    void testSupports_TextType() {
        assertTrue(textMessageHandler.supports("text"));
        assertTrue(textMessageHandler.supports("TEXT"));
    }

    @Test
    @DisplayName("supports should return false for non-text types")
    void testSupports_NonTextTypes() {
        assertFalse(textMessageHandler.supports("image"));
        assertFalse(textMessageHandler.supports("event"));
        assertFalse(textMessageHandler.supports("voice"));
    }

    @Test
    @DisplayName("handle should process text message and return AI response")
    void testHandle_Success() {
        WeComCallbackMessage message = createTextMessage("Hello AI", "user123");
        
        ChatResponseDTO chatResponse = ChatResponseDTO.builder()
                .status("success")
                .answer("Hello! How can I help you?")
                .build();
        
        when(chatService.sendMessage(any(ChatRequestDTO.class))).thenReturn(chatResponse);

        Object result = textMessageHandler.handle(message);

        assertInstanceOf(WeComTextResponse.class, result);
        WeComTextResponse response = (WeComTextResponse) result;
        assertEquals("Hello! How can I help you?", response.getText().getContent());
    }

    @Test
    @DisplayName("handle should return error message when text content is empty")
    void testHandle_EmptyContent() {
        WeComCallbackMessage message = createTextMessage("", "user123");

        Object result = textMessageHandler.handle(message);

        assertInstanceOf(WeComTextResponse.class, result);
        WeComTextResponse response = (WeComTextResponse) result;
        assertTrue(response.getText().getContent().contains("didn't receive"));
    }

    @Test
    @DisplayName("handle should return error message when chat service fails")
    void testHandle_ServiceFailure() {
        WeComCallbackMessage message = createTextMessage("Hello", "user123");
        
        when(chatService.sendMessage(any(ChatRequestDTO.class)))
                .thenThrow(new RuntimeException("Service error"));

        Object result = textMessageHandler.handle(message);

        assertInstanceOf(WeComTextResponse.class, result);
        WeComTextResponse response = (WeComTextResponse) result;
        assertTrue(response.getText().getContent().contains("error"));
    }

    @Test
    @DisplayName("getPriority should return expected value")
    void testGetPriority() {
        assertEquals(10, textMessageHandler.getPriority());
    }

    private WeComCallbackMessage createTextMessage(String content, String userId) {
        WeComCallbackMessage message = new WeComCallbackMessage();
        message.setMsgType("text");
        message.setMsgId("msg_" + System.currentTimeMillis());
        
        WeComCallbackMessage.TextContent textContent = new WeComCallbackMessage.TextContent();
        textContent.setContent(content);
        message.setText(textContent);
        
        WeComCallbackMessage.FromUser fromUser = new WeComCallbackMessage.FromUser();
        fromUser.setUserId(userId);
        message.setFrom(fromUser);
        
        return message;
    }
}
