package com.aiblog.dto;

import com.aiblog.model.Prompt;
import com.aiblog.model.PromptSource;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PromptResponse {

    private Long id;
    private String title;
    private String content;
    private String contentHtml;
    private String description;
    private String notes;
    private String notesHtml;
    private PromptSource source;
    private String model;
    private List<String> tags;
    private Boolean published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PromptResponse fromEntity(Prompt prompt, String contentHtml, String notesHtml) {
        PromptResponse response = new PromptResponse();
        response.setId(prompt.getId());
        response.setTitle(prompt.getTitle());
        response.setContent(prompt.getContent());
        response.setContentHtml(contentHtml);
        response.setDescription(prompt.getDescription());
        response.setNotes(prompt.getNotes());
        response.setNotesHtml(notesHtml);
        response.setSource(prompt.getSource());
        response.setModel(prompt.getModel());
        response.setTags(prompt.getTags());
        response.setPublished(prompt.getPublished());
        response.setCreatedAt(prompt.getCreatedAt());
        response.setUpdatedAt(prompt.getUpdatedAt());
        return response;
    }
}
