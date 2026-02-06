package com.example.aialibaba.handler.wecom;

import com.example.aialibaba.model.dto.wecom.request.WeComCallbackMessage;
import com.example.aialibaba.model.dto.wecom.response.WeComTextResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EventMessageHandler Tests")
class EventMessageHandlerTest {

    private EventMessageHandler eventMessageHandler;

    @BeforeEach
    void setUp() {
        eventMessageHandler = new EventMessageHandler();
    }

    @Test
    @DisplayName("supports should return true for event type")
    void testSupports_EventType() {
        assertTrue(eventMessageHandler.supports("event"));
        assertTrue(eventMessageHandler.supports("EVENT"));
    }

    @Test
    @DisplayName("supports should return false for non-event types")
    void testSupports_NonEventTypes() {
        assertFalse(eventMessageHandler.supports("text"));
        assertFalse(eventMessageHandler.supports("image"));
    }

    @Test
    @DisplayName("handle should return greeting for enter_chat event")
    void testHandle_EnterChatEvent() {
        WeComCallbackMessage message = createEventMessage("enter_chat", "TestUser");

        Object result = eventMessageHandler.handle(message);

        assertInstanceOf(WeComTextResponse.class, result);
        WeComTextResponse response = (WeComTextResponse) result;
        assertTrue(response.getText().getContent().contains("TestUser"));
        assertTrue(response.getText().getContent().contains("Hello"));
    }

    @Test
    @DisplayName("handle should return generic greeting when username is null")
    void testHandle_EnterChatEvent_NoUsername() {
        WeComCallbackMessage message = createEventMessage("enter_chat", null);

        Object result = eventMessageHandler.handle(message);

        assertInstanceOf(WeComTextResponse.class, result);
        WeComTextResponse response = (WeComTextResponse) result;
        assertTrue(response.getText().getContent().contains("Hello"));
    }

    @Test
    @DisplayName("handle should return null for unknown event types")
    void testHandle_UnknownEvent() {
        WeComCallbackMessage message = createEventMessage("unknown_event", "User");

        Object result = eventMessageHandler.handle(message);

        assertNull(result);
    }

    @Test
    @DisplayName("getPriority should return expected value")
    void testGetPriority() {
        assertEquals(5, eventMessageHandler.getPriority());
    }

    private WeComCallbackMessage createEventMessage(String eventType, String userName) {
        WeComCallbackMessage message = new WeComCallbackMessage();
        message.setMsgType("event");
        
        WeComCallbackMessage.EventContent eventContent = new WeComCallbackMessage.EventContent();
        eventContent.setEventType(eventType);
        message.setEvent(eventContent);
        
        WeComCallbackMessage.FromUser fromUser = new WeComCallbackMessage.FromUser();
        fromUser.setUserId("user123");
        fromUser.setName(userName);
        message.setFrom(fromUser);
        
        return message;
    }
}
