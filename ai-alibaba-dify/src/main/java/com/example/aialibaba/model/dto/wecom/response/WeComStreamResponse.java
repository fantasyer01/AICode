package com.example.aialibaba.model.dto.wecom.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Streaming response for WeChat Work intelligent robot.
 * Supports incremental content updates for long-running AI responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeComStreamResponse {
    
    @JsonProperty("msgtype")
    @Builder.Default
    private String msgType = "stream";
    
    @JsonProperty("stream")
    private StreamContent stream;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamContent {
        /**
         * Stream ID for tracking incremental updates
         */
        @JsonProperty("stream_id")
        private String streamId;
        
        /**
         * The incremental content to append
         */
        @JsonProperty("delta")
        private String delta;
        
        /**
         * Whether this is the final message in the stream
         */
        @JsonProperty("finish")
        @Builder.Default
        private Boolean finish = false;
    }
    
    /**
     * Create a streaming response with delta content
     */
    public static WeComStreamResponse delta(String streamId, String delta) {
        return WeComStreamResponse.builder()
                .stream(StreamContent.builder()
                        .streamId(streamId)
                        .delta(delta)
                        .finish(false)
                        .build())
                .build();
    }
    
    /**
     * Create a final streaming response
     */
    public static WeComStreamResponse finish(String streamId, String delta) {
        return WeComStreamResponse.builder()
                .stream(StreamContent.builder()
                        .streamId(streamId)
                        .delta(delta)
                        .finish(true)
                        .build())
                .build();
    }
}
