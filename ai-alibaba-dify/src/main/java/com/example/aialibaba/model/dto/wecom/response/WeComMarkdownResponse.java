package com.example.aialibaba.model.dto.wecom.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Markdown response for WeChat Work intelligent robot.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeComMarkdownResponse {
    
    @JsonProperty("msgtype")
    @Builder.Default
    private String msgType = "markdown";
    
    @JsonProperty("markdown")
    private MarkdownContent markdown;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarkdownContent {
        @JsonProperty("content")
        private String content;
    }
    
    /**
     * Create a markdown response with the given content
     */
    public static WeComMarkdownResponse of(String content) {
        return WeComMarkdownResponse.builder()
                .markdown(MarkdownContent.builder().content(content).build())
                .build();
    }
}
