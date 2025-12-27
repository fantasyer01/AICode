package com.ithistory.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class StoryDto {
    private UUID storyId;
    private DateDto date;
    private String title;
    private String introduction;
    private String content;
    private String epilogue;
    private String references;
    private List<ImageDto> images;
    private LocalDateTime generatedAt;
    private Boolean cached;
    
    @Data
    public static class DateDto {
        private Integer month;
        private Integer day;
        
        public DateDto(Integer month, Integer day) {
            this.month = month;
            this.day = day;
        }
    }
}
