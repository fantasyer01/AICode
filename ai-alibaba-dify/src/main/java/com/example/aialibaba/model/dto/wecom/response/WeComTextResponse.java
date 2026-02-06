package com.example.aialibaba.model.dto.wecom.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Text response for WeChat Work intelligent robot.
 * Used for passive reply to user messages.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeComTextResponse {
    
    @JsonProperty("msgtype")
    @Builder.Default
    private String msgType = "text";
    
    @JsonProperty("text")
    private TextContent text;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextContent {
        @JsonProperty("content")
        private String content;
    }
    
    /**
     * Create a text response with the given content
     */
    public static WeComTextResponse of(String content) {
        return WeComTextResponse.builder()
                .text(TextContent.builder().content(content).build())
                .build();
    }
}
