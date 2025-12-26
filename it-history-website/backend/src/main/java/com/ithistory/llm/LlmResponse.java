package com.ithistory.llm;

import lombok.Data;

import java.util.List;

@Data
public class LlmResponse {
    private String title;
    private List<Section> sections;
    private List<ImageDescription> imageDescriptions;
    
    @Data
    public static class Section {
        private String heading;
        private String content;
    }
    
    @Data
    public static class ImageDescription {
        private String description;
        private String caption;
        private Integer orderIndex;
    }
}
