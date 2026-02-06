package com.example.aialibaba.model.dto.wecom;

import com.example.aialibaba.model.dto.wecom.request.WeComCallbackMessage;
import com.example.aialibaba.model.dto.wecom.response.WeComTextResponse;
import com.example.aialibaba.model.dto.wecom.response.WeComMarkdownResponse;
import com.example.aialibaba.model.dto.wecom.response.WeComStreamResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WeComDTO Tests")
class WeComDTOTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    @DisplayName("WeComCallbackMessage should deserialize text message correctly")
    void testWeComCallbackMessage_TextMessage() throws Exception {
        String json = """
            {
                "msgtype": "text",
                "msgid": "msg123456",
                "chatid": "chat789",
                "chattype": "single",
                "from": {
                    "UserId": "user001",
                    "Name": "Test User",
                    "Alias": "testuser"
                },
                "text": {
                    "content": "Hello, AI assistant!"
                }
            }
            """;

        WeComCallbackMessage message = objectMapper.readValue(json, WeComCallbackMessage.class);

        assertEquals("text", message.getMsgType());
        assertEquals("msg123456", message.getMsgId());
        assertEquals("chat789", message.getChatId());
        assertEquals("single", message.getChatType());
        assertEquals("user001", message.getUserId());
        assertEquals("Test User", message.getUserName());
        assertEquals("Hello, AI assistant!", message.getTextContent());
    }

    @Test
    @DisplayName("WeComCallbackMessage should deserialize event message correctly")
    void testWeComCallbackMessage_EventMessage() throws Exception {
        String json = """
            {
                "msgtype": "event",
                "event": {
                    "EventType": "enter_chat"
                },
                "from": {
                    "UserId": "user002",
                    "Name": "Another User"
                }
            }
            """;

        WeComCallbackMessage message = objectMapper.readValue(json, WeComCallbackMessage.class);

        assertEquals("event", message.getMsgType());
        assertNotNull(message.getEvent());
        assertEquals("enter_chat", message.getEvent().getEventType());
    }

    @Test
    @DisplayName("WeComTextResponse should serialize correctly")
    void testWeComTextResponse_Serialization() throws Exception {
        WeComTextResponse response = WeComTextResponse.of("Hello, how can I help?");

        String json = objectMapper.writeValueAsString(response);

        assertTrue(json.contains("\"msgtype\":\"text\""));
        assertTrue(json.contains("\"content\":\"Hello, how can I help?\""));
    }

    @Test
    @DisplayName("WeComMarkdownResponse should serialize correctly")
    void testWeComMarkdownResponse_Serialization() throws Exception {
        WeComMarkdownResponse response = WeComMarkdownResponse.of("**Bold** text");

        String json = objectMapper.writeValueAsString(response);

        assertTrue(json.contains("\"msgtype\":\"markdown\""));
        assertTrue(json.contains("**Bold** text"));
    }

    @Test
    @DisplayName("WeComStreamResponse delta should serialize correctly")
    void testWeComStreamResponse_Delta() throws Exception {
        WeComStreamResponse response = WeComStreamResponse.delta("stream123", "Hello");

        String json = objectMapper.writeValueAsString(response);

        assertTrue(json.contains("\"msgtype\":\"stream\""));
        assertTrue(json.contains("\"stream_id\":\"stream123\""));
        assertTrue(json.contains("\"delta\":\"Hello\""));
        assertTrue(json.contains("\"finish\":false"));
    }

    @Test
    @DisplayName("WeComStreamResponse finish should serialize correctly")
    void testWeComStreamResponse_Finish() throws Exception {
        WeComStreamResponse response = WeComStreamResponse.finish("stream123", "Done");

        String json = objectMapper.writeValueAsString(response);

        assertTrue(json.contains("\"finish\":true"));
    }

    @Test
    @DisplayName("WeComMessageType should parse correctly")
    void testWeComMessageType_FromValue() {
        assertEquals(WeComMessageType.TEXT, WeComMessageType.fromValue("text"));
        assertEquals(WeComMessageType.IMAGE, WeComMessageType.fromValue("image"));
        assertEquals(WeComMessageType.EVENT, WeComMessageType.fromValue("event"));
        assertEquals(WeComMessageType.TEXT, WeComMessageType.fromValue("unknown")); // default
    }
}
