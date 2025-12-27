package com.ithistory.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LlmResponse {
    private String title;
    private String introduction;
    private List<Section> sections;
    private List<ImageDescription> imageDescriptions;
    private String epilogue;
    private String references;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Section {
        private String heading;
        private String content;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageDescription {
        private String description;
        private String caption;
        private Integer orderIndex;
    }
}
