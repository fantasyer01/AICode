package com.example.aialibaba.model.dto.wecom.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Base callback message from WeChat Work intelligent robot.
 * This represents the decrypted JSON message received via callback.
 */
@Data
public class WeComCallbackMessage {
    
    @JsonProperty("msgtype")
    private String msgType;
    
    @JsonProperty("msgid")
    private String msgId;
    
    @JsonProperty("chatid")
    private String chatId;
    
    @JsonProperty("chattype")
    private String chatType;
    
    @JsonProperty("GetChatInfoUrl")
    private String getChatInfoUrl;
    
    @JsonProperty("from")
    private FromUser from;
    
    @JsonProperty("text")
    private TextContent text;
    
    @JsonProperty("image")
    private ImageContent image;
    
    @JsonProperty("event")
    private EventContent event;
    
    @JsonProperty("quote")
    private QuoteContent quote;
    
    @Data
    public static class FromUser {
        @JsonProperty("UserId")
        private String userId;
        
        @JsonProperty("Name")
        private String name;
        
        @JsonProperty("Alias")
        private String alias;
    }
    
    @Data
    public static class TextContent {
        @JsonProperty("content")
        private String content;
    }
    
    @Data
    public static class ImageContent {
        @JsonProperty("GetImgDataUrl")
        private String getImgDataUrl;
        
        @JsonProperty("FileName")
        private String fileName;
    }
    
    @Data
    public static class EventContent {
        @JsonProperty("EventType")
        private String eventType;
    }
    
    @Data
    public static class QuoteContent {
        @JsonProperty("user")
        private FromUser user;
        
        @JsonProperty("text")
        private TextContent text;
    }
    
    /**
     * Get the text content from the message
     */
    public String getTextContent() {
        return text != null ? text.getContent() : null;
    }
    
    /**
     * Get the user ID from the message
     */
    public String getUserId() {
        return from != null ? from.getUserId() : null;
    }
    
    /**
     * Get the user name from the message
     */
    public String getUserName() {
        return from != null ? from.getName() : null;
    }
}
